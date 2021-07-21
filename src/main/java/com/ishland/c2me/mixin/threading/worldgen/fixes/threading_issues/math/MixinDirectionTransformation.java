package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues.math;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.DirectionTransformation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DirectionTransformation.class)
public abstract class MixinDirectionTransformation {

    @Shadow public abstract Direction map(Direction direction);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        map(Direction.UP); // force load mapping to prevent further issues
    }

}
