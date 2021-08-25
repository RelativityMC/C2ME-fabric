package com.ishland.c2me.common.command;

import com.ishland.c2me.common.config.C2MEConfig;
import com.ishland.c2me.common.notickvd.IChunkTicketManager;
import com.ishland.c2me.common.perftracking.PerfTrackingObject;
import com.ishland.c2me.common.perftracking.PerfTrackingRegistry;
import com.ishland.c2me.common.threading.chunkio.ChunkIoThreadingExecutorUtils;
import com.ishland.c2me.common.threading.worldgen.WorldGenThreadingExecutorUtils;
import com.ishland.c2me.common.util.AsyncCombinedLock;
import com.ishland.c2me.common.util.FilteringIterable;
import com.ishland.c2me.mixin.access.IServerChunkManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;

import java.nio.file.Path;

public class C2MECommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("c2me")
                        .then(
                                CommandManager.literal("stats")
                                        .executes(C2MECommands::statsCommand)
                                        .then(
                                                CommandManager.literal("ioWorkers")
                                                        .executes(C2MECommands::statsIoWorkersCommand)
                                        )
                                        .then(
                                                CommandManager.literal("tacs")
                                                        .executes(C2MECommands::statsTACsCommand)
                                        )
                                        .then(
                                                CommandManager.literal("threadExecutors")
                                                        .executes(C2MECommands::statsThreadExecutorsCommand)
                                        )
                                        .then(
                                                CommandManager.literal("all")
                                                        .executes(ctx -> {
                                                            statsCommand(ctx);
                                                            statsIoWorkersCommand(ctx);
                                                            statsTACsCommand(ctx);
                                                            statsThreadExecutorsCommand(ctx);
                                                            return 0;
                                                        })
                                        )
                        )
                        .then(
                                CommandManager.literal("notick")
                                        .requires(unused -> C2MEConfig.noTickViewDistanceConfig.enabled)
                                        .executes(C2MECommands::noTickCommand)
                        )
                        .then(
                                CommandManager.literal("debug")
                                        .requires(unused -> FabricLoader.getInstance().isDevelopmentEnvironment())
                                        .then(
                                                CommandManager.literal("mobcaps")
                                                        .requires(unused -> C2MEConfig.noTickViewDistanceConfig.enabled)
                                                        .executes(C2MECommands::mobcapsCommand)
                                        )
                        )
        );
    }

    private static int noTickCommand(CommandContext<ServerCommandSource> ctx) {
        final ServerChunkManager chunkManager = ctx.getSource().getWorld().toServerWorld().getChunkManager();
        final ChunkTicketManager ticketManager = ((IServerChunkManager) chunkManager).getTicketManager();
        final int noTickOnlyChunks = ((IChunkTicketManager) ticketManager).getNoTickOnlyChunks().size();
        final int noTickPendingTicketUpdates = ((IChunkTicketManager) ticketManager).getNoTickPendingTicketUpdates();
        ctx.getSource().sendFeedback(new LiteralText(String.format("No-tick chunks: %d", noTickOnlyChunks)), true);
        ctx.getSource().sendFeedback(new LiteralText(String.format("No-tick chunk pending ticket updates: %d", noTickPendingTicketUpdates)), true);

        return 0;
    }

    private static int statsCommand(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(new LiteralText(""), true);
        ctx.getSource().sendFeedback(new LiteralText("System load average from last 5s, 10s, 1m, 5m, 15m: "), true);
        if (C2MEConfig.asyncIoConfig.enabled) {
            printLoad(ctx, ChunkIoThreadingExecutorUtils.serializerExecutor, "Serializer");
        }
        printLoad(ctx, PerfTrackingRegistry.overall(PerfTrackingRegistry.ioWorkers), "IO Workers Overall");
        printLoad(ctx, PerfTrackingRegistry.overall(PerfTrackingRegistry.TACs), "WorldGen Systems Overall");
        if (C2MEConfig.threadedWorldGenConfig.enabled) {
            printLoad(ctx, WorldGenThreadingExecutorUtils.mainExecutor, "WorldGen Workers");
        }
        printLoad(ctx, AsyncCombinedLock.getPerfTrackingObject(), "Lock System");
        printLoad(ctx, AsyncCombinedLock.lockWorker, "Lock Workers");
        printLoad(ctx, PerfTrackingRegistry.overall(PerfTrackingRegistry.threadExecutors), "Thread Executors Overall");

        ctx.getSource().sendFeedback(new LiteralText(""), true);

        return 0;
    }

    private static int statsIoWorkersCommand(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(new LiteralText(""), true);
        ctx.getSource().sendFeedback(new LiteralText("IO Workers detailed load average from last 5s, 10s, 1m, 5m, 15m: "), true);
        for (PerfTrackingObject.PerfTrackingIoWorker perfTrackingIoWorker : PerfTrackingRegistry.ioWorkers) {
            printLoad(ctx, perfTrackingIoWorker, Path.of(".").relativize(perfTrackingIoWorker.getDirectory().toPath()).toString());
        }
        return 0;
    }

    private static int statsTACsCommand(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(new LiteralText(""), true);
        ctx.getSource().sendFeedback(new LiteralText("WorldGen Systems detailed load average from last 5s, 10s, 1m, 5m, 15m: "), true);
        for (PerfTrackingObject.PerfTrackingTACS perfTrackingTACS : PerfTrackingRegistry.TACs) {
            printLoad(ctx, perfTrackingTACS, perfTrackingTACS.getWorldRegistryKey().toString());
        }
        return 0;
    }

    private static int statsThreadExecutorsCommand(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(new LiteralText(""), true);
        ctx.getSource().sendFeedback(new LiteralText("Thread Executors detailed load average from last 5s, 10s, 1m, 5m, 15m: "), true);
        for (PerfTrackingObject.PerfTrackingThreadExecutor perfTrackingThreadExecutor : PerfTrackingRegistry.threadExecutors) {
            printLoad(ctx, perfTrackingThreadExecutor, String.format("%s (%s)", perfTrackingThreadExecutor.getThreadName(), perfTrackingThreadExecutor.getExecutorName()));
        }
        return 0;
    }

    private static void printLoad(CommandContext<ServerCommandSource> ctx, PerfTrackingObject perfTrackingObject, String name) {
        ctx.getSource().sendFeedback(
                new LiteralText(String.format("%s %.1f, %.1f, %.1f, %.1f, %.1f", name,
                        perfTrackingObject.getAverage5s(), perfTrackingObject.getAverage10s(),
                        perfTrackingObject.getAverage1m(), perfTrackingObject.getAverage5m(),
                        perfTrackingObject.getAverage15m())),
                true
        );
    }

    private static int mobcapsCommand(CommandContext<ServerCommandSource> ctx) {
        final ServerWorld serverWorld = ctx.getSource().getWorld().toServerWorld();
        final ServerChunkManager chunkManager = serverWorld.getChunkManager();
        final ChunkTicketManager ticketManager = ((IServerChunkManager) chunkManager).getTicketManager();
        final LongSet noTickOnlyChunks = ((IChunkTicketManager) ticketManager).getNoTickOnlyChunks();
        final Iterable<Entity> iterable;
        if (noTickOnlyChunks == null) {
            iterable = serverWorld.iterateEntities();
        } else {
            iterable = new FilteringIterable<>(serverWorld.iterateEntities(), entity -> !noTickOnlyChunks.contains(entity.getChunkPos().toLong()));
        }

        ctx.getSource().sendFeedback(new LiteralText("Mobcap details"), true);
        for (Entity entity : iterable) {
            if (entity instanceof MobEntity mobEntity) {
                ctx.getSource().sendFeedback(new LiteralText(String.format("%s: ", mobEntity.getType().getSpawnGroup().asString())).append(mobEntity.getDisplayName()).append(String.format(" in %s", mobEntity.getChunkPos())), true);
            }
        }
        return 0;
    }

}
