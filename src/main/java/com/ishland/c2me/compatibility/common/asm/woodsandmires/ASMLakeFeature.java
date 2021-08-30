package com.ishland.c2me.compatibility.common.asm.woodsandmires;

import net.fabricmc.loader.api.FabricLoader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodNode;

public class ASMLakeFeature { // juuxel.woodsandmires.mixin.LakeFeatureMixin

    private static final String targetClass = FabricLoader.getInstance().getMappingResolver().mapClassName("intermediary", "net.minecraft.class_3085").replace('.', '/');

    public static void transform(ClassNode classNode) {
        if (!FabricLoader.getInstance().isModLoaded("woods_and_mires")) return;
        if (classNode.name.equals(targetClass)) {
            for (MethodNode method : classNode.methods) {
                if (method.name.endsWith("wam_onGenerate_return")) {
                    System.out.println("Transforming LakeFeature to prevent woods_and_mires to do NPE");
                    method.instructions = new InsnList();
                    method.instructions.add(new InsnNode(Opcodes.RETURN));
                }
            }
        }
    }

}
