package com.ishland.c2me.client.metrics.config;

import com.ishland.c2me.base.common.metrics.MetricsConfig;
import com.ishland.c2me.client.metrics.common.MetricsClientState;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public final class MetricsConfigScreen {

    private MetricsConfigScreen() {
    }

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("title.c2me.metrics.config"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Text.translatable("category.c2me.metrics"));

        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("option.c2me.metrics.enabled"),
                        MetricsConfig.enabled)
                .setDefaultValue(false)
                .setTooltip(Text.translatable("option.c2me.metrics.enabled.tooltip"))
                .setSaveConsumer(value -> MetricsConfig.enabled = value)
                .build());

        general.addEntry(entryBuilder.startIntField(
                        Text.translatable("option.c2me.metrics.maxHistorySize"),
                        MetricsConfig.maxHistorySize)
                .setDefaultValue(1000)
                .setMin(1)
                .setTooltip(Text.translatable("option.c2me.metrics.maxHistorySize.tooltip"))
                .setSaveConsumer(value -> MetricsConfig.maxHistorySize = value)
                .build());

        general.addEntry(entryBuilder.startIntField(
                        Text.translatable("option.c2me.metrics.broadcastIntervalMs"),
                        MetricsConfig.broadcastIntervalMs)
                .setDefaultValue(500)
                .setMin(50)
                .setTooltip(Text.translatable("option.c2me.metrics.broadcastIntervalMs.tooltip"))
                .setSaveConsumer(value -> MetricsConfig.broadcastIntervalMs = value)
                .build());

        general.addEntry(entryBuilder.startBooleanToggle(
                        Text.translatable("option.c2me.metrics.clientHudEnabled"),
                        MetricsConfig.clientHudEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("option.c2me.metrics.clientHudEnabled.tooltip"))
                .setSaveConsumer(value -> MetricsConfig.clientHudEnabled = value)
                .build());

        general.addEntry(entryBuilder.startIntField(
                        Text.translatable("option.c2me.metrics.clientHistorySize"),
                        MetricsConfig.clientHistorySize)
                .setDefaultValue(120)
                .setMin(1)
                .setTooltip(Text.translatable("option.c2me.metrics.clientHistorySize.tooltip"))
                .setSaveConsumer(value -> MetricsConfig.clientHistorySize = value)
                .build());

        builder.setSavingRunnable(() -> {
            MetricsConfig.save();
            MetricsClientState.getInstance().applyConfig();
        });

        return builder.build();
    }

}
