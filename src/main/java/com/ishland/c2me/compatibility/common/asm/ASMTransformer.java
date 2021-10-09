package com.ishland.c2me.compatibility.common.asm;

import com.ishland.c2me.compatibility.common.ThreadLocalMutableBlockPos;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import java.util.ListIterator;
import java.util.function.Consumer;

public class ASMTransformer {

    private static final Logger LOGGER = LogManager.getLogger("C2ME Compatibility Module ASM Transformer");
    private static final MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();
    private static final String INTERMEDIARY = "intermediary";

    private static final String BlockPosMutableName = mappingResolver.mapClassName(INTERMEDIARY, "net/minecraft/class_2338$class_2339".replace('/', '.')).replace('.', '/');
    private static final String ChunkRandomName = mappingResolver.mapClassName(INTERMEDIARY, "net/minecraft/class_2919".replace('/', '.')).replace('.', '/');

    public static void transform(ClassNode classNode) {
        final Consumer<MethodNode> transformer = methodNode -> {
            LOGGER.info("Transforming L{};{}{}", classNode.name, methodNode.name, methodNode.desc);
            final ListIterator<AbstractInsnNode> iterator = methodNode.instructions.iterator();
            while (iterator.hasNext()) {
                final AbstractInsnNode insnNode = iterator.next();
                if (insnNode instanceof TypeInsnNode typeInsnNode) {
                    if (typeInsnNode.getOpcode() == Opcodes.NEW) {
                        if (BlockPosMutableName.equals(typeInsnNode.desc)) {
                            LOGGER.info("Replacing NEW {} with NEW {}", typeInsnNode.desc, Type.getInternalName(ThreadLocalMutableBlockPos.class));
                            iterator.set(new TypeInsnNode(Opcodes.NEW, Type.getInternalName(ThreadLocalMutableBlockPos.class)));
                        }
//                        else if (ChunkRandomName.equals(typeInsnNode.desc) || "java/util/Random".equals(typeInsnNode.desc)) {
//                            LOGGER.info("Replacing NEW {} with NEW {}", typeInsnNode.desc, Type.getInternalName(ThreadLocalChunkRandom.class));
//                            iterator.set(new TypeInsnNode(Opcodes.NEW, Type.getInternalName(ThreadLocalChunkRandom.class)));
//                        }
                    }
                } else if (insnNode instanceof MethodInsnNode methodInsnNode) {
                    if (methodInsnNode.getOpcode() == Opcodes.INVOKESPECIAL && methodInsnNode.name.equals("<init>")) {
                        if (BlockPosMutableName.equals(methodInsnNode.owner)) {
                            LOGGER.info("Replacing initializer call of {} with {}", methodInsnNode.owner, Type.getInternalName(ThreadLocalMutableBlockPos.class));
                            iterator.set(new MethodInsnNode(Opcodes.INVOKESPECIAL, Type.getInternalName(ThreadLocalMutableBlockPos.class), "<init>", methodInsnNode.desc));
                        }
//                        else if (ChunkRandomName.equals(methodInsnNode.owner) || "java/util/Random".equals(methodInsnNode.owner)) {
//                            LOGGER.info("Replacing initializer call of {} with {}", methodInsnNode.owner, Type.getInternalName(ThreadLocalChunkRandom.class));
//                            iterator.set(new MethodInsnNode(Opcodes.INVOKESPECIAL, Type.getInternalName(ThreadLocalChunkRandom.class), "<init>", methodInsnNode.desc));
//                        }
                    }
                }
            }
        };
        classNode.methods.stream()
                .filter(methodNode -> methodNode.name.equals("<clinit>") || methodNode.name.equals("<init>"))
                .forEach(transformer);
    }


}
