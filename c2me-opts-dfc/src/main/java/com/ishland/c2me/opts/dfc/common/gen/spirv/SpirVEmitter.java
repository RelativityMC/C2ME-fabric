package com.ishland.c2me.opts.dfc.common.gen.spirv;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.gen.CodeEmitter;

/**
 * Emits SPIR-V instructions for a single AST node into the current basic block.
 *
 * <p>Unlike the OpenCL C backend, which appends text and assigns into a named
 * variable, this returns the SPIR-V result {@code <id>} holding the node's value.
 * Nodes that need branching allocate a {@code Function}-storage {@code OpVariable}
 * via {@link SpirVGenFunctionContext#newLocal(int)}, store into it from each arm,
 * and return an {@code OpLoad} of it -- this avoids constructing {@code OpPhi} by
 * hand, and driver mem2reg removes the variable.</p>
 */
public interface SpirVEmitter<T extends AstNode> extends CodeEmitter<T> {

    /**
     * @return the SPIR-V result {@code <id>} holding this node's value.
     */
    int doSpirVGen(T node, SpirVGenFunctionContext context);

}
