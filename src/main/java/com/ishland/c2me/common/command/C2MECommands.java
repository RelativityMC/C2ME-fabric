package com.ishland.c2me.common.command;

import com.ishland.c2me.common.config.C2MEConfig;
import com.ishland.c2me.common.notickvd.IChunkTicketManager;
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

public class C2MECommands {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("c2me")
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
