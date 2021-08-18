package com.ishland.c2me.asm;

import com.ishland.c2me.common.config.C2MEConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.Optional;

public class ASMTransformerLithiumChunkAccessWorkaround {

    private static final String INTERMEDIARY = "intermediary";
    private static final MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();
    private static final String ServerChunkManager = mappingResolver.mapClassName(INTERMEDIARY, "net/minecraft/class_3215".replace('/', '.')).replace('.', '/');

    private ASMTransformerLithiumChunkAccessWorkaround() {
    }

    // private getChunkOffThread(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;
    // INVOKEVIRTUAL java/util/concurrent/CompletableFuture.join ()Ljava/lang/Object;
    static void transform(ClassNode classNode) {
        if (!C2MEConfig.vanillaWorldGenOptimizationsConfig.optimizeAsyncChunkRequest) return;
        try {
            if (classNode.name.equals(ServerChunkManager) && FabricLoader.getInstance().isModLoaded("lithium")) {
                for (MethodNode method : classNode.methods) {
                    if (method.name.equals("c2me$getChunkOffThread") && method.desc.equals(ASMTransformerMakeVolatile.remapMethodDescriptor("(IILnet/minecraft/class_2806;Z)Lnet/minecraft/class_2791;"))) {
                        ASMMixinPlugin.LOGGER.info("Replacing lithium chunk_access method getChunkOffThread to apply non-blocking async chunk request");
                        final Optional<MethodNode> getChunkOffThread = classNode.methods.stream().filter(methodNode -> methodNode.name.equals("getChunkOffThread")).findAny();
                        getChunkOffThread.ifPresentOrElse(oldMethodNode -> {
                            final MethodNode newMethodNode = new MethodNode();
                            method.accept(newMethodNode);
                            newMethodNode.name = oldMethodNode.name;
                            newMethodNode.access = method.access;
                            newMethodNode.desc = method.desc;
                            newMethodNode.signature = method.signature;
                            newMethodNode.exceptions = new ArrayList<>(method.exceptions);
                            if (method.attrs != null) newMethodNode.attrs = new ArrayList<>(method.attrs);
                            newMethodNode.tryCatchBlocks = new ArrayList<>(method.tryCatchBlocks);
                            classNode.methods.remove(oldMethodNode);
                            classNode.methods.add(newMethodNode);
                        }, () -> {
                            ASMMixinPlugin.LOGGER.warn("lithium getChunkOffThread not found");
                        });
                        break;
                    }
                }

            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
