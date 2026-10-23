package com.badspell.magic;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.EntityHitResult;

/** Same bounded targeting rule for server execution and the client crosshair preview. */
public final class SpellTargets {
    private SpellTargets(){}
    public static EntityHitResult entity(Entity actor){
        var from=actor.getEyePosition();var look=actor.getLookAngle();var to=from.add(look.scale(12));
        var wall=actor.level().clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,actor));
        return ProjectileUtil.getEntityHitResult(actor,from,to,actor.getBoundingBox().expandTowards(look.scale(12)).inflate(1),e->e!=actor&&e instanceof LivingEntity&&e.isAlive()&&!e.isSpectator(),from.distanceToSqr(wall.getLocation()));
    }
}
