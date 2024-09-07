package com.ishland.c2me.opts.dfc.common.gen;

import com.ishland.c2me.opts.dfc.common.ast.EvalType;

@FunctionalInterface
public interface ISingleMethod {

    double evalSingle(int x, int y, int z, EvalType type);

}
