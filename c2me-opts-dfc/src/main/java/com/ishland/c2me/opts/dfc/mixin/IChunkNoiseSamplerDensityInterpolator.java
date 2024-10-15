package com.ishland.c2me.opts.dfc.mixin;

import com.ishland.c2me.base.mixin.access.IChunkNoiseSampler;
import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import com.ishland.c2me.opts.dfc.common.vif.NoisePosVanillaInterface;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkNoiseSampler.DensityInterpolator.class)
public class IChunkNoiseSamplerDensityInterpolator {

    @Shadow @Final private ChunkNoiseSampler field_34622;

    @Shadow private double x0y0z0;

    @Shadow private double x1y0z0;

    @Shadow private double x0y1z0;

    @Shadow private double x1y1z0;

    @Shadow private double x0y0z1;

    @Shadow private double x1y0z1;

    @Shadow private double x0y1z1;

    @Shadow private double x1y1z1;

    @Shadow private double result;

    @WrapMethod(method = "sample")
    private double wrapSample(DensityFunction.NoisePos pos, Operation<Double> original) {
        if (pos instanceof ChunkNoiseSampler) {
            return original.call(pos);
        }
        if (pos instanceof NoisePosVanillaInterface vif && vif.getType() == EvalType.INTERPOLATION) {
            boolean isInInterpolationLoop = ((IChunkNoiseSampler) this.field_34622).getIsInInterpolationLoop();
            boolean isSamplingForCaches = ((IChunkNoiseSampler) this.field_34622).getIsSamplingForCaches();
            if (!isInInterpolationLoop) {
                return original.call(pos);
            }
            int startBlockX = ((IChunkNoiseSampler) this.field_34622).getStartBlockX();
            int startBlockY = ((IChunkNoiseSampler) this.field_34622).getStartBlockY();
            int startBlockZ = ((IChunkNoiseSampler) this.field_34622).getStartBlockZ();
            int horizontalCellBlockCount = ((IChunkNoiseSampler) this.field_34622).getHorizontalCellBlockCount();
            int verticalCellBlockCount = ((IChunkNoiseSampler) this.field_34622).getVerticalCellBlockCount();
            int cellBlockX = pos.blockX() - startBlockX;
            int cellBlockY = pos.blockY() - startBlockY;
            int cellBlockZ = pos.blockZ() - startBlockZ;
            return isSamplingForCaches
                    ? MathHelper.lerp3(
                    (double)cellBlockX / (double)horizontalCellBlockCount,
                    (double)cellBlockY / (double)verticalCellBlockCount,
                    (double)cellBlockZ / (double)horizontalCellBlockCount,
                    this.x0y0z0,
                    this.x1y0z0,
                    this.x0y1z0,
                    this.x1y1z0,
                    this.x0y0z1,
                    this.x1y0z1,
                    this.x0y1z1,
                    this.x1y1z1
            ) : this.result;
        }
        return original.call(pos);
    }

}
