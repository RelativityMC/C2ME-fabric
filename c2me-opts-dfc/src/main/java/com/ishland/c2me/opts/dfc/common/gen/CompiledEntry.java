package com.ishland.c2me.opts.dfc.common.gen;

import com.ishland.c2me.opts.dfc.common.ast.EvalType;

import java.util.List;

public interface CompiledEntry {

    double evalSingle(int x, int y, int z, EvalType type);

    void evalMulti(double[] res, int[] x, int[] y, int[] z, EvalType type);

    CompiledEntry newInstance(List<?> args);

    List<Object> getArgs();

}
