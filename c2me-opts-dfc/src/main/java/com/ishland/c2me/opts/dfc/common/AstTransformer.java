package com.ishland.c2me.opts.dfc.common;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;

public interface AstTransformer {

    AstNode transform(AstNode astNode);

}
