package com.ishland.c2me.tests.testmod;

import com.google.common.base.Preconditions;
import com.ishland.c2me.tests.testmod.mixin.IServerChunkManager;
import com.ishland.c2me.tests.testmod.mixin.IThreadedAnvilChunkStorage;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Unit;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class PreGenTask {

    private static final Logger LOGGER = LogManager.getLogger("C2ME Test");
    private static final ChunkTicketType<Unit> TICKET = ChunkTicketType.create("c2metest", (unit, unit2) -> 0);

    private static final int PREGEN_RADIUS = 64;

    public static CompletableFuture<Void> runPreGen(ServerWorld world) {
        LOGGER.info("Starting pre-generation in {};{}", world.toString(), world.getRegistryKey().getValue().toString());
        final ChunkPos center = new ChunkPos(world.getSpawnPos());
        final ArrayList<ChunkPos> chunks = new ArrayList<>((int) Math.pow(PREGEN_RADIUS * 2 + 1, 2));
        for (int x = center.x - PREGEN_RADIUS; x <= center.x + PREGEN_RADIUS; x++)
            for (int z = center.z - PREGEN_RADIUS; z <= center.z + PREGEN_RADIUS; z++)
                chunks.add(new ChunkPos(x, z));
        final int total = chunks.size();
        final Set<CompletableFuture<Void>> futures = chunks.stream().map(pos -> getChunkAtAsync(world, pos)).collect(Collectors.toSet());
        AtomicInteger generatedCount = new AtomicInteger();
        AtomicLong lastPrint = new AtomicLong();
        for (CompletableFuture<Void> future : futures) {
            future.thenAccept(unused -> {
                generatedCount.incrementAndGet();
                final long currentTime = System.currentTimeMillis();
                if (currentTime - lastPrint.get() > 1000) {
                    lastPrint.set(currentTime);
                    LOGGER.info("Generation for {};{} in progress: {} / {} ({}%)", world, world.getRegistryKey().getValue().toString(), generatedCount, total, Math.round(generatedCount.get() / (float) total * 1000.0) / 10.0);
                }
            });
        }
        LOGGER.info("Task for {};{} started", world, world.getRegistryKey().getValue().toString());
        final CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        future.thenRun(() -> LOGGER.info("Task for {};{} completed", world, world.getRegistryKey().getValue().toString()));
        return future;
    }

    private static CompletableFuture<Void> getChunkAtAsync(ServerWorld world, ChunkPos pos) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        world.getChunkManager().addTicket(TICKET, pos, 0, Unit.INSTANCE);
        ((IServerChunkManager) world.getChunkManager()).invokeTick();
        final ChunkHolder chunkHolder = ((IThreadedAnvilChunkStorage) world.getChunkManager().threadedAnvilChunkStorage).invokeGetChunkHolder(pos.toLong());
        Preconditions.checkNotNull(chunkHolder, "chunkHolder is null");
        world.getChunkManager().threadedAnvilChunkStorage.getChunk(chunkHolder, ChunkStatus.FULL).thenAccept(either -> {
            world.getChunkManager().removeTicket(TICKET, pos, 0, Unit.INSTANCE);
            if (either.left().isPresent())
                future.complete(null);
            else if (either.right().isPresent())
                future.completeExceptionally(new RuntimeException(either.right().get().toString()));
        });
        return future;
    }

}
