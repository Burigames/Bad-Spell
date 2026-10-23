package com.badspell.server;

import com.badspell.network.Packets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.*;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.*;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/** Temporary auxiliary illumination: no block placement, with persisted expiry and chunk-safe removal. */
public final class LightFields extends SavedData {
    public record Field(BlockPos position,int strength,long expiry) {
        static final Codec<Field> CODEC=RecordCodecBuilder.create(i->i.group(
            BlockPos.CODEC.fieldOf("position").forGetter(Field::position),
            Codec.intRange(1,8).fieldOf("strength").forGetter(Field::strength),
            Codec.LONG.fieldOf("expiry").forGetter(Field::expiry)).apply(i,Field::new));
    }
    private static final Codec<LightFields> CODEC=Field.CODEC.listOf().xmap(LightFields::new,s->List.copyOf(s.fields.values()));
    public static final SavedDataType<LightFields> TYPE=new SavedDataType<>(Identifier.fromNamespaceAndPath("badspell","light_fields"),()->new LightFields(List.of()),CODEC);
    private final Map<BlockPos,Field> fields=new HashMap<>();
    private boolean indexed;
    private LightFields(List<Field> stored) { for(var f:stored)fields.put(f.position(),f); }
    public static LightFields get(ServerLevel level) {
        var data=level.getDataStorage().computeIfAbsent(TYPE);
        if(!data.indexed) {
            data.fields.values().forEach(f->com.badspell.lighting.FieldLightSources.set(level,f.position(),f.strength()));
            data.indexed=true;
        }
        return data;
    }
    public static void load(net.neoforged.neoforge.event.level.LevelEvent.Load event) {
        if(event.getLevel() instanceof ServerLevel level)get(level);
    }
    public static void sendChunk(net.neoforged.neoforge.event.level.ChunkWatchEvent.Sent event) {
        get(event.getLevel());
        com.badspell.lighting.FieldLightSources.inChunk(event.getLevel(),event.getPos()).forEach((position,strength)->
            PacketDistributor.sendToPlayer(event.getPlayer(),new Packets.LightUpdate(position,strength)));
    }
    public boolean canAdd(ServerLevel level,BlockPos position) {
        if(fields.size()>=1024 || fields.containsKey(position) || !level.getChunkSource().hasChunk((position).getX() >> 4,(position).getZ() >> 4))return false;
        var manager=level.getChunkAt(position).getAuxLightManager(ChunkPos.containing(position));
        return manager.getLightAt(position)==0;
    }
    public void add(ServerLevel level,BlockPos position,int strength,int duration) {
        if(!canAdd(level,position))throw new IllegalStateException("Light target changed");
        var f=new Field(position.immutable(),strength,Math.addExact(level.getGameTime(),duration));
        fields.put(f.position(),f);setDirty();update(level,f.position(),strength);
        // Cosmetic feedback is separate from the committed world/energy action.
        try { level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,position.getX()+.5,position.getY()+.5,position.getZ()+.5,4,.12,.12,.12,.005); }
        catch(RuntimeException error){com.mojang.logging.LogUtils.getLogger().warn("Bad Spell cast particles unavailable",error);}
    }
    private static void update(ServerLevel level,BlockPos position,int strength) {
        com.badspell.lighting.FieldLightSources.set(level,position,strength);
        com.badspell.lighting.FieldLightSources.syncSection(level,position);
        level.getChunkAt(position).getAuxLightManager(ChunkPos.containing(position)).setLightAt(position,strength);
        // Notification failure must not roll back energy after a committed light change.
        try { PacketDistributor.sendToPlayersTrackingChunk(level,ChunkPos.containing(position),new Packets.LightUpdate(position,strength)); }
        catch(RuntimeException error) { com.mojang.logging.LogUtils.getLogger().warn("Bad Spell light notification failed; normal chunk sync retains authoritative state",error); }
    }
    public static void tick(LevelTickEvent.Post event) {
        if(!(event.getLevel() instanceof ServerLevel level))return;
        var data=get(level);var iterator=data.fields.values().iterator();
        while(iterator.hasNext()) {
            var f=iterator.next();
            // Never force-load a chunk. An unloaded expired field is cleared on its first loaded tick.
            if(f.expiry()<=level.getGameTime() && level.getChunkSource().hasChunk((f.position()).getX() >> 4,(f.position()).getZ() >> 4)) {
                var manager=level.getChunkAt(f.position()).getAuxLightManager(ChunkPos.containing(f.position()));
                if(manager.getLightAt(f.position())==f.strength())update(level,f.position(),0);
                iterator.remove();data.setDirty();
            }
        }
    }
}
