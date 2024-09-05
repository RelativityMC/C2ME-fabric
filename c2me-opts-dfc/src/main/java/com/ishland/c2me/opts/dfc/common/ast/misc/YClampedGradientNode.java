package com.ishland.c2me.opts.dfc.common.ast.misc;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.AstTransformer;
import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import com.ishland.c2me.opts.dfc.common.gen.BytecodeGen;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.math.MathHelper;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.InstructionAdapter;

public class YClampedGradientNode implements AstNode {

    private final double fromY;
    private final double toY;
    private final double fromValue;
    private final double toValue;

    public YClampedGradientNode(double fromY, double toY, double fromValue, double toValue) {
        this.fromY = fromY;
        this.toY = toY;
        this.fromValue = fromValue;
        this.toValue = toValue;
    }

    @Override
    public double evalSingle(int x, int y, int z, EvalType type) {
        return MathHelper.clampedMap(y, this.fromY, this.toY, this.fromValue, this.toValue);
    }

    @Override
    public void evalMulti(double[] res, int[] x, int[] y, int[] z, EvalType type) {
        for (int i = 0; i < res.length; i++) {
            res[i] = MathHelper.clampedMap(y[i], this.fromY, this.toY, this.fromValue, this.toValue);
        }
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[0];
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        return transformer.transform(this);
    }

    @Override
    public void doBytecodeGenSingle(BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        m.load(2, Type.INT_TYPE);
        m.cast(Type.INT_TYPE, Type.DOUBLE_TYPE);
        m.dconst(this.fromY);
        m.dconst(this.toY);
        m.dconst(this.fromValue);
        m.dconst(this.toValue);
        m.invokestatic(
                Type.getInternalName(MathHelper.class),
                FabricLoader.getInstance().getMappingResolver().mapMethodName("intermediary", "net.minecraft.class_3532", "method_32854", "(DDDDD)D"),
                "(DDDDD)D",
                false
        );
        m.areturn(Type.DOUBLE_TYPE);
    }

    @Override
    public void doBytecodeGenMulti(BytecodeGen.Context context, InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer) {
        context.doCountedLoop(m, localVarConsumer, idx -> {
            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(idx, Type.INT_TYPE);

            {
                m.load(3, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);
                m.aload(Type.INT_TYPE);
                m.cast(Type.INT_TYPE, Type.DOUBLE_TYPE);
                m.dconst(this.fromY);
                m.dconst(this.toY);
                m.dconst(this.fromValue);
                m.dconst(this.toValue);
                m.invokestatic(
                        Type.getInternalName(MathHelper.class),
                        FabricLoader.getInstance().getMappingResolver().mapMethodName("intermediary", "net.minecraft.class_3532", "method_32854", "(DDDDD)D"),
                        "(DDDDD)D",
                        false
                );
            }

            m.astore(Type.DOUBLE_TYPE);
        });

        m.areturn(Type.VOID_TYPE);
    }
}
