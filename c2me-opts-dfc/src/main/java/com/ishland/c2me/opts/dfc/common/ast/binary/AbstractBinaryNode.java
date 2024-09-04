package com.ishland.c2me.opts.dfc.common.ast.binary;

import com.ishland.c2me.opts.dfc.common.ast.AstTransformer;
import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.gen.BytecodeGen;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.Objects;

public abstract class AbstractBinaryNode implements AstNode {

    protected final AstNode left;
    protected final AstNode right;

    public AbstractBinaryNode(AstNode left, AstNode right) {
        this.left = Objects.requireNonNull(left);
        this.right = Objects.requireNonNull(right);
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[]{left, right};
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractBinaryNode that = (AbstractBinaryNode) o;
        return Objects.equals(left, that.left) && Objects.equals(right, that.right);
    }

    @Override
    public int hashCode() {
        int result = 1;

        Object o = this.getClass();
        result = 31 * result + o.hashCode();
        result = 31 * result + left.hashCode();
        result = 31 * result + right.hashCode();

        return result;
    }

    protected abstract AstNode newInstance(AstNode left, AstNode right);

    @Override
    public AstNode transform(AstTransformer transformer) {
        AstNode left = this.left.transform(transformer);
        AstNode right = this.right.transform(transformer);
        if (left == this.left && right == this.right) {
            return transformer.transform(this);
        } else {
            return transformer.transform(newInstance(left, right));
        }
    }

    @Override
    public void doBytecodeGenSingle(BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        String leftMethod = context.newSingleMethod(this.left);
        String rightMethod = context.newSingleMethod(this.right);

        context.callDelegateSingle(m, leftMethod);
        context.callDelegateSingle(m, rightMethod);
    }

    @Override
    public void doBytecodeGenMulti(BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        String leftMethod = context.newMultiMethod(this.left);
        String rightMethod = context.newMultiMethod(this.right);

        int res1 = localVarConsumer.createLocalVariable("res1", Type.getDescriptor(double[].class));

        m.load(1, InstructionAdapter.OBJECT_TYPE);
        m.arraylength();
        m.newarray(Type.DOUBLE_TYPE);
        m.store(res1, InstructionAdapter.OBJECT_TYPE);
        context.callDelegateMulti(m, leftMethod);
        m.load(0, InstructionAdapter.OBJECT_TYPE);
        m.load(res1, InstructionAdapter.OBJECT_TYPE);
        m.load(2, InstructionAdapter.OBJECT_TYPE);
        m.load(3, InstructionAdapter.OBJECT_TYPE);
        m.load(4, InstructionAdapter.OBJECT_TYPE);
        m.load(5, InstructionAdapter.OBJECT_TYPE);
        m.invokevirtual(context.className, rightMethod, BytecodeGen.Context.MULTI_DESC, false);

        context.doCountedLoop(m, localVarConsumer, idx -> bytecodeGenMultiBody(m, idx, res1));

        m.areturn(Type.VOID_TYPE);
    }

    protected abstract void bytecodeGenMultiBody(InstructionAdapter m, int idx, int res1);
}
