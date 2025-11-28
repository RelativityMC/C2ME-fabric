package com.ishland.c2me.client.uncapvd.common;

import net.caffeinemc.mods.sodium.api.config.ConfigEntryPoint;
import net.caffeinemc.mods.sodium.api.config.option.OptionFlag;
import net.caffeinemc.mods.sodium.api.config.option.OptionImpact;
import net.caffeinemc.mods.sodium.api.config.structure.ConfigBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class SodiumConfigBuilder implements ConfigEntryPoint {
    @Override
    public void registerConfigLate(ConfigBuilder builder) {
        Identifier renderDistance = Identifier.of("sodium", "general.render_distance");
        MinecraftClient client = MinecraftClient.getInstance();
        GameOptions options = client.options;
        builder.registerOwnModOptions()
                .registerOptionOverride(
                        builder.createOptionOverride()
                                .setTarget(renderDistance)
                                .setReplacement(
                                        builder.createIntegerOption(renderDistance)
                                                .setStorageHandler(() -> {
                                                    if (options != null) {
                                                        options.write();
                                                    }
                                                })
                                                .setName(Text.translatable("options.renderDistance"))
                                                .setTooltip(Text.translatable("sodium.options.view_distance.tooltip"))
                                                .setValueFormatter(v -> Text.translatable("options.chunks", v))
                                                .setRange(2, Config.maxViewDistance, 1)
                                                .setDefaultValue(12)
                                                .setBinding(options.getViewDistance()::setValue, options.getViewDistance()::getValue)
                                                .setImpact(OptionImpact.HIGH)
                                                .setFlags(OptionFlag.REQUIRES_RENDERER_RELOAD)
                                )
                );
    }
}
