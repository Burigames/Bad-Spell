package com.badspell.lighting;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;

/** Thread-safe, derived emission index. World ownership is weak; SavedData remains authoritative. */
public final class FieldLightSources {
    private static final Map<BlockGetter,ConcurrentHashMap<ChunkPos,ConcurrentHashMap<BlockPos,Integer>>> WORLDS=Collections.synchronizedMap(new WeakHashMap<>());
    private FieldLightSources() {}
    public static void set(BlockGetter world,BlockPos position,int strength) {
        if(strength<0 || strength>8)throw new IllegalArgumentException("Light strength 0..8");
        var chunks=WORLDS.computeIfAbsent(world,k->new ConcurrentHashMap<>());
        ChunkPos chunk=ChunkPos.containing(position);
        chunks.compute(chunk,(key,sources)->{
            if(sources==null) {
                if(strength==0)return null;
                sources=new ConcurrentHashMap<>();
            }
            if(strength==0)sources.remove(position);else sources.put(position.immutable(),strength);
            return sources.isEmpty()?null:sources;
        });
    }
    public static int emission(BlockGetter world,BlockPos position) {
        var chunks=WORLDS.get(world);if(chunks==null)return 0;
        var sources=chunks.get(ChunkPos.containing(position));
        return sources==null?0:sources.getOrDefault(position,0);
    }
    public static boolean hasInSection(BlockGetter world,SectionPos section) {
        return inChunk(world,new ChunkPos(section.x(),section.z())).keySet().stream().anyMatch(pos->SectionPos.blockToSectionCoord(pos.getY())==section.y());
    }
    public static void syncSection(net.minecraft.world.level.Level level,BlockPos position) {
        var section=SectionPos.of(position);
        boolean empty=level.getChunkAt(position).getSection(level.getSectionIndex(position.getY())).hasOnlyAir();
        level.getChunkSource().getLightEngine().updateSectionStatus(section,empty && !hasInSection(level,section));
    }
    public static void unloadClientChunk(net.neoforged.neoforge.event.level.ChunkEvent.Unload event) {
        if(event.getLevel() instanceof net.minecraft.world.level.Level level && level.isClientSide()) {
            var chunks=WORLDS.get(level);if(chunks!=null)chunks.remove(event.getChunk().getPos());
        }
    }
    public static Map<BlockPos,Integer> inChunk(BlockGetter world,ChunkPos chunk) {
        var chunks=WORLDS.get(world);if(chunks==null)return Map.of();
        var sources=chunks.get(chunk);return sources==null?Map.of():Map.copyOf(sources);
    }
}
