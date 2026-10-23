package com.badspell.mixin;

import com.badspell.server.RuneBoards;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Physical engravings are invalidated by every real block replacement, including explosions and commands. */
@Mixin(LevelChunk.class)
public abstract class LevelChunkMixin {
    @Shadow public abstract Level getLevel();
    @Inject(method="setBlockState",at=@At("RETURN"))
    private void badspell$replace(BlockPos at,BlockState state,int flags,CallbackInfoReturnable<BlockState> callback){var old=callback.getReturnValue();if(old!=null&&old.getBlock()!=state.getBlock()&&getLevel() instanceof net.minecraft.server.level.ServerLevel level)RuneBoards.replaced(level,at);}
}
