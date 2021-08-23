package com.ishland.c2me.common.command;

import com.ishland.c2me.common.config.C2MEConfig;
import com.ishland.c2me.common.notickvd.IChunkTicketManager;
import com.ishland.c2me.common.threading.chunkio.ChunkIoThreadingExecutorUtils;
import com.ishland.c2me.common.threading.worldgen.WorldGenThreadingExecutorUtils;
import com.ishland.c2me.common.util.FilteringIterable;
import com.ishland.c2me.common.util.StatsTrackingExecutor;
import com.ishland.c2me.mixin.access.IServerChunkManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.ChunkPos;

import java.util.Set;

public class C2MECommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("c2me")
                        .then(
                                CommandManager.literal("stats")
                                        .executes(C2MECommands::statsCommand)
                        )
                        .then(
                                CommandManager.literal("mobcaps")
                                        .requires(unused -> C2MEConfig.noTickViewDistanceConfig.enabled)
                                        .executes(C2MECommands::mobcapsCommand)
                        )
        );
    }

    private static int statsCommand(CommandContext<ServerCommandSource> ctx) {
        ctx.getSource().sendFeedback(new LiteralText("Executor load average from last 5s, 10s, 1m, 5m, 15m: "), true);
        if (C2MEConfig.asyncIoConfig.enabled) {
            final StatsTrackingExecutor executor = ChunkIoThreadingExecutorUtils.serializerExecutor;
            ctx.getSource().sendFeedback(
                    new LiteralText(String.format("Serializer: %.1f, %.1f, %.1f, %.1f, %.1f",
                            executor.getAverage5s().average(), executor.getAverage10s().average(),
                            executor.getAverage1m().average(), executor.getAverage5m().average(),
                            executor.getAverage15m().average())),
                    true
            );
        }
        if (C2MEConfig.threadedWorldGenConfig.enabled) {
            final StatsTrackingExecutor executor = WorldGenThreadingExecutorUtils.mainExecutor;
            ctx.getSource().sendFeedback(
                    new LiteralText(String.format("WorldGen: %.1f, %.1f, %.1f, %.1f, %.1f",
                            executor.getAverage5s().average(), executor.getAverage10s().average(),
                            executor.getAverage1m().average(), executor.getAverage5m().average(),
                            executor.getAverage15m().average())),
                    true
            );
        }
        if (C2MEConfig.noTickViewDistanceConfig.enabled) {
            final ServerChunkManager chunkManager = ctx.getSource().getWorld().toServerWorld().getChunkManager();
            final ChunkTicketManager ticketManager = ((IServerChunkManager) chunkManager).getTicketManager();
            final int noTickPendingTicketUpdates = ((IChunkTicketManager) ticketManager).getNoTickPendingTicketUpdates();
            ctx.getSource().sendFeedback(new LiteralText(String.format("No-tick chunk pending ticket updates: %d", noTickPendingTicketUpdates)), true);
        }

        return 0;
    }

    private static int mobcapsCommand(CommandContext<ServerCommandSource> ctx) {
        final ServerWorld serverWorld = ctx.getSource().getWorld().toServerWorld();
        final ServerChunkManager chunkManager = serverWorld.getChunkManager();
        final ChunkTicketManager ticketManager = ((IServerChunkManager) chunkManager).getTicketManager();
        final Set<ChunkPos> noTickOnlyChunks = ((IChunkTicketManager) ticketManager).getNoTickOnlyChunks();
        final Iterable<Entity> iterable;
        if (noTickOnlyChunks == null) {
            iterable = serverWorld.iterateEntities();
        } else {
            iterable = new FilteringIterable<>(serverWorld.iterateEntities(), entity -> !noTickOnlyChunks.contains(entity.getChunkPos()));
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
