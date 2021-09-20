package com.ishland.c2me.common.util;

import com.ishland.c2me.C2MEMod;
import com.ishland.c2me.libs.Locator;

import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Path;
import java.util.List;

public class ModuleUtil {

    public static final boolean isModuleLoaded;

    static {
        boolean isLoaded = false;
        try {
            final Path jars = Path.of(Locator.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParent();
            final ModuleFinder moduleFinder = ModuleFinder.of(jars);
            final List<String> modules = moduleFinder.findAll().stream()
                    .map(ModuleReference::descriptor)
                    .map(ModuleDescriptor::name)
                    .toList();
            final ModuleLayer moduleLayer = ModuleLayer.boot();
            final Configuration configuration = moduleLayer.configuration().resolve(moduleFinder, ModuleFinder.of(), modules);
            moduleLayer.defineModulesWithOneLoader(configuration, C2MEMod.class.getClassLoader());
            isLoaded = true;
        } catch (Throwable t) {
            C2MEMod.LOGGER.debug("Error while loading module", t);
        }
        isModuleLoaded = isLoaded;
    }
}
