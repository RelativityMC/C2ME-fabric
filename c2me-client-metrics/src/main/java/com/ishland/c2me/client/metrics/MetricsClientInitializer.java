package com.ishland.c2me.client.metrics;

import com.ishland.c2me.base.common.metrics.MetricsConfig;
import com.ishland.c2me.base.common.network.ChunkLoadingMetricsPayload;
import com.ishland.c2me.client.metrics.common.MetricsClientState;
import com.ishland.c2me.client.metrics.screen.MetricsHudOverlay;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsClientInitializer implements ClientModInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger("C2ME Client Metrics");

    private static final KeyBinding.Category METRICS_CATEGORY =
            KeyBinding.Category.create(Identifier.of("c2me", "metrics"));

    public static final KeyBinding TOGGLE_METRICS_KEY = KeyBindingHelper.registerKeyBinding(
        new KeyBinding(
            "key.c2me.metrics.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_F6,
            METRICS_CATEGORY
        )
    );

    @Override
    public void onInitializeClient() {
        MetricsConfig.reload();
        MetricsClientState.getInstance().applyConfig();
        if (!MetricsConfig.enabled) {
            LOGGER.info("Metrics disabled in config, client HUD only");
        } else {
            LOGGER.info("Initializing C2ME client metrics");
        }

        // Register network receiver
        ClientPlayNetworking.registerGlobalReceiver(ChunkLoadingMetricsPayload.ID, (payload, context) -> {
            if (!MetricsConfig.enabled) {
                return;
            }
            context.client().execute(() -> {
                MetricsClientState.getInstance().updateMetrics(payload.snapshot());
            });
        });

        HudRenderCallback.EVENT.register(new MetricsHudOverlay());

        // Register key binding handler
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (TOGGLE_METRICS_KEY.wasPressed()) {
                MetricsConfig.clientHudEnabled = !MetricsConfig.clientHudEnabled;
                MetricsConfig.save();
                MetricsClientState.getInstance().applyConfig();
                if (client.player != null) {
                    String stateText = MetricsConfig.clientHudEnabled ? "enabled" : "disabled";
                    client.player.sendMessage(Text.literal("C2ME Metrics HUD: " + stateText), true);
                }
            }
        });
    }

}
