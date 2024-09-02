package com.ishland.c2me.opts.dfc.common.ast.unary;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.EvalType;

public class CubeNode extends AbstractUnaryNode {

    public CubeNode(AstNode operand) {
        super(operand);
    }

    @Override
    protected AstNode newInstance(AstNode operand) {
        return new CubeNode(operand);
    }

    @Override
    public double evalSingle(int x, int y, int z, EvalType type) {
        double v = this.operand.evalSingle(x, y, z, type);
        return v * v * v;
    }

    @Override
    public void evalMulti(double[] res, int[] x, int[] y, int[] z, EvalType type) {
        this.operand.evalMulti(res, x, y, z, type);
        for (int i = 0; i < res.length; i++) {
            res[i] = res[i] * res[i] * res[i];
        }
    }

}
