package com.ishland.c2me.natives.mixin.density_functions;

import com.google.common.collect.ImmutableMap;
import com.ishland.c2me.base.mixin.access.IChunkNoiseSampler;
import com.ishland.c2me.natives.common.CompiledDensityFunctionArg;
import com.ishland.c2me.natives.common.CompiledDensityFunctionImpl;
import com.ishland.c2me.natives.common.DensityFunctionUtils;
import com.ishland.c2me.natives.common.NativeInterface;
import com.ishland.c2me.natives.common.NativeMemoryTracker;
import com.ishland.c2me.natives.common.UnsafeUtil;
import net.minecraft.world.biome.source.BiomeCoords;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkNoiseSampler.FlatCacheDensityFunction.class)
public abstract class MixinChunkNoiseSamplerFlatCacheDensityFunction implements CompiledDensityFunctionImpl, DensityFunction {

    @Shadow
    @Final
    private DensityFunction delegate;
    @Mutable
    @Shadow
    @Final
    private double[][] cache;
    @Shadow
    @Final
    private ChunkNoiseSampler field_36611;
    @Unique
    private long pointer = 0L;

    @Unique
    private String errorMessage = null;

    @Unique
    private boolean isInitDelayed = false;

    @Unique
    private int length = -1;

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/densityfunction/DensityFunction;sample(Lnet/minecraft/world/gen/densityfunction/DensityFunction$NoisePos;)D"))
    private double preventSample(DensityFunction instance, NoisePos noisePos) {
        return 0;
    }

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void onInit(ChunkNoiseSampler chunkNoiseSampler, DensityFunction delegate, boolean sample, CallbackInfo ci) {
        if (!DensityFunctionUtils.isCompiled(delegate)) {
            if (DensityFunctionUtils.DEBUG) {
                this.errorMessage = DensityFunctionUtils.getErrorMessage(
                        this,
                        ImmutableMap.of("delegate", delegate)
                );
                assert this.errorMessage != null;
                System.err.println("Failed to compile density function: flat_cache_initialized %s".formatted(this));
                System.err.println(DensityFunctionUtils.indent(this.errorMessage, false));
            }
            return;
        }

        this.length = ((IChunkNoiseSampler) this.field_36611).getBiomeHorizontalEnd() + 1;

        if (sample) {
            this.cache = null; // release unused cache
            runCompilation();
        } else {
            this.isInitDelayed = true;
        }
    }

    @Unique
    private void runCompilation() {
        this.isInitDelayed = false;
        long ptr_cacheFlattened = 0L; // set to null to let cache initialization happen in native
        if (this.cache != null) {
            // provided cache, copy contents to native
            ptr_cacheFlattened = NativeMemoryTracker.allocateMemory(this, this.length * this.length * 8L);
            final double[][] cache1 = this.cache;
            long counter = 0;
            for (int offsetX = 0; offsetX < this.length; offsetX++) {
                for (int offsetZ = 0; offsetZ < this.length; offsetZ++) {
                    UnsafeUtil.getInstance().putDouble(ptr_cacheFlattened + ((counter++) * 8L), cache1[offsetX][offsetZ]);
                }
            }
        }

        // we assume the delegate is compiled here since the caller need to check isInitDelayed before doing this
        this.pointer = NativeInterface.createDFICachingFloatCacheData(
                ((CompiledDensityFunctionImpl) this.delegate).getDFIPointer(),
                this.length,
                ((IChunkNoiseSampler) this.field_36611).getBaseX(),
                ((IChunkNoiseSampler) this.field_36611).getBaseZ(),
                ptr_cacheFlattened
        );
        NativeMemoryTracker.registerAllocatedMemory(
                this,
                NativeInterface.SIZEOF_density_function_data +
                        NativeInterface.SIZEOF_dfi_caching_flat_cache_data +
                        (ptr_cacheFlattened == 0L ? this.length * this.length * 8L : 0L),
                this.pointer
        );
    }

    /**
     * @author ishland
     * @reason use native method
     */
    @Overwrite
    public double sample(DensityFunction.NoisePos pos) {
        if (this.isInitDelayed) runCompilation();
        if (this.pointer != 0L) {
            return NativeInterface.dfiBindingsSingleOp(this.pointer, pos.blockX(), pos.blockY(), pos.blockZ());
        } else {
            // TODO [VanillaCopy]
            int i = BiomeCoords.fromBlock(pos.blockX());
            int j = BiomeCoords.fromBlock(pos.blockZ());
            int k = i - ((IChunkNoiseSampler) this.field_36611).getBaseX();
            int l = j - ((IChunkNoiseSampler) this.field_36611).getBaseZ();
            int m = this.cache.length;
            return k >= 0 && l >= 0 && k < m && l < m ? this.cache[k][l] : this.delegate.sample(pos);
        }
    }

    /**
     * @author ishland
     * @reason use native method
     */
    @Overwrite
    public void applyEach(double[] ds, DensityFunction.EachApplier arg) {
        if (this.isInitDelayed) runCompilation();
        if (arg instanceof CompiledDensityFunctionArg dfa && dfa.getDFAPointer() != 0 && DensityFunctionUtils.isSafeForNative(arg) && this.pointer != 0) {
            NativeInterface.dfiBindingsMultiOp(this.pointer, dfa.getDFAPointer(), ds);
        } else {
            arg.applyEach(ds, this);
        }
    }


    @Override
    public long getDFIPointer() {
        if (this.isInitDelayed) runCompilation();
        return this.pointer;
    }

    @Nullable
    @Override
    public String getCompilationFailedReason() {
        return this.errorMessage;
    }

}
