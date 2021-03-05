package org.yatopiamc.c2me;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.yatopiamc.c2me.common.config.C2MEConfig;
import org.yatopiamc.c2me.metrics.Metrics;

import java.util.Locale;

public class C2MEMod implements ModInitializer {
    @Override
    public void onInitialize() {
        final Metrics metrics = new Metrics(10514);
        metrics.addCustomChart(new Metrics.SimplePie("environmentType", () -> FabricLoader.getInstance().getEnvironmentType().name().toLowerCase(Locale.ENGLISH)));
        metrics.addCustomChart(new Metrics.SimplePie("useThreadedWorldGeneration", () -> String.valueOf(C2MEConfig.threadedWorldGenConfig.enabled)));
    }
}
