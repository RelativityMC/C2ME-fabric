package com.ishland.c2me.client.metrics.screen;

import com.ishland.c2me.client.metrics.common.MetricsClientState;
import com.ishland.c2me.base.common.metrics.ChunkLoadingSnapshot;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/**
 * Screen displaying chunk loading performance metrics as graphs
 */
public class MetricsHudScreen extends Screen {

    private final MetricsClientState state = MetricsClientState.getInstance();

    public MetricsHudScreen() {
        super(Text.literal("C2ME Chunk Loading Metrics"));
    }

    @Override
    protected void init() {
        // Empty - no widgets needed
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        if (!state.isEnabled()) {
            int x = 10;
            int y = 10;
            context.drawText(textRenderer, "C2ME Metrics HUD is disabled in config", x, y, 0xFF5555, true);
            return;
        }

        ChunkLoadingSnapshot snapshot = state.getLatestSnapshot();

        int x = 10;
        int y = 10;
        int lineHeight = 12;

        // Title
        context.drawText(textRenderer, "C2ME Chunk Loading Metrics", x, y, 0xFFFFFF, true);
        y += lineHeight * 2;

        // Chunk system metrics
        context.drawText(textRenderer, "Chunk System:", x, y, 0xFFFF00, true);
        y += lineHeight;
        context.drawText(textRenderer, String.format("Load Time: %.1fms (min: %.1f, max: %.1f)",
            snapshot.avgChunkLoadTimeMs(), snapshot.minChunkLoadTimeMs(), snapshot.maxChunkLoadTimeMs()), x + 10, y, 0xFFFFFF, false);
        y += lineHeight;
        context.drawText(textRenderer, String.format("Queue Size: %d", snapshot.currentChunkQueueSize()), x + 10, y, 0xFFFFFF, false);
        y += lineHeight * 2;

        // Worldgen metrics
        context.drawText(textRenderer, "World Generation:", x, y, 0xFFFF00, true);
        y += lineHeight;
        context.drawText(textRenderer, String.format("Gen Time: %.1fms", snapshot.avgWorldgenTimeMs()), x + 10, y, 0xFFFFFF, false);
        y += lineHeight;
        context.drawText(textRenderer, String.format("Load Time: %.1fms", snapshot.avgWorldgenLoadTimeMs()), x + 10, y, 0xFFFFFF, false);
        y += lineHeight * 2;

        // IO metrics
        context.drawText(textRenderer, "I/O Performance:", x, y, 0xFFFF00, true);
        y += lineHeight;
        context.drawText(textRenderer, String.format("Read: %.1fms, Write: %.1fms", snapshot.avgIoReadTimeMs(), snapshot.avgIoWriteTimeMs()), x + 10, y, 0xFFFFFF, false);
        y += lineHeight;
        context.drawText(textRenderer, String.format("Backlog: %d, Pending: %d", snapshot.currentIoBacklogSize(), snapshot.currentIoPendingWrites()), x + 10, y, 0xFFFFFF, false);
        y += lineHeight * 2;

        // Scheduler metrics
        context.drawText(textRenderer, "Task Scheduler:", x, y, 0xFFFF00, true);
        y += lineHeight;
        context.drawText(textRenderer, String.format("Queue: %d, Active Threads: %d", snapshot.currentSchedulerQueueSize(), snapshot.currentSchedulerActiveThreads()), x + 10, y, 0xFFFFFF, false);
    }

    @Override
    public boolean shouldPause() {
        return false; // Don't pause the game
    }

}