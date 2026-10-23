package com.badspell.mixin;

import com.badspell.lighting.FieldLightSources;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.lighting.LevelLightEngine;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** A field is a light source even when its entire 16-block section contains no physical blocks. */
@Mixin(LevelLightEngine.class)
public abstract class LevelLightEngineMixin {
    @Shadow @Final private LevelHeightAccessor levelHeightAccessor;
    @Inject(method="updateSectionStatus",at=@At("HEAD"),cancellable=true)
    private void badspell$retainFieldSection(SectionPos section,boolean empty,CallbackInfo callback) {
        if(empty && levelHeightAccessor instanceof BlockGetter world && FieldLightSources.hasInSection(world,section)) {
            ((LevelLightEngine)(Object)this).updateSectionStatus(section,false);
            callback.cancel();
        }
    }
}
