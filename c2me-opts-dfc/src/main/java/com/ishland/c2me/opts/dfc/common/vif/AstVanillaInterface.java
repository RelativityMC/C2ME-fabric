package com.ishland.c2me.opts.dfc.common.vif;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import com.ishland.c2me.opts.dfc.common.ast.misc.DelegateNode;
import net.minecraft.util.dynamic.CodecHolder;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.Objects;

public class AstVanillaInterface implements DensityFunction {

    private final AstNode astNode;
    private final DensityFunction blendingFallback;

    public AstVanillaInterface(AstNode astNode, DensityFunction blendingFallback) {
        this.astNode = Objects.requireNonNull(astNode);
        this.blendingFallback = blendingFallback;
    }

    @Override
    public double sample(NoisePos pos) {
        if (pos.getBlender() != Blender.getNoBlending()) {
            if (this.blendingFallback == null) {
                throw new IllegalStateException("blendingFallback is no more");
            }
            return this.blendingFallback.sample(pos);
        } else {
            return this.astNode.evalSingle(pos.blockX(), pos.blockY(), pos.blockZ(), EvalType.from(pos));
        }
    }

    @Override
    public void fill(double[] densities, EachApplier applier) {
        if (applier instanceof ChunkNoiseSampler sampler) {
            if (sampler.getBlender() != Blender.getNoBlending()) {
                if (this.blendingFallback == null) {
                    throw new IllegalStateException("blendingFallback is no more");
                }
                this.blendingFallback.fill(densities, applier);
                return;
            }
        }
        if (applier instanceof EachApplierVanillaInterface vanillaInterface) {
            this.astNode.evalMulti(densities, vanillaInterface.getX(), vanillaInterface.getY(), vanillaInterface.getZ(), EvalType.from(applier));
            return;
        }

        int[] x = new int[densities.length];
        int[] y = new int[densities.length];
        int[] z = new int[densities.length];
        for (int i = 0; i < densities.length; i ++) {
            NoisePos pos = applier.at(i);
            x[i] = pos.blockX();
            y[i] = pos.blockY();
            z[i] = pos.blockZ();
        }
        this.astNode.evalMulti(densities, x, y, z, EvalType.from(applier));
    }

    @Override
    public DensityFunction apply(DensityFunctionVisitor visitor) {
        return new AstVanillaInterface(
                this.astNode.transform(astNode -> {
                    if (astNode instanceof DelegateNode delegateNode) {
                        return new DelegateNode(delegateNode.getDelegate().apply(visitor));
                    }
                    return astNode;
                }),
                this.blendingFallback != null ? this.blendingFallback.apply(visitor) : null
        );
    }

    @Override
    public double minValue() {
        return this.blendingFallback.minValue();
    }

    @Override
    public double maxValue() {
        return this.blendingFallback.maxValue();
    }

    @Override
    public CodecHolder<? extends DensityFunction> getCodecHolder() {
        throw new UnsupportedOperationException();
    }

}
