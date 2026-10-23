package com.badspell.server;

import net.minecraft.core.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.*;

/** Reuses the official interaction event so normal block-protection listeners can veto magic actions. */
public final class MagicPermissions {
    private MagicPermissions(){}
    public static boolean mayUse(ServerPlayer player,BlockPos at,Direction face){
        var level=player.level();if(level.isOutsideBuildHeight(at)||!level.getChunkSource().hasChunk(at.getX()>>4,at.getZ()>>4)||!level.getWorldBorder().isWithinBounds(at)||!level.mayInteract(player,at))return false;
        var event=net.neoforged.neoforge.common.CommonHooks.onRightClickBlock(player,InteractionHand.MAIN_HAND,at,new BlockHitResult(Vec3.atCenterOf(at),face,at,false));
        return !event.isCanceled()&&event.getUseBlock()!=net.minecraft.util.TriState.FALSE&&event.getUseItem()!=net.minecraft.util.TriState.FALSE;
    }
}
