package com.ishland.c2me.opts.dfc.common.ast.misc;

import com.ishland.c2me.opts.dfc.common.AstTransformer;
import com.ishland.c2me.opts.dfc.common.vif.EachApplierVanillaInterface;
import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import com.ishland.c2me.opts.dfc.common.vif.NoisePosVanillaInterface;
import net.minecraft.world.gen.densityfunction.DensityFunction;

import java.util.Objects;

public class DelegateNode implements AstNode {

    private final DensityFunction densityFunction;

    public DelegateNode(DensityFunction densityFunction) {
        this.densityFunction = Objects.requireNonNull(densityFunction);
    }

    @Override
    public double evalSingle(int x, int y, int z, EvalType type) {
        return densityFunction.sample(new NoisePosVanillaInterface(x, y, z, type));
    }

    @Override
    public void evalMulti(double[] res, int[] x, int[] y, int[] z, EvalType type) {
        densityFunction.fill(res, new EachApplierVanillaInterface(x, y, z, type));
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[0];
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        return transformer.transform(this);
    }

    public DensityFunction getDelegate() {
        return this.densityFunction;
    }
}
