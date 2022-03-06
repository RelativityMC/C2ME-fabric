package com.ishland.c2me.threading.lighting;

import net.fabricmc.loader.api.FabricLoader;

public class ModuleEntryPoint {

    private static final boolean enabled = !FabricLoader.getInstance().isModLoaded("lightbench");

}
