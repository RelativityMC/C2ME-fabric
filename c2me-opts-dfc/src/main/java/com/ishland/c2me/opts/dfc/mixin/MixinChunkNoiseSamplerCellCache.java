package com.ishland.c2me.opts.dfc.mixin;

import com.ishland.c2me.base.mixin.access.IChunkNoiseSampler;
import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import com.ishland.c2me.opts.dfc.common.vif.NoisePosVanillaInterface;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkNoiseSampler.CellCache.class)
public class MixinChunkNoiseSamplerCellCache {

    @Shadow @Final private ChunkNoiseSampler field_36602;

    @Shadow @Final private double[] cache;

    @Shadow @Final private DensityFunction delegate;

    @WrapMethod(method = "sample")
    private double wrapSample(DensityFunction.NoisePos pos, Operation<Double> original) {
        if (pos instanceof ChunkNoiseSampler) {
            return original.call(pos);
        }
        if (pos instanceof NoisePosVanillaInterface vif && vif.getType() == EvalType.INTERPOLATION) {
            boolean isInInterpolationLoop = ((IChunkNoiseSampler) this.field_36602).getIsInInterpolationLoop();
            if (!isInInterpolationLoop) {
                return original.call(pos);
            }
            int startBlockX = ((IChunkNoiseSampler) this.field_36602).getStartBlockX();
            int startBlockY = ((IChunkNoiseSampler) this.field_36602).getStartBlockY();
            int startBlockZ = ((IChunkNoiseSampler) this.field_36602).getStartBlockZ();
            int horizontalCellBlockCount = ((IChunkNoiseSampler) this.field_36602).getHorizontalCellBlockCount();
            int verticalCellBlockCount = ((IChunkNoiseSampler) this.field_36602).getVerticalCellBlockCount();
            int cellBlockX = pos.blockX() - startBlockX;
            int cellBlockY = pos.blockY() - startBlockY;
            int cellBlockZ = pos.blockZ() - startBlockZ;
            return cellBlockX >= 0
                    && cellBlockY >= 0
                    && cellBlockZ >= 0
                    && cellBlockX < horizontalCellBlockCount
                    && cellBlockY < verticalCellBlockCount
                    && cellBlockZ < horizontalCellBlockCount
                    ? this.cache[((verticalCellBlockCount - 1 - cellBlockY) * horizontalCellBlockCount + cellBlockX)
                    * horizontalCellBlockCount
                    + cellBlockZ]
                    : this.delegate.sample(pos);
        }
        return original.call(pos);
    }

}
