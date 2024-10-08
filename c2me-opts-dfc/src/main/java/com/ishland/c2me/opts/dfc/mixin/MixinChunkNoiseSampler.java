package com.ishland.c2me.opts.dfc.mixin;

import com.ishland.c2me.opts.dfc.common.ducks.IArrayCacheCapable;
import com.ishland.c2me.opts.dfc.common.ducks.ICoordinatesFilling;
import com.ishland.c2me.opts.dfc.common.gen.DelegatingBlendingAwareVisitor;
import com.ishland.c2me.opts.dfc.common.util.ArrayCache;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkNoiseSampler.class)
public abstract class MixinChunkNoiseSampler implements IArrayCacheCapable, ICoordinatesFilling {

    @Shadow @Final private int verticalCellBlockCount;
    @Shadow @Final private int horizontalCellBlockCount;
    @Shadow private int startBlockY;
    @Shadow private int startBlockX;
    @Shadow private int startBlockZ;
    @Shadow private boolean isInInterpolationLoop;

    @Shadow public abstract Blender getBlender();

    private final ArrayCache c2me$arrayCache = new ArrayCache();

    @Override
    public ArrayCache c2me$getArrayCache() {
        return this.c2me$arrayCache != null ? this.c2me$arrayCache : new ArrayCache();
    }

    @Override
    public void c2me$fillCoordinates(int[] x, int[] y, int[] z) {
        int index = 0;
        for (int i = this.verticalCellBlockCount - 1; i >= 0; i--) {
            int blockY = this.startBlockY + i;
            for (int j = 0; j < this.horizontalCellBlockCount; j++) {
                int blockX = this.startBlockX + j;
                for (int k = 0; k < this.horizontalCellBlockCount; k++) {
                    int blockZ = this.startBlockZ + k;

                    x[index] = blockX;
                    y[index] = blockY;
                    z[index] = blockZ;

                    index++;
                }
            }
        }
    }

    @Inject(method = "getActualDensityFunctionImpl", at = @At("HEAD"))
    private void protectInterpolationLoop(DensityFunction function, CallbackInfoReturnable<DensityFunction> cir) {
        if (this.isInInterpolationLoop && function instanceof DensityFunctionTypes.Wrapping) {
            throw new IllegalStateException("Cannot create more wrapping during interpolation loop");
        }
    }

    @ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/noise/NoiseRouter;apply(Lnet/minecraft/world/gen/densityfunction/DensityFunction$DensityFunctionVisitor;)Lnet/minecraft/world/gen/noise/NoiseRouter;"))
    private DensityFunction.DensityFunctionVisitor modifyVisitor1(DensityFunction.DensityFunctionVisitor visitor) {
        return c2me$getDelegatingBlendingAwareVisitor(visitor);
    }

    @Unique
    private @NotNull DelegatingBlendingAwareVisitor c2me$getDelegatingBlendingAwareVisitor(DensityFunction.DensityFunctionVisitor visitor) {
        return new DelegatingBlendingAwareVisitor(visitor, this.getBlender() != Blender.getNoBlending());
    }

    @ModifyArg(method = {"<init>", "createMultiNoiseSampler"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/densityfunction/DensityFunction;apply(Lnet/minecraft/world/gen/densityfunction/DensityFunction$DensityFunctionVisitor;)Lnet/minecraft/world/gen/densityfunction/DensityFunction;"), require = 7, expect = 7)
    private DensityFunction.DensityFunctionVisitor modifyVisitor2(DensityFunction.DensityFunctionVisitor visitor) {
        return c2me$getDelegatingBlendingAwareVisitor(visitor);
    }

}
