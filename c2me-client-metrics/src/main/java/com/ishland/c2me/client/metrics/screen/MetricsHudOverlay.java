package com.ishland.c2me.client.metrics.screen;

import com.ishland.c2me.base.common.metrics.ChunkLoadingSnapshot;
import com.ishland.c2me.client.metrics.common.MetricsClientState;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

public class MetricsHudOverlay implements HudRenderCallback {

    private final MetricsClientState state = MetricsClientState.getInstance();

    @Override
    public void onHudRender(DrawContext context, RenderTickCounter tickCounter) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        if (!state.isEnabled()) {
            context.drawText(textRenderer, "C2ME Metrics HUD: disabled (F6)", 10, 10, 0xFF5555, true);
            return;
        }
        ChunkLoadingSnapshot snapshot = state.getLatestSnapshot();

        int x = 10;
        int y = 10;
        int lineHeight = 12;

        context.drawText(textRenderer, "C2ME Chunk Loading Metrics", x, y, 0xFFFFFF, true);
        y += lineHeight * 2;

        if (snapshot == ChunkLoadingSnapshot.EMPTY) {
            context.drawText(textRenderer, "Waiting for server metrics...", x, y, 0xAAAAAA, true);
            return;
        }

        context.drawText(textRenderer, "Chunk System:", x, y, 0xFFFF00, true);
        y += lineHeight;
        context.drawText(textRenderer, String.format("Load Time: %.1fms (min: %.1f, max: %.1f)",
                snapshot.avgChunkLoadTimeMs(), snapshot.minChunkLoadTimeMs(), snapshot.maxChunkLoadTimeMs()),
                x + 10, y, 0xFFFFFF, false);
        y += lineHeight;
        context.drawText(textRenderer, String.format("Queue Size: %d", snapshot.currentChunkQueueSize()),
                x + 10, y, 0xFFFFFF, false);
        y += lineHeight * 2;

        context.drawText(textRenderer, "World Generation:", x, y, 0xFFFF00, true);
        y += lineHeight;
        context.drawText(textRenderer, String.format("Gen Time: %.1fms", snapshot.avgWorldgenTimeMs()),
                x + 10, y, 0xFFFFFF, false);
        y += lineHeight;
        context.drawText(textRenderer, String.format("Load Time: %.1fms", snapshot.avgWorldgenLoadTimeMs()),
                x + 10, y, 0xFFFFFF, false);
        y += lineHeight * 2;

        context.drawText(textRenderer, "I/O Performance:", x, y, 0xFFFF00, true);
        y += lineHeight;
        context.drawText(textRenderer, String.format("Read: %.1fms, Write: %.1fms",
                snapshot.avgIoReadTimeMs(), snapshot.avgIoWriteTimeMs()),
                x + 10, y, 0xFFFFFF, false);
        y += lineHeight;
        context.drawText(textRenderer, String.format("Backlog: %d, Pending: %d",
                snapshot.currentIoBacklogSize(), snapshot.currentIoPendingWrites()),
                x + 10, y, 0xFFFFFF, false);
        y += lineHeight * 2;

        context.drawText(textRenderer, "Task Scheduler:", x, y, 0xFFFF00, true);
        y += lineHeight;
        context.drawText(textRenderer, String.format("Queue: %d, Active Threads: %d",
                snapshot.currentSchedulerQueueSize(), snapshot.currentSchedulerActiveThreads()),
                x + 10, y, 0xFFFFFF, false);
    }

}
