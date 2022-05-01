package com.ishland.c2me.natives.mixin;

import com.ishland.c2me.natives.common.NativeMemoryTracker;
import com.ishland.c2me.natives.common.NativeInterface;
import io.netty.util.internal.PlatformDependent;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = PerlinNoiseSampler.class, priority = 1200)
public class MixinPerlinNoiseSampler {

    @Shadow @Final private byte[] permutations;
    @Shadow @Final public double originX;
    @Shadow @Final public double originY;
    @Shadow @Final public double originZ;
    @Unique
    private long permutationsPointer = 0L;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.permutationsPointer = NativeMemoryTracker.allocateMemory(this, 256);
        PlatformDependent.copyMemory(this.permutations, 0, this.permutationsPointer, 256);
    }

    /**
     * @author ishland
     * @reason use native method
     */
    @Deprecated
    @Overwrite
    public double sample(double x, double y, double z, double yScale, double yMax) {
        if (permutationsPointer == 0L) throw new NullPointerException();
        return NativeInterface.perlinSample(this.permutationsPointer, this.originX, this.originY, this.originZ, x, y, z, yScale, yMax);
    }

}
