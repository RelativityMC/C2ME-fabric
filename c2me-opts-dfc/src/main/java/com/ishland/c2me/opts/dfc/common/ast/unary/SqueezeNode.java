package com.ishland.c2me.opts.dfc.common.ast.unary;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import net.minecraft.util.math.MathHelper;

public class SqueezeNode extends AbstractUnaryNode {

    public SqueezeNode(AstNode operand) {
        super(operand);
    }

    @Override
    protected AstNode newInstance(AstNode operand) {
        return new SqueezeNode(operand);
    }

    @Override
    public double evalSingle(int x, int y, int z, EvalType type) {
        double v = MathHelper.clamp(this.operand.evalSingle(x, y, z, type), -1.0, 1.0);
        return v / 2.0 - v * v * v / 24.0;
    }

    @Override
    public void evalMulti(double[] res, int[] x, int[] y, int[] z, EvalType type) {
        this.operand.evalMulti(res, x, y, z, type);
        for (int i = 0; i < res.length; i++) {
            double v = MathHelper.clamp(res[i], -1.0, 1.0);
            res[i] = v / 2.0 - v * v * v / 24.0;
        }
    }

}
