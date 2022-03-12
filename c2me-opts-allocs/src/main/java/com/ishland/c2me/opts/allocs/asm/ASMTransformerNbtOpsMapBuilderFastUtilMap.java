package com.ishland.c2me.opts.allocs.asm;

import com.ishland.c2me.base.common.util.ASMUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ListIterator;
import java.util.Map;

public class ASMTransformerNbtOpsMapBuilderFastUtilMap {

    static final Logger LOGGER = LoggerFactory.getLogger("C2ME (c2me-opts-allocs) ASM Transformer");

    private static final String NbtOps$MapBuilder = "net/minecraft/class_2509$class_5320";
    private static final String NbtOps$MapBuilderMapped = ASMUtils.mappingResolver.mapClassName(ASMUtils.INTERMEDIARY, NbtOps$MapBuilder.replace('/', '.')).replace('.', '/');
    private static final String buildDesc = "(Lnet/minecraft/class_2487;Lnet/minecraft/class_2520;)Lcom/mojang/serialization/DataResult;";
    private static final String buildDescMapped = ASMUtils.remapMethodDescriptor(buildDesc);
    private static final String build = ASMUtils.mappingResolver.mapMethodName(ASMUtils.INTERMEDIARY, NbtOps$MapBuilder.replace('/', '.'), "method_29170", buildDesc);

    //    NEW net/minecraft/nbt/NbtCompound
    //    DUP
    //    ALOAD 2
    //    CHECKCAST net/minecraft/nbt/NbtCompound
    //    INVOKEVIRTUAL net/minecraft/nbt/NbtCompound.toMap ()Ljava/util/Map;
    //    INVOKESTATIC com/google/common/collect/Maps.newHashMap (Ljava/util/Map;)Ljava/util/HashMap;   <---
    //    INVOKESPECIAL net/minecraft/nbt/NbtCompound.<init> (Ljava/util/Map;)V
    //    ASTORE 3

    public static void transform(ClassNode classNode) {
        try {
            if (classNode.name.equals(NbtOps$MapBuilderMapped)) {
                for (MethodNode method : classNode.methods) {
                    if (method.name.equals(build) && method.desc.equals(buildDescMapped)) {
                        LOGGER.debug("Replacing NbtOps$MapBuilder build method newHashMap to fastutil map");
                        final ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
                        boolean patched = false;
                        while (iterator.hasNext()) {
                            final AbstractInsnNode next = iterator.next();
                            if (next instanceof MethodInsnNode methodInsnNode) {
                                if (methodInsnNode.getOpcode() == Opcodes.INVOKESTATIC &&
                                        methodInsnNode.owner.equals("com/google/common/collect/Maps") &&
                                        methodInsnNode.name.equals("newHashMap") &&
                                        methodInsnNode.desc.equals("(Ljava/util/Map;)Ljava/util/HashMap;")) {
                                    iterator.set(new MethodInsnNode(
                                            Opcodes.INVOKESTATIC,
                                            Type.getInternalName(ASMTransformerNbtOpsMapBuilderFastUtilMap.class),
                                            "newFastUtilMap",
                                            Type.getMethodDescriptor(ASMTransformerNbtOpsMapBuilderFastUtilMap.class.getMethod("newFastUtilMap", Map.class))
                                    ));
                                    patched = true;
                                }
                            }
                        }
                        if (!patched) LOGGER.warn("Unable to find target opcode in NbtOps$MapBuilder");
                    }
                }
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static <K, V> Map<K, V> newFastUtilMap(Map<K, V> map) {
        return new Object2ObjectOpenHashMap<>(map);
    }

}
