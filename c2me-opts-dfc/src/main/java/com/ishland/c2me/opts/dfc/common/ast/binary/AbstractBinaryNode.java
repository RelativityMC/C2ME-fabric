package com.ishland.c2me.opts.dfc.common.ast.binary;

import com.ishland.c2me.opts.dfc.common.AstTransformer;
import com.ishland.c2me.opts.dfc.common.ast.AstNode;

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
}
