package com.ishland.c2me.natives.mixin.density_functions;

import com.google.common.collect.ImmutableMap;
import com.ishland.c2me.natives.common.CompiledDensityFunctionArg;
import com.ishland.c2me.natives.common.CompiledDensityFunctionImpl;
import com.ishland.c2me.natives.common.DensityFunctionUtils;
import com.ishland.c2me.natives.common.NativeInterface;
import com.ishland.c2me.natives.common.NativeMemoryTracker;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkNoiseSampler.Cache2D.class)
public abstract class MixinChunkNoiseSamplerCache2D implements CompiledDensityFunctionImpl, DensityFunction {

    @Shadow @Final private DensityFunction delegate;
    @Shadow private long lastSamplingColumnPos;
    @Shadow private double lastSamplingResult;
    @Unique
    private long pointer = 0L;

    @Unique
    private String errorMessage = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(DensityFunction delegate, CallbackInfo ci) {
        compileIfNeeded(false);
    }

    @Override
    public void compileIfNeeded(boolean includeParents) {
        if (this.pointer != 0L) return;

        if (includeParents) {
            DensityFunctionUtils.triggerCompilationIfNeeded(this.delegate);
        }

        if (!DensityFunctionUtils.isCompiled(this.delegate)) {
            if (DensityFunctionUtils.DEBUG) {
                this.errorMessage = DensityFunctionUtils.getErrorMessage(
                        this,
                        ImmutableMap.of("delegate", this.delegate)
                );
                assert this.errorMessage != null;
                System.err.println("Failed to compile density function: cache_2d_initialized %s".formatted(this));
                System.err.println(DensityFunctionUtils.indent(this.errorMessage, false));
            }
            return;
        }

        this.pointer = NativeInterface.createDFICachingCache2DData(((CompiledDensityFunctionImpl) this.delegate).getDFIPointer());

        NativeMemoryTracker.registerAllocatedMemory(
                this,
                NativeInterface.SIZEOF_density_function_data + NativeInterface.SIZEOF_dfi_caching_cache_2d_data,
                this.pointer
        );
    }

    @Override
    public double sample(DensityFunction.NoisePos pos) {
        if (DensityFunctionUtils.isSafeForNative(pos) && this.pointer != 0) {
            return NativeInterface.dfiBindingsSingleOp(this.pointer, pos.blockX(), pos.blockY(), pos.blockZ());
        } else {
            // TODO [VanillaCopy]
            int i = pos.blockX();
            int j = pos.blockZ();
            long l = ChunkPos.toLong(i, j);
            if (this.lastSamplingColumnPos == l) {
                return this.lastSamplingResult;
            } else {
                this.lastSamplingColumnPos = l;
                double d = this.delegate.sample(pos);
                this.lastSamplingResult = d;
                return d;
            }
        }
    }

    @Override
    public void fill(double[] densities, EachApplier applier) {
        if (applier instanceof CompiledDensityFunctionArg dfa && dfa.getDFAPointer() != 0 && DensityFunctionUtils.isSafeForNative(applier) && this.pointer != 0) {
            NativeInterface.dfiBindingsMultiOp(this.pointer, dfa.getDFAPointer(), densities);
        } else {
            this.delegate.fill(densities, applier);
        }
    }

    @Override
    public long getDFIPointer() {
        return this.pointer;
    }

    @Nullable
    @Override
    public String getCompilationFailedReason() {
        return this.errorMessage;
    }

}
