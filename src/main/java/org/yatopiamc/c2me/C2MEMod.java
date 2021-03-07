package org.yatopiamc.c2me;

import com.google.common.collect.ImmutableMap;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.MinecraftVersion;
import org.yatopiamc.c2me.common.config.C2MEConfig;
import org.yatopiamc.c2me.metrics.Metrics;

import java.util.Locale;

public class C2MEMod implements ModInitializer {
    @Override
    public void onInitialize() {
        final Metrics metrics = new Metrics(10514);
        metrics.addCustomChart(new Metrics.SimplePie("environmentType", () -> FabricLoader.getInstance().getEnvironmentType().name().toLowerCase(Locale.ENGLISH)));
        metrics.addCustomChart(new Metrics.SimplePie("useThreadedWorldGeneration", () -> String.valueOf(C2MEConfig.threadedWorldGenConfig.enabled)));
        metrics.addCustomChart(new Metrics.SimplePie("useThreadedWorldFeatureGeneration", () -> String.valueOf(C2MEConfig.threadedWorldGenConfig.allowThreadedFeatures)));
        metrics.addCustomChart(new Metrics.DrilldownPie("detailedMinecraftVersion", () -> ImmutableMap.of(MinecraftVersion.GAME_VERSION.getReleaseTarget(), ImmutableMap.of(MinecraftVersion.GAME_VERSION.getName(), 1))));
    }
}
