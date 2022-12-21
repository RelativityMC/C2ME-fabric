package com.ishland.c2me.natives.mixin.spline;

import com.ishland.c2me.natives.common.CompiledSpline;
import com.ishland.c2me.natives.common.NativeInterface;
import com.ishland.c2me.natives.common.NativeMemoryTracker;
import net.minecraft.util.math.Spline;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Spline.FixedFloatFunction.class)
public class MixinSplineFixedFloatFunction implements CompiledSpline {

    @Shadow @Final private float value;
    @Unique
    private long pointer = 0L;

    @Unique
    private String errorMessage = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.pointer = NativeInterface.createSplineConstant(this.value);
        NativeMemoryTracker.registerAllocatedMemory(this, NativeInterface.SIZEOF_spline_data_constant, this.pointer);
    }

    @Override
    public void compileIfNeeded(boolean includeParents) {
        // no-op: always compilable
    }

    @Override
    public long getSplinePointer() {
        return this.pointer;
    }

    @Nullable
    @Override
    public String getCompilationFailedReason() {
        return this.errorMessage;
    }
}
