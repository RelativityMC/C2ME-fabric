package com.ishland.c2me.asm;

import com.ishland.c2me.common.util.CFUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ListIterator;
import java.util.concurrent.CompletableFuture;

public class ASMTransformerLithiumChunkAccessWorkaround {

    private static final String INTERMEDIARY = "intermediary";
    private static final MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();
    private static final String ServerChunkManager = mappingResolver.mapClassName(INTERMEDIARY, "net/minecraft/class_3215".replace('/', '.')).replace('.', '/');

    private ASMTransformerLithiumChunkAccessWorkaround() {
    }

    // private getChunkOffThread(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;
    // INVOKEVIRTUAL java/util/concurrent/CompletableFuture.join ()Ljava/lang/Object;
    static void transform(ClassNode classNode) {
        try {
            if (classNode.name.equals(ServerChunkManager) && FabricLoader.getInstance().isModLoaded("lithium")) {
                for (MethodNode method : classNode.methods) {
                    if (method.name.equals("getChunkOffThread") && method.desc.equals(ASMTransformerMakeVolatile.remapDescriptor("(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;"))) {
                        ASMMixinPlugin.LOGGER.info("Patching lithium chunk_access method getChunkOffThread to workaround CompletableFuture weirdness on ForkJoinPool");
                        final ListIterator<AbstractInsnNode> iterator = method.instructions.iterator();
                        while (iterator.hasNext()) {
                            final AbstractInsnNode insnNode = iterator.next();
                            if (insnNode instanceof MethodInsnNode methodInsnNode) {
                                if (methodInsnNode.getOpcode() == Opcodes.INVOKEVIRTUAL &&
                                        methodInsnNode.owner.equals("java/util/concurrent/CompletableFuture") &&
                                        methodInsnNode.name.equals("join") &&
                                        methodInsnNode.desc.equals("()Ljava/lang/Object;")) {
                                    ASMMixinPlugin.LOGGER.info("Replacing CompletableFuture.join() with CFUtil.join(CompletableFuture)");
                                    iterator.set(new MethodInsnNode(Opcodes.INVOKESTATIC,
                                            Type.getInternalName(CFUtil.class),
                                            "join",
                                            Type.getMethodDescriptor(CFUtil.class.getMethod("join", CompletableFuture.class))));
                                }
                            }
                        }
                    }
                }

            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
