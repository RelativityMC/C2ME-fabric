package com.ishland.c2me.opts.dfc.mixin;

import com.ishland.c2me.opts.dfc.common.vif.EachApplierVanillaInterface;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Arrays;

@Mixin(ChunkNoiseSampler.CacheOnce.class)
public class MixinChunkNoiseSamplerCacheOnce {

    @Shadow @Final private DensityFunction delegate;
    private double c2me$lastValue = Double.NaN;
    private int c2me$lastX = Integer.MIN_VALUE;
    private int c2me$lastY = Integer.MIN_VALUE;
    private int c2me$lastZ = Integer.MIN_VALUE;

    private int[] c2me$lastXa;
    private int[] c2me$lastYa;
    private int[] c2me$lastZa;
    private double[] c2me$lastValuea;

    @WrapMethod(method = "sample")
    private double wrapSample(DensityFunction.NoisePos pos, Operation<Double> original) {
        if (pos instanceof ChunkNoiseSampler) {
            return original.call(pos);
        }
        int blockX = pos.blockX();
        int blockY = pos.blockY();
        int blockZ = pos.blockZ();
        if (c2me$lastValuea != null) {
            for (int i = 0; i < this.c2me$lastValuea.length; i ++) {
                if (c2me$lastXa[i] == blockX && c2me$lastYa[i] == blockY && c2me$lastZa[i] == blockZ) {
                    return c2me$lastValuea[i];
                }
            }
        }
        if (!Double.isNaN(c2me$lastValue) && c2me$lastX == blockX && c2me$lastY == blockY && c2me$lastZ == blockZ) {
            return c2me$lastValue;
        }
        double sample = this.delegate.sample(pos);
        c2me$lastValue = sample;
        c2me$lastX = blockX;
        c2me$lastY = blockY;
        c2me$lastZ = blockZ;
        return sample;
    }

    @WrapMethod(method = "fill")
    private void wrapFill(double[] densities, DensityFunction.EachApplier applier, Operation<Void> original) {
        if (applier instanceof ChunkNoiseSampler) {
            original.call(densities, applier);
            return;
        }
        if (applier instanceof EachApplierVanillaInterface ap) {
            if (c2me$lastValuea != null && Arrays.equals(ap.getY(), c2me$lastYa) && Arrays.equals(ap.getX(), c2me$lastXa) && Arrays.equals(ap.getZ(), c2me$lastZa)) {
                System.arraycopy(c2me$lastValuea, 0, densities, 0, c2me$lastValuea.length);
            } else {
                this.delegate.fill(densities, applier);
                this.c2me$lastValuea = Arrays.copyOf(densities, c2me$lastValuea.length);
                this.c2me$lastXa = Arrays.copyOf(ap.getX(), c2me$lastXa.length);
                this.c2me$lastYa = Arrays.copyOf(ap.getY(), c2me$lastYa.length);
                this.c2me$lastZa = Arrays.copyOf(ap.getY(), c2me$lastZa.length);
            }
            return;
        }
        this.delegate.fill(densities, applier);
    }

}
