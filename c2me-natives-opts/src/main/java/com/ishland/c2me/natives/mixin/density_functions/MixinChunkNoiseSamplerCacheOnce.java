package com.ishland.c2me.natives.mixin.density_functions;

import com.google.common.collect.ImmutableMap;
import com.ishland.c2me.base.mixin.access.IChunkNoiseSampler;
import com.ishland.c2me.natives.common.CompiledDensityFunctionArg;
import com.ishland.c2me.natives.common.CompiledDensityFunctionImpl;
import com.ishland.c2me.natives.common.DensityFunctionUtils;
import com.ishland.c2me.natives.common.NativeInterface;
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

@Mixin(ChunkNoiseSampler.CacheOnce.class)
public abstract class MixinChunkNoiseSamplerCacheOnce implements CompiledDensityFunctionImpl, DensityFunction {

    @Shadow @Final private DensityFunction delegate;
    @Shadow @Final private ChunkNoiseSampler field_36605;
    @Shadow @Nullable private double[] cache;
    @Shadow private long sampleUniqueIndex;
    @Shadow private double lastSamplingResult;
    @Shadow private long cacheOnceUniqueIndex;
    @Unique
    private long pointer = 0L;

    @Unique
    private String errorMessage = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
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
                System.err.println("Failed to compile density function: cache_once_initialized %s".formatted(this));
                System.err.println(DensityFunctionUtils.indent(this.errorMessage, false));
            }
            return;
        }

        this.pointer = NativeInterface.createDFICachingCacheOnceData(((CompiledDensityFunctionImpl) this.delegate).getDFIPointer());
    }

    @Override
    public double sample(NoisePos pos) {
        if (DensityFunctionUtils.isSafeForNative(pos) && this.pointer != 0) {
            return NativeInterface.dfiBindingsSingleOp(this.pointer, pos.blockX(), pos.blockY(), pos.blockZ());
        } else {
            // TODO [VanillaCopy]
            if (pos != field_36605) {
                return this.delegate.sample(pos);
            } else if (this.cache != null && this.cacheOnceUniqueIndex == ((IChunkNoiseSampler) field_36605).getCacheOnceUniqueIndex()) {
                return this.cache[((IChunkNoiseSampler) field_36605).getIndex()];
            } else if (this.sampleUniqueIndex == ((IChunkNoiseSampler) field_36605).getSampleUniqueIndex()) {
                return this.lastSamplingResult;
            } else {
                this.sampleUniqueIndex = ((IChunkNoiseSampler) field_36605).getSampleUniqueIndex();
                double d = this.delegate.sample(pos);
                this.lastSamplingResult = d;
                return d;
            }
        }
    }

    @Override
    public void applyEach(double[] densities, EachApplier applier) {
        if (applier instanceof CompiledDensityFunctionArg dfa && dfa.getDFAPointer() != 0 && DensityFunctionUtils.isSafeForNative(applier) && this.pointer != 0) {
            NativeInterface.dfiBindingsMultiOp(this.pointer, dfa.getDFAPointer(), densities);
        } else {
            this.delegate.applyEach(densities, applier);
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
