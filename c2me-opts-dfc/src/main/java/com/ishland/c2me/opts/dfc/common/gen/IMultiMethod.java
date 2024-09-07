package com.ishland.c2me.opts.dfc.common.gen;

import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import com.ishland.c2me.opts.dfc.common.util.ArrayCache;

@FunctionalInterface
public interface IMultiMethod {

    void evalMulti(double[] res, int[] x, int[] y, int[] z, EvalType type, ArrayCache arrayCache);

}
