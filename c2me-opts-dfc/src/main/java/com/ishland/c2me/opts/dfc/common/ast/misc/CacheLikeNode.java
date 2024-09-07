package com.ishland.c2me.opts.dfc.common.ast.misc;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.AstTransformer;
import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import com.ishland.c2me.opts.dfc.common.ducks.IFastCacheLike;
import com.ishland.c2me.opts.dfc.common.gen.BytecodeGen;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

import java.util.Objects;

public class CacheLikeNode implements AstNode {

    private final IFastCacheLike cacheLike;
    private final AstNode delegate;

    public CacheLikeNode(IFastCacheLike cacheLike, AstNode delegate) {
        this.cacheLike = cacheLike;
        this.delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public double evalSingle(int x, int y, int z, EvalType type) {
        if (this.cacheLike == null) {
            return this.delegate.evalSingle(x, y, z, type);
        }
        double cached = this.cacheLike.c2me$getCached(x, y, z, type);
        if (Double.doubleToRawLongBits(cached) != IFastCacheLike.CACHE_MISS_NAN_BITS) {
            return cached;
        } else {
            double eval = this.delegate.evalSingle(x, y, z, type);
            this.cacheLike.c2me$cache(x, y, z, type, eval);
            return eval;
        }
    }

    @Override
    public void evalMulti(double[] res, int[] x, int[] y, int[] z, EvalType type) {
        if (this.cacheLike == null) {
            this.delegate.evalMulti(res, x, y, z, type);
            return;
        }
        boolean cached = this.cacheLike.c2me$getCached(res, x, y, z, type);
        if (!cached) {
            this.delegate.evalMulti(res, x, y, z, type);
            this.cacheLike.c2me$cache(res, x, y, z, type);
        }
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[]{this.delegate};
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        AstNode delegate = this.delegate.transform(transformer);
        if (this.delegate == delegate) {
            return transformer.transform(this);
        } else {
            return transformer.transform(new CacheLikeNode(this.cacheLike, delegate));
        }
    }

    @Override
    public void doBytecodeGenSingle(BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        String delegateMethod = context.newSingleMethod(this.delegate);
        String cacheLikeField = context.newField(IFastCacheLike.class, this.cacheLike);
        int eval = localVarConsumer.createLocalVariable("eval", Type.DOUBLE_TYPE.getDescriptor());

        Label cacheExists = new Label();
        Label cacheMiss = new Label();

        m.load(0, InstructionAdapter.OBJECT_TYPE);
        m.getfield(context.className, cacheLikeField, Type.getDescriptor(IFastCacheLike.class));
        m.ifnonnull(cacheExists);
        context.callDelegateSingle(m, delegateMethod);
        m.areturn(Type.DOUBLE_TYPE);

        m.visitLabel(cacheExists);
        m.load(0, InstructionAdapter.OBJECT_TYPE);
        m.getfield(context.className, cacheLikeField, Type.getDescriptor(IFastCacheLike.class));
        m.load(1, Type.INT_TYPE);
        m.load(2, Type.INT_TYPE);
        m.load(3, Type.INT_TYPE);
        m.load(4, InstructionAdapter.OBJECT_TYPE);
        m.invokeinterface(Type.getInternalName(IFastCacheLike.class), "c2me$getCached", Type.getMethodDescriptor(Type.DOUBLE_TYPE, Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE, Type.getType(EvalType.class)));
        m.dup2();
        m.invokestatic(Type.getInternalName(Double.class), "doubleToRawLongBits", Type.getMethodDescriptor(Type.LONG_TYPE, Type.DOUBLE_TYPE), false);
        m.lconst(IFastCacheLike.CACHE_MISS_NAN_BITS);
        m.lcmp();
        m.ifeq(cacheMiss); // operand1 == operand2, branched with cache res
        m.areturn(Type.DOUBLE_TYPE);

        m.visitLabel(cacheMiss);
        m.pop2();

        context.callDelegateSingle(m, delegateMethod);
        m.store(eval, Type.DOUBLE_TYPE);
        m.load(0, InstructionAdapter.OBJECT_TYPE);
        m.getfield(context.className, cacheLikeField, Type.getDescriptor(IFastCacheLike.class));
        m.load(1, Type.INT_TYPE);
        m.load(2, Type.INT_TYPE);
        m.load(3, Type.INT_TYPE);
        m.load(4, InstructionAdapter.OBJECT_TYPE);
        m.load(eval, Type.DOUBLE_TYPE);
        m.invokeinterface(Type.getInternalName(IFastCacheLike.class), "c2me$cache", Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE, Type.INT_TYPE, Type.INT_TYPE, Type.getType(EvalType.class), Type.DOUBLE_TYPE));

        m.load(eval, Type.DOUBLE_TYPE);
        m.areturn(Type.DOUBLE_TYPE);
    }

    @Override
    public void doBytecodeGenMulti(BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        String delegateMethod = context.newMultiMethod(this.delegate);
        String cacheLikeField = context.newField(IFastCacheLike.class, this.cacheLike);

        Label cacheExists = new Label();
        Label cacheMiss = new Label();

        m.load(0, InstructionAdapter.OBJECT_TYPE);
        m.getfield(context.className, cacheLikeField, Type.getDescriptor(IFastCacheLike.class));
        m.ifnonnull(cacheExists);
        context.callDelegateMulti(m, delegateMethod);
        m.areturn(Type.VOID_TYPE);

        m.visitLabel(cacheExists);
        m.load(0, InstructionAdapter.OBJECT_TYPE);
        m.getfield(context.className, cacheLikeField, Type.getDescriptor(IFastCacheLike.class));
        m.load(1, InstructionAdapter.OBJECT_TYPE);
        m.load(2, InstructionAdapter.OBJECT_TYPE);
        m.load(3, InstructionAdapter.OBJECT_TYPE);
        m.load(4, InstructionAdapter.OBJECT_TYPE);
        m.load(5, InstructionAdapter.OBJECT_TYPE);
        m.invokeinterface(Type.getInternalName(IFastCacheLike.class), "c2me$getCached", Type.getMethodDescriptor(Type.BOOLEAN_TYPE, Type.getType(double[].class), Type.getType(int[].class), Type.getType(int[].class), Type.getType(int[].class), Type.getType(EvalType.class)));
        m.ifeq(cacheMiss);
        m.areturn(Type.VOID_TYPE);

        m.visitLabel(cacheMiss);
        context.callDelegateMulti(m, delegateMethod);
        m.load(0, InstructionAdapter.OBJECT_TYPE);
        m.getfield(context.className, cacheLikeField, Type.getDescriptor(IFastCacheLike.class));
        m.load(1, InstructionAdapter.OBJECT_TYPE);
        m.load(2, InstructionAdapter.OBJECT_TYPE);
        m.load(3, InstructionAdapter.OBJECT_TYPE);
        m.load(4, InstructionAdapter.OBJECT_TYPE);
        m.load(5, InstructionAdapter.OBJECT_TYPE);
        m.invokeinterface(Type.getInternalName(IFastCacheLike.class), "c2me$cache", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(double[].class), Type.getType(int[].class), Type.getType(int[].class), Type.getType(int[].class), Type.getType(EvalType.class)));
        m.areturn(Type.VOID_TYPE);
    }

    public IFastCacheLike getCacheLike() {
        return cacheLike;
    }

    public AstNode getDelegate() {
        return delegate;
    }
}
