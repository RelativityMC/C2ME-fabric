package com.ishland.c2me.fixes.worldgen.threading_issues.common.debug;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.service.IClassBytecodeProvider;
import org.spongepowered.asm.service.MixinService;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class SMAPPool {

    private static final ConcurrentHashMap<String, Optional<String>> CACHE = new ConcurrentHashMap<>();

    public static void put(String className, ClassNode node) {
        if (className == null || node == null) {
            return;
        }
        className = className.replace('/', '.');
        CACHE.put(className, Optional.ofNullable(node.sourceDebug));
    }

    public static String getSourceDebugInfo(String className) {
        Optional<String> cached = CACHE.get(className);
        if (cached != null) {
            return cached.orElse(null);
        }

        try {
            IClassBytecodeProvider provider = MixinService.getService().getBytecodeProvider();
            ClassNode classNode = provider.getClassNode(className.replace('.', '/'));

            if (classNode != null) {
                put(className, classNode);
                return classNode.sourceDebug;
            }
        } catch (Exception e) {
            // ignore
        }

        CACHE.put(className, Optional.empty());
        return null;
    }

    private SMAPPool() {
    }

}
