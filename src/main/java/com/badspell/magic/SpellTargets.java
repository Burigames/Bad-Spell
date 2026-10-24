package com.badspell.magic;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;

/** Same bounded targeting rule for server execution and the client crosshair preview. */
public final class SpellTargets {
    private SpellTargets(){}
    public static net.minecraft.core.BlockPos position(Entity actor){
        var hit=actor.pick(12,1,false);if(hit.getType()==net.minecraft.world.phys.HitResult.Type.BLOCK){var block=(net.minecraft.world.phys.BlockHitResult)hit;return block.getBlockPos().relative(block.getDirection());}
        double distance=12;net.minecraft.core.BlockPos target;
        do{target=net.minecraft.core.BlockPos.containing(actor.getEyePosition().add(actor.getLookAngle().scale(distance)));distance-=.25;}while(target.distSqr(actor.blockPosition())>144&&distance>0);
        return target;
    }
    public static EntityHitResult entity(Entity actor){
        var from=actor.getEyePosition();var look=actor.getLookAngle();var to=from.add(look.scale(12));
        var wall=actor.level().clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,actor));
        return ProjectileUtil.getEntityHitResult(actor,from,to,actor.getBoundingBox().expandTowards(look.scale(12)).inflate(1),e->e!=actor&&e instanceof LivingEntity&&e.isAlive()&&!e.isSpectator(),from.distanceToSqr(wall.getLocation()));
    }
}
