package com.badspell.mixin;

import com.badspell.lighting.FieldLightSources;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Minecraft 26.2: auxiliary entries alone do not make vanilla air emit light. No blocks are replaced. */
@Mixin(net.minecraft.world.level.lighting.BlockLightEngine.class)
@SuppressWarnings({"rawtypes","unchecked"})
public abstract class BlockLightEngineMixin extends LightEngine {
    protected BlockLightEngineMixin(){super(null,null);}
    @Redirect(method="*",at=@At(value="INVOKE",target="Lnet/minecraft/world/level/block/state/BlockState;getLightEmission(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I"),require=2)
    private int badspell$fieldEmission(BlockState state,BlockGetter world,BlockPos position) {
        return Math.max(state.getLightEmission(world,position),FieldLightSources.emission(world,position));
    }
    @Inject(method="propagateLightSources",at=@At("TAIL"))
    private void badspell$restoreFieldSources(ChunkPos position,CallbackInfo callback) {
        var chunk=chunkSource.getChunkForLighting(position.x(),position.z());
        if(chunk==null)return;
        FieldLightSources.inChunk(chunkSource.getLevel(),position).forEach((pos,strength)->
            enqueueIncrease(pos.asLong(),LightEngine.QueueEntry.increaseLightFromEmission(strength,isEmptyShape(chunk.getBlockState(pos)))));
    }
}
