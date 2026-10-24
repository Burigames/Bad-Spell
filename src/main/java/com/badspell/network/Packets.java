package com.badspell.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import com.badspell.server.MagicActions;

public final class Packets {
    private Packets() {}
    public static final int MAX_FORMULA_BYTES=30_000;
    public record OpenBook(int hand,byte[] formula,int ink) implements CustomPacketPayload {
        public static final Type<OpenBook> TYPE=new Type<>(Identifier.fromNamespaceAndPath("badspell","open_book"));
        public static final StreamCodec<RegistryFriendlyByteBuf,OpenBook> CODEC=CustomPacketPayload.codec(
            (p,b)->{b.writeVarInt(p.hand);b.writeByteArray(p.formula);b.writeVarInt(p.ink);},
            b->new OpenBook(b.readVarInt(),b.readByteArray(MAX_FORMULA_BYTES),b.readVarInt()));
        public Type<OpenBook> type(){return TYPE;}
    }
    /** action 0=save, 1=export authoritative book snapshot, 2=refill ink, 3=read private revision history. */
    public record BookAction(int hand,int action,byte[] formula) implements CustomPacketPayload {
        public static final Type<BookAction> TYPE=new Type<>(Identifier.fromNamespaceAndPath("badspell","book_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf,BookAction> CODEC=CustomPacketPayload.codec(
            (p,b)->{b.writeVarInt(p.hand);b.writeVarInt(p.action);b.writeByteArray(p.formula);},
            b->new BookAction(b.readVarInt(),b.readVarInt(),b.readByteArray(MAX_FORMULA_BYTES)));
        public Type<BookAction> type(){return TYPE;}
    }
    public record BookHistory(int hand,byte[] current,int ink,java.util.List<byte[]> revisions) implements CustomPacketPayload {
        public static final Type<BookHistory> TYPE=new Type<>(Identifier.fromNamespaceAndPath("badspell","book_history"));
        public static final StreamCodec<RegistryFriendlyByteBuf,BookHistory> CODEC=CustomPacketPayload.codec(
            (p,b)->{
                if(p.revisions.size()>20)throw new IllegalArgumentException("History limit");
                b.writeVarInt(p.hand);b.writeByteArray(p.current);b.writeVarInt(p.ink);b.writeVarInt(p.revisions.size());
                for(byte[] revision:p.revisions)b.writeByteArray(revision);
            },b->{
                int hand=b.readVarInt();byte[] current=b.readByteArray(MAX_FORMULA_BYTES);int ink=b.readVarInt(),count=b.readVarInt();
                if(count<0 || count>20)throw new IllegalArgumentException("History limit");
                java.util.List<byte[]> revisions=new java.util.ArrayList<>();
                for(int i=0;i<count;i++)revisions.add(b.readByteArray(MAX_FORMULA_BYTES));
                return new BookHistory(hand,current,ink,java.util.List.copyOf(revisions));
            });
        public Type<BookHistory> type(){return TYPE;}
    }
    /** Only a hand and a one-step intent cross the trust boundary; no client snapshot or capacity. */
    public record GloveSlot(int hand,int direction) implements CustomPacketPayload {
        public static final Type<GloveSlot> TYPE=new Type<>(Identifier.fromNamespaceAndPath("badspell","glove_slot"));
        public static final StreamCodec<RegistryFriendlyByteBuf,GloveSlot> CODEC=CustomPacketPayload.codec(
            (p,b)->{b.writeVarInt(p.hand);b.writeVarInt(p.direction);},b->new GloveSlot(b.readVarInt(),b.readVarInt()));
        public Type<GloveSlot> type(){return TYPE;}
    }
    public record LightUpdate(BlockPos position,int strength) implements CustomPacketPayload {
        public static final Type<LightUpdate> TYPE=new Type<>(Identifier.fromNamespaceAndPath("badspell","light_update"));
        public static final StreamCodec<RegistryFriendlyByteBuf,LightUpdate> CODEC=CustomPacketPayload.codec(
            (p,b)->{b.writeBlockPos(p.position);b.writeVarInt(p.strength);},b->new LightUpdate(b.readBlockPos(),b.readVarInt()));
        public Type<LightUpdate> type(){return TYPE;}
    }
    public record LibraryAction(int hand,int action,java.util.UUID id,String title,String tags,int page) implements CustomPacketPayload {
        public static final Type<LibraryAction> TYPE=new Type<>(Identifier.fromNamespaceAndPath("badspell","library_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf,LibraryAction> CODEC=CustomPacketPayload.codec((p,b)->{b.writeVarInt(p.hand);b.writeVarInt(p.action);b.writeUUID(p.id);b.writeUtf(p.title,96);b.writeUtf(p.tags,256);b.writeVarInt(p.page);},b->new LibraryAction(b.readVarInt(),b.readVarInt(),b.readUUID(),b.readUtf(96),b.readUtf(256),b.readVarInt()));
        public Type<LibraryAction> type(){return TYPE;}
    }
    public record BoardAction(int hand,BlockPos position,int face,byte[] formula) implements CustomPacketPayload {
        public static final Type<BoardAction> TYPE=new Type<>(Identifier.fromNamespaceAndPath("badspell","board_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf,BoardAction> CODEC=CustomPacketPayload.codec((p,b)->{b.writeVarInt(p.hand);b.writeBlockPos(p.position);b.writeVarInt(p.face);b.writeByteArray(p.formula);},b->new BoardAction(b.readVarInt(),b.readBlockPos(),b.readVarInt(),b.readByteArray(MAX_FORMULA_BYTES)));
        public Type<BoardAction> type(){return TYPE;}
    }
    public record BoardView(int hand,BlockPos position,int face,byte[] formula,int durability) implements CustomPacketPayload {
        public static final Type<BoardView> TYPE=new Type<>(Identifier.fromNamespaceAndPath("badspell","board_view"));
        public static final StreamCodec<RegistryFriendlyByteBuf,BoardView> CODEC=CustomPacketPayload.codec((p,b)->{b.writeVarInt(p.hand);b.writeBlockPos(p.position);b.writeVarInt(p.face);b.writeByteArray(p.formula);b.writeVarInt(p.durability);},b->new BoardView(b.readVarInt(),b.readBlockPos(),b.readVarInt(),b.readByteArray(MAX_FORMULA_BYTES),b.readVarInt()));
        public Type<BoardView> type(){return TYPE;}
    }
    public record BoardMarks(BlockPos position,int face,byte[] glyphs) implements CustomPacketPayload {
        public static final Type<BoardMarks> TYPE=new Type<>(Identifier.fromNamespaceAndPath("badspell","board_marks"));
        public static final StreamCodec<RegistryFriendlyByteBuf,BoardMarks> CODEC=CustomPacketPayload.codec((p,b)->{b.writeBlockPos(p.position);b.writeVarInt(p.face);b.writeByteArray(p.glyphs);},b->new BoardMarks(b.readBlockPos(),b.readVarInt(),b.readByteArray(16)));
        public Type<BoardMarks> type(){return TYPE;}
    }
    public record MonocleToggle() implements CustomPacketPayload {
        public static final Type<MonocleToggle> TYPE=new Type<>(Identifier.fromNamespaceAndPath("badspell","monocle_toggle"));
        public static final StreamCodec<RegistryFriendlyByteBuf,MonocleToggle> CODEC=StreamCodec.unit(new MonocleToggle());public Type<MonocleToggle> type(){return TYPE;}
    }
    public record ScanInfo(BlockPos position,String text,int intensity) implements CustomPacketPayload {
        public static final Type<ScanInfo> TYPE=new Type<>(Identifier.fromNamespaceAndPath("badspell","scan_info"));
        public static final StreamCodec<RegistryFriendlyByteBuf,ScanInfo> CODEC=CustomPacketPayload.codec((p,b)->{b.writeBlockPos(p.position);b.writeUtf(p.text,512);b.writeVarInt(p.intensity);},b->new ScanInfo(b.readBlockPos(),b.readUtf(512),b.readVarInt()));
        public Type<ScanInfo> type(){return TYPE;}
    }
    public record LibraryEntry(byte[] formula,String tags){}
    public record LibraryPage(int hand,int page,int total,java.util.List<LibraryEntry> entries) implements CustomPacketPayload {
        public static final Type<LibraryPage> TYPE=new Type<>(Identifier.fromNamespaceAndPath("badspell","library_page"));
        public static final StreamCodec<RegistryFriendlyByteBuf,LibraryPage> CODEC=CustomPacketPayload.codec((p,b)->{b.writeVarInt(p.hand);b.writeVarInt(p.page);b.writeVarInt(p.total);b.writeVarInt(p.entries.size());for(var e:p.entries){b.writeByteArray(e.formula);b.writeUtf(e.tags,256);}},b->{int hand=b.readVarInt(),page=b.readVarInt(),total=b.readVarInt(),count=b.readVarInt();if(count<0||count>8)throw new IllegalArgumentException("Page limit");var entries=new java.util.ArrayList<LibraryEntry>();for(int i=0;i<count;i++)entries.add(new LibraryEntry(b.readByteArray(MAX_FORMULA_BYTES),b.readUtf(256)));return new LibraryPage(hand,page,total,java.util.List.copyOf(entries));});
        public Type<LibraryPage> type(){return TYPE;}
    }
    public record OpenCodex() implements CustomPacketPayload {
        public static final Type<OpenCodex> TYPE=new Type<>(Identifier.fromNamespaceAndPath("badspell","open_codex"));
        public static final StreamCodec<RegistryFriendlyByteBuf,OpenCodex> CODEC=StreamCodec.unit(new OpenCodex());
        public Type<OpenCodex> type(){return TYPE;}
    }
    public record ResearchAction(int action,String rune,java.util.List<Integer> answer,boolean open) implements CustomPacketPayload {
        public static final Type<ResearchAction> TYPE=new Type<>(Identifier.fromNamespaceAndPath("badspell","research_action"));
        public static final StreamCodec<RegistryFriendlyByteBuf,ResearchAction> CODEC=CustomPacketPayload.codec((p,b)->{b.writeVarInt(p.action);b.writeUtf(p.rune,80);b.writeVarInt(p.answer.size());for(int a:p.answer)b.writeVarInt(a);b.writeBoolean(p.open);},b->{int action=b.readVarInt();String rune=b.readUtf(80);int count=b.readVarInt();if(count<0||count>12)throw new IllegalArgumentException("Research ports");var answer=new java.util.ArrayList<Integer>();for(int i=0;i<count;i++)answer.add(b.readVarInt());return new ResearchAction(action,rune,java.util.List.copyOf(answer),b.readBoolean());});
        public Type<ResearchAction> type(){return TYPE;}
    }
    public record ResearchState(boolean open,long seed,java.util.List<String> known,java.util.List<String> discovered,java.util.List<String> stations,int decoded,boolean formula,String focus,String message) implements CustomPacketPayload {
        public static final Type<ResearchState> TYPE=new Type<>(Identifier.fromNamespaceAndPath("badspell","research_state"));
        private static void strings(RegistryFriendlyByteBuf b,java.util.List<String> values,int max){if(values.size()>max)throw new IllegalArgumentException("Research collection");b.writeVarInt(values.size());for(String s:values)b.writeUtf(s,128);}
        private static java.util.List<String> strings(RegistryFriendlyByteBuf b,int max){int n=b.readVarInt();if(n<0||n>max)throw new IllegalArgumentException("Research collection");var list=new java.util.ArrayList<String>();for(int i=0;i<n;i++)list.add(b.readUtf(128));return java.util.List.copyOf(list);}
        public static final StreamCodec<RegistryFriendlyByteBuf,ResearchState> CODEC=CustomPacketPayload.codec((p,b)->{b.writeBoolean(p.open);b.writeLong(p.seed);strings(b,p.known,128);strings(b,p.discovered,4096);strings(b,p.stations,32);b.writeVarInt(p.decoded);b.writeBoolean(p.formula);b.writeUtf(p.focus,80);b.writeUtf(p.message,80);},b->new ResearchState(b.readBoolean(),b.readLong(),strings(b,128),strings(b,4096),strings(b,32),b.readVarInt(),b.readBoolean(),b.readUtf(80),b.readUtf(80)));
        public Type<ResearchState> type(){return TYPE;}
    }
    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar=event.registrar("5");
        registrar.playToClient(ResearchState.TYPE,ResearchState.CODEC);
        registrar.playToServer(ResearchAction.TYPE,ResearchAction.CODEC,(p,c)->{if(c.player() instanceof net.minecraft.server.level.ServerPlayer player)com.badspell.progression.ResearchActions.handle(player,p);});
        registrar.playToClient(OpenCodex.TYPE,OpenCodex.CODEC);
        registrar.playToServer(MonocleToggle.TYPE,MonocleToggle.CODEC,(p,c)->{if(c.player() instanceof net.minecraft.server.level.ServerPlayer player)com.badspell.server.ToolActions.toggleMonocle(player);});
        registrar.playToClient(BoardMarks.TYPE,BoardMarks.CODEC);
        registrar.playToClient(BoardView.TYPE,BoardView.CODEC);registrar.playToClient(ScanInfo.TYPE,ScanInfo.CODEC);
        registrar.playToServer(BoardAction.TYPE,BoardAction.CODEC,(p,c)->{if(c.player() instanceof net.minecraft.server.level.ServerPlayer player)com.badspell.server.ToolActions.handle(player,p);});
        registrar.playToClient(LibraryPage.TYPE,LibraryPage.CODEC);
        registrar.playToServer(LibraryAction.TYPE,LibraryAction.CODEC,(p,c)->{if(c.player() instanceof net.minecraft.server.level.ServerPlayer player)com.badspell.server.FormulaLibrary.handle(player,p);});
        registrar.playToClient(OpenBook.TYPE,OpenBook.CODEC);
        registrar.playToClient(BookHistory.TYPE,BookHistory.CODEC);
        registrar.playToServer(GloveSlot.TYPE,GloveSlot.CODEC,(packet,context)->{
            if(context.player() instanceof net.minecraft.server.level.ServerPlayer player)MagicActions.selectSlot(player,packet.hand(),packet.direction());
        });
        registrar.playToClient(LightUpdate.TYPE,LightUpdate.CODEC);
        registrar.playToServer(BookAction.TYPE,BookAction.CODEC,(packet,context)->{
            if(context.player() instanceof net.minecraft.server.level.ServerPlayer player)MagicActions.handle(player,packet);
        });
    }
}
