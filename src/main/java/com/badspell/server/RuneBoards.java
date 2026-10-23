package com.badspell.server;

import com.badspell.formula.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.*;

/** Six independently owned 4×4 physical rune boards per block; only valid confirmed graphs execute. */
public final class RuneBoards extends SavedData {
    public record Board(BlockPos position,int face,String snapshot){
        static final Codec<Board> CODEC=RecordCodecBuilder.create(i->i.group(BlockPos.CODEC.fieldOf("position").forGetter(Board::position),Codec.intRange(0,5).fieldOf("face").forGetter(Board::face),Codec.STRING.fieldOf("snapshot").forGetter(Board::snapshot)).apply(i,Board::new));
        public Formula formula(){try{return FormulaCodec.decode(Base64.getDecoder().decode(snapshot));}catch(java.io.IOException error){throw new IllegalArgumentException(error);}}
    }
    private static final Map<ServerLevel,RuneBoards> ACTIVE=Collections.synchronizedMap(new WeakHashMap<>());
    private final it.unimi.dsi.fastutil.longs.LongOpenHashSet occupied=new it.unimi.dsi.fastutil.longs.LongOpenHashSet();
    private final Map<String,Board> boards=new HashMap<>();
    private static final Codec<RuneBoards> CODEC=Board.CODEC.sizeLimitedListOf(4096).xmap(RuneBoards::new,data->List.copyOf(data.boards.values()));
    public static final SavedDataType<RuneBoards> TYPE=new SavedDataType<>(Identifier.fromNamespaceAndPath("badspell","rune_boards"),()->new RuneBoards(List.of()),CODEC);
    private RuneBoards(List<Board> stored){stored.forEach(board->{boards.put(key(board.position(),board.face()),board);occupied.add(board.position().asLong());});}
    private static String key(BlockPos at,int face){return at.asLong()+":"+face;}
    private static byte[] glyphs(Formula formula){byte[] glyphs=new byte[16];for(var node:formula.nodes()){int cell=node.x()/154+node.y()/100*4;if(cell>=0&&cell<16)glyphs[cell]=(byte)RuneGlyphs.code(node.rune());}return glyphs;}
    public static void sendChunk(net.neoforged.neoforge.event.level.ChunkWatchEvent.Sent event){for(var board:get(event.getLevel()).boards.values())if(net.minecraft.world.level.ChunkPos.containing(board.position()).equals(event.getPos()))net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(event.getPlayer(),new com.badspell.network.Packets.BoardMarks(board.position(),board.face(),glyphs(board.formula())));}
    public static void sync(ServerLevel level,BlockPos at,int face,Formula formula){net.neoforged.neoforge.network.PacketDistributor.sendToPlayersTrackingChunk(level,net.minecraft.world.level.ChunkPos.containing(at),new com.badspell.network.Packets.BoardMarks(at,face,formula==null?new byte[16]:glyphs(formula)));}
    public static RuneBoards get(ServerLevel level){var data=level.getDataStorage().computeIfAbsent(TYPE);ACTIVE.put(level,data);return data;}
    public Board find(BlockPos at,int face){return boards.get(key(at,face));}
    public void put(BlockPos at,int face,Formula formula) throws java.io.IOException {
        if(boards.size()>=4096&&!boards.containsKey(key(at,face)))throw new java.io.IOException("Physical board limit");
        occupied.add(at.asLong());boards.put(key(at,face),new Board(at.immutable(),face,Base64.getEncoder().encodeToString(FormulaCodec.encode(formula))));setDirty();
    }
    public static void load(net.neoforged.neoforge.event.level.LevelEvent.Load event){if(event.getLevel() instanceof ServerLevel level)get(level);}
    public static void replaced(ServerLevel level,BlockPos at){var self=ACTIVE.get(level);if(self==null||self.occupied.isEmpty()||!self.occupied.remove(at.asLong()))return;for(int face=0;face<6;face++)if(self.boards.remove(key(at,face))!=null)sync(level,at,face,null);self.setDirty();}
    private static final Map<ServerLevel,Map<BlockPos,net.minecraft.world.level.block.state.BlockState>> pending=new WeakHashMap<>();
    public static void tick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event){if(event.getLevel() instanceof ServerLevel level){var changes=pending.remove(level);if(changes==null)return;var self=get(level);boolean changed=false;for(var entry:changes.entrySet())if(level.getBlockState(entry.getKey())!=entry.getValue())for(int face=0;face<6;face++){boolean removed=self.boards.remove(key(entry.getKey(),face))!=null;changed|=removed;if(removed)sync(level,entry.getKey(),face,null);}if(changed)self.setDirty();}}
    public static void broken(net.neoforged.neoforge.event.level.block.BreakBlockEvent event){if(!event.isCanceled()&&event.getLevel() instanceof ServerLevel level)pending.computeIfAbsent(level,k->new HashMap<>()).put(event.getPos().immutable(),event.getState());}
}
