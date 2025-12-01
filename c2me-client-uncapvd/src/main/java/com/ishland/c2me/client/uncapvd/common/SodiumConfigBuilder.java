package com.ishland.c2me.client.uncapvd.common;

import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.util.Identifier;

public class SodiumConfigBuilder implements ConfigEntryPoint {
    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        Identifier renderDistance = Identifier.of("sodium", "general.render_distance");
        builder.registerOwnModOptions()
                .registerOptionOverlay(
                        renderDistance,
                        builder.createIntegerOption(renderDistance)
                                .setRange(2, Config.maxViewDistance, 1)
                );
    }
}
