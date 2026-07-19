package com.ishland.c2me.opts.dfc.common.gen.jvm.internalapi;

public interface ArgumentVisitor {

    public static final ArgumentVisitor IDENTITY = o -> o;

    Object apply(Object operand);

}
