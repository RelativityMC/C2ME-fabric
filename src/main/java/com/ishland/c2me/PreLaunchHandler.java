package com.ishland.c2me;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.service.MixinService;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PreLaunchHandler implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        if (Boolean.getBoolean("com.ishland.c2me.mixin.doAudit")) {
            Logger auditLogger = LoggerFactory.getLogger("C2ME Mixin Audit");
            try {
                final Class<?> transformerClazz = Class.forName("org.spongepowered.asm.mixin.transformer.MixinTransformer");
                if (transformerClazz.isInstance(MixinEnvironment.getCurrentEnvironment().getActiveTransformer())) {
                    final Field processorField = transformerClazz.getDeclaredField("processor");
                    processorField.setAccessible(true);
                    final Object processor = processorField.get(MixinEnvironment.getCurrentEnvironment().getActiveTransformer());
                    final Class<?> processorClazz = Class.forName("org.spongepowered.asm.mixin.transformer.MixinProcessor");
                    final Field configsField = processorClazz.getDeclaredField("configs");
                    configsField.setAccessible(true);
                    final List<?> configs = (List<?>) configsField.get(processor);
                    final Class<?> configClazz = Class.forName("org.spongepowered.asm.mixin.transformer.MixinConfig");
                    final Method getUnhandledTargetsMethod = configClazz.getDeclaredMethod("getUnhandledTargets");
                    getUnhandledTargetsMethod.setAccessible(true);
                    Set<String> unhandled = new HashSet<>();
                    for (Object config : configs) {
                        final Set<String> unhandledTargets = (Set<String>) getUnhandledTargetsMethod.invoke(config);
                        unhandled.addAll(unhandledTargets);
                    }
                    for (String s : unhandled) {
                        auditLogger.info("Loading class {}", s);
                        MixinService.getService().getClassProvider().findClass(s, false);
                    }
                    for (Object config : configs) {
                        for (String unhandledTarget : (Set<String>) getUnhandledTargetsMethod.invoke(config)) {
                            auditLogger.error("{} is already classloaded", unhandledTarget);
                        }
                    }
                }
            } catch (Throwable t) {
                throw new RuntimeException("Failed to audit mixins", t);
            }
        }
    }
}
