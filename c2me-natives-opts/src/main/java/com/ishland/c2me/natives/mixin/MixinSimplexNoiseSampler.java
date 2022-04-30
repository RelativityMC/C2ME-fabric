package com.ishland.c2me.natives.mixin;

import com.ishland.c2me.natives.common.Cleaners;
import com.ishland.c2me.natives.common.NativesInterface;
import com.ishland.c2me.natives.common.NativesStruct;
import com.ishland.c2me.natives.common.UnsafeUtil;
import io.netty.util.internal.PlatformDependent;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SimplexNoiseSampler.class, priority = 1200)
public class MixinSimplexNoiseSampler implements NativesStruct {

    @Shadow
    @Final
    private int[] permutations;
    @Unique
    private long permutationsPointer = 0L;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.permutationsPointer = UnsafeUtil.getInstance().allocateMemory(4 * 256);
        byte[] tmp = new byte[4 * 256];
        UnsafeUtil.getInstance().copyMemory(
                permutations,
                UnsafeUtil.getInstance().arrayBaseOffset(int[].class),
                tmp,
                UnsafeUtil.getInstance().arrayBaseOffset(byte[].class),
                4 * 256
        );
        PlatformDependent.copyMemory(tmp, 0, this.permutationsPointer, 4 * 256);
        Cleaners.register(this, this.permutationsPointer);
    }

    /**
     * @author ishland
     * @reason use native method
     */
    @Overwrite
    public double sample(double x, double y) {
        return NativesInterface.simplexSample(this.permutationsPointer, x, y);
    }

    @Override
    public long getNativePointer() {
        return this.permutationsPointer;
    }
}
