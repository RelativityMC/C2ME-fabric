package com.ishland.c2me.opts.dfc.common.gen;

import com.google.common.base.Suppliers;
import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import com.ishland.c2me.opts.dfc.common.ducks.IArrayCacheCapable;
import com.ishland.c2me.opts.dfc.common.util.ArrayCache;
import com.ishland.c2me.opts.dfc.common.vif.EachApplierVanillaInterface;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Supplier;

public class CompiledDensityFunction implements DensityFunction {

    private final CompiledEntry compiledEntry;
    private final Supplier<DensityFunction> blendingFallback;

    public CompiledDensityFunction(CompiledEntry compiledEntry, DensityFunction blendingFallback) {
        this.compiledEntry = Objects.requireNonNull(compiledEntry);
        this.blendingFallback = blendingFallback != null ? () -> blendingFallback : null;
    }

    private CompiledDensityFunction(CompiledEntry compiledEntry, Supplier<DensityFunction> blendingFallback) {
        this.compiledEntry = Objects.requireNonNull(compiledEntry);
        this.blendingFallback = blendingFallback;
    }

    @Override
    public double sample(NoisePos pos) {
        if (pos.getBlender() != Blender.getNoBlending()) {
            DensityFunction fallback = this.getFallback();
            if (fallback == null) {
                throw new IllegalStateException("blendingFallback is no more");
            }
            return fallback.sample(pos);
        } else {
            ArrayCache cache = pos instanceof IArrayCacheCapable cacheCapable ? cacheCapable.c2me$getArrayCache() : new ArrayCache();
            return this.compiledEntry.evalSingle(pos.blockX(), pos.blockY(), pos.blockZ(), EvalType.from(pos), cache);
        }
    }

    @Override
    public void fill(double[] densities, EachApplier applier) {
        if (applier instanceof ChunkNoiseSampler sampler) {
            if (sampler.getBlender() != Blender.getNoBlending()) {
                DensityFunction fallback = this.getFallback();
                if (fallback == null) {
                    throw new IllegalStateException("blendingFallback is no more");
                }
                fallback.fill(densities, applier);
                return;
            }
        }
        if (applier instanceof EachApplierVanillaInterface vanillaInterface) {
            this.compiledEntry.evalMulti(densities, vanillaInterface.getX(), vanillaInterface.getY(), vanillaInterface.getZ(), EvalType.from(applier), vanillaInterface.c2me$getArrayCache());
            return;
        }

        ArrayCache cache = applier instanceof IArrayCacheCapable cacheCapable ? cacheCapable.c2me$getArrayCache() : new ArrayCache();
        int[] x = cache.getIntArray(densities.length, false);
        int[] y = cache.getIntArray(densities.length, false);
        int[] z = cache.getIntArray(densities.length, false);
        for (int i = 0; i < densities.length; i ++) {
            NoisePos pos = applier.at(i);
            x[i] = pos.blockX();
            y[i] = pos.blockY();
            z[i] = pos.blockZ();
        }
        this.compiledEntry.evalMulti(densities, x, y, z, EvalType.from(applier), cache);
    }

    @Override
    public DensityFunction apply(DensityFunctionVisitor visitor) {
        boolean modified = false;
        List<Object> args = this.compiledEntry.getArgs();
        ListIterator<Object> iterator = args.listIterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (next instanceof DensityFunction df) {
                DensityFunction applied = df.apply(visitor);
                if (df != applied) {
                    iterator.set(applied);
                    modified = true;
                }
            }
            if (next instanceof Noise noise) {
                Noise applied = visitor.apply(noise);
                if (noise != applied) {
                    iterator.set(applied);
                    modified = true;
                }
            }
        }
        Supplier<DensityFunction> fallback = this.blendingFallback != null ? Suppliers.memoize(() -> {
            DensityFunction densityFunction = this.blendingFallback.get();
            return densityFunction != null ? densityFunction.apply(visitor) : null;
        }) : null;
        if (fallback != this.blendingFallback) {
            modified = true;
        }
        if (modified) {
            return new CompiledDensityFunction(this.compiledEntry.newInstance(args), fallback);
        } else {
            return this;
        }
    }

    @Override
    public double minValue() {
//        DensityFunction fallback = this.getFallback();
//        return fallback != null ? fallback.minValue() : Double.MIN_VALUE;
        return Double.MIN_VALUE;
    }

    @Override
    public double maxValue() {
//        DensityFunction fallback = this.getFallback();
//        return fallback != null ? fallback.maxValue() : Double.MAX_VALUE;
        return Double.MAX_VALUE;
    }

    @Override
    public CodecHolder<? extends DensityFunction> getCodecHolder() {
        throw new UnsupportedOperationException();
    }

    private DensityFunction getFallback() {
        return this.blendingFallback != null ? this.blendingFallback.get() : null;
    }
}
