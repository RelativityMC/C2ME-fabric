package com.ishland.c2me.opts.natives_math.mixin;

import com.ishland.c2me.base.mixin.access.ISimplexNoiseSampler;
import com.ishland.c2me.opts.natives_math.common.Bindings;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.SimplexNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.invoke.VarHandle;

@Mixin(DensityFunctionTypes.EndIslands.class)
public abstract class MixinDFTypesEndIslands {

    @Shadow @Final private SimplexNoiseSampler sampler;

    @Shadow
    protected static float sample(SimplexNoiseSampler sampler, int x, int z) {
        return 0;
    }

    @Unique
    private final Arena c2me$arena = Arena.ofAuto();
    @Unique
    private MemorySegment c2me$samplerData = null;
    @Unique
    private long c2me$samplerDataPtr;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void postInit(CallbackInfo ci) {
        int[] permutation = ((ISimplexNoiseSampler) this.sampler).getPermutation();
        MemorySegment segment = this.c2me$arena.allocate(permutation.length * 4L, 64);
        MemorySegment.copy(MemorySegment.ofArray(permutation), 0L, segment, 0L, permutation.length * 4L);
        VarHandle.fullFence();
        this.c2me$samplerData = segment;
        this.c2me$samplerDataPtr = segment.address();
    }

    /**
     * @author ishland
     * @reason replace impl
     */
    @Overwrite
    public double sample(DensityFunction.NoisePos pos) {
        if (this.c2me$samplerDataPtr != 0L) {
            double v = ((double) Bindings.c2me_natives_end_islands_sample(this.c2me$samplerDataPtr, pos.blockX() / 8, pos.blockZ() / 8) - 8.0) / 128.0;
            double vanilla = ((double)sample(this.sampler, pos.blockX() / 8, pos.blockZ() / 8) - 8.0) / 128.0;
//            if (v != vanilla) {
//                System.out.println(String.format("%f %f", v, vanilla));
//            }
            return v;
        } else {
            return ((double)sample(this.sampler, pos.blockX() / 8, pos.blockZ() / 8) - 8.0) / 128.0;
        }
    }

}
