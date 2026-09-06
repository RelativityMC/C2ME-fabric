package com.ishland.c2me.opts.dfc.common.gen.spirv;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.gen.CodeGenRegistry;

public class SpirVGenRegistry {
    public static final CodeGenRegistry<SpirVEmitter<? extends AstNode>> REGISTRY = new CodeGenRegistry<>();
}
