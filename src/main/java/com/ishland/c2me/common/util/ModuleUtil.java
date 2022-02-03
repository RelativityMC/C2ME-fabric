package com.ishland.c2me.common.util;

import com.google.common.base.Preconditions;
import com.ishland.c2me.C2MEMod;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModuleUtil {

    public static final Logger LOGGER = LogManager.getLogger("C2ME ModuleManager");

    public static final boolean isModuleLoaded;

    static {
        boolean isLoaded = false;
        try {
            List<Path> jars = new ArrayList<>();
            try {
                final Path path = Path.of(com.ishland.c2me.libs.Locator.class.getProtectionDomain().getCodeSource().getLocation().getPath());
                if (!path.getParent().toString().equals("/")) {
                    jars.add(path);
                }
            } catch (Throwable ignored) {
            }
            FabricLoader.getInstance().getModContainer("c2me").ifPresent(modContainer -> {
                jars.add(modContainer.getRootPath().resolve("META-INF").resolve("jars"));
            });
            Preconditions.checkState(!jars.isEmpty());
            LOGGER.info("Searching for modules in {} locations...", jars.size());

            final ModuleFinder moduleFinder = ModuleFinder.of(jars.toArray(Path[]::new));
            final List<String> modules = moduleFinder.findAll().stream()
                    .map(ModuleReference::descriptor)
                    .map(ModuleDescriptor::name)
                    .toList();
            LOGGER.info("Loading {} modules: {}", modules.size(), Arrays.toString(modules.toArray(String[]::new)));
            final ModuleLayer moduleLayer = ModuleLayer.boot();
            final Configuration configuration = moduleLayer.configuration().resolve(moduleFinder, ModuleFinder.of(), modules);
            moduleLayer.defineModulesWithOneLoader(configuration, C2MEMod.class.getClassLoader());
            isLoaded = true;
        } catch (Throwable t) {
            LOGGER.warn("Error while loading submodule", t);
        }
        isModuleLoaded = isLoaded;
    }
}
