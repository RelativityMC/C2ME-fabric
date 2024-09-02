package com.ishland.c2me.opts.dfc.common.ast.unary;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.EvalType;

public class NegMulNode extends AbstractUnaryNode {

    private final double negMul;

    public NegMulNode(AstNode operand, double negMul) {
        super(operand);
        this.negMul = negMul;
    }

    @Override
    protected AstNode newInstance(AstNode operand) {
        return new NegMulNode(operand, this.negMul);
    }

    @Override
    public double evalSingle(int x, int y, int z, EvalType type) {
        double v = this.operand.evalSingle(x, y, z, type);
        return v > 0.0 ? v : v * this.negMul;
    }

    @Override
    public void evalMulti(double[] res, int[] x, int[] y, int[] z, EvalType type) {
        this.operand.evalMulti(res, x, y, z, type);
        for (int i = 0; i < res.length; i++) {
            double v = res[i];
            res[i] = v > 0.0 ? v : v * this.negMul;
        }
    }
}
