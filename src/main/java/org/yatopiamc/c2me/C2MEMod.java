package org.yatopiamc.c2me;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.MinecraftVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yatopiamc.c2me.common.config.C2MEConfig;
import org.yatopiamc.c2me.metrics.Metrics;

import java.util.Locale;

public class C2MEMod implements ModInitializer {
    public static final Logger LOGGER = LogManager.getLogger("C2ME");

    @Override
    public void onInitialize() {
        final Metrics metrics = new Metrics(10514);
        metrics.addCustomChart(new Metrics.SimplePie("environmentType", () -> FabricLoader.getInstance().getEnvironmentType().name().toLowerCase(Locale.ENGLISH)));
        metrics.addCustomChart(new Metrics.SimplePie("useThreadedWorldGeneration", () -> String.valueOf(C2MEConfig.threadedWorldGenConfig.enabled)));
        metrics.addCustomChart(new Metrics.SimplePie("useThreadedWorldFeatureGeneration", () -> String.valueOf(C2MEConfig.threadedWorldGenConfig.allowThreadedFeatures)));
        metrics.addCustomChart(new Metrics.DrilldownPie("detailedMinecraftVersion", () -> ImmutableMap.of(MinecraftVersion.field_25319.getReleaseTarget(), ImmutableMap.of(MinecraftVersion.field_25319.getName(), 1))));
    }
}
