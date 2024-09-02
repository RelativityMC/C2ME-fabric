package com.ishland.c2me.opts.dfc.common.ast;

import com.ishland.c2me.opts.dfc.common.AstTransformer;

public interface AstNode {

    double evalSingle(int x, int y, int z, EvalType type);

    void evalMulti(double[] res, int[] x, int[] y, int[] z, EvalType type);

    AstNode[] getChildren();

    AstNode transform(AstTransformer transformer);

}
