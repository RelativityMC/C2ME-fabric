package com.ishland.c2me.tests.testmod;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.ibm.asyncutil.locks.AsyncSemaphore;
import com.ibm.asyncutil.locks.FairAsyncSemaphore;
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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PreGenTask {

    private static final Logger LOGGER = LogManager.getLogger("C2ME Test");
    private static final ChunkTicketType<Unit> TICKET = ChunkTicketType.create("c2metest", (unit, unit2) -> 0);

    private static final int PREGEN_RADIUS = 48;

    public static CompletableFuture<Void> runPreGen(ServerWorld world, Consumer<ChunkGeneratedEventInfo> eventListener) {
        Preconditions.checkNotNull(eventListener);
        System.err.printf("Starting pre-generation in %s;%s\n", world.toString(), world.getRegistryKey().getValue().toString());
        final ChunkPos center = new ChunkPos(world.getSpawnPos());
        final ArrayList<ChunkPos> chunks = new ArrayList<>((int) Math.pow(PREGEN_RADIUS * 2 + 1, 2));
        for (int x = center.x - PREGEN_RADIUS; x <= center.x + PREGEN_RADIUS; x++)
            for (int z = center.z - PREGEN_RADIUS; z <= center.z + PREGEN_RADIUS; z++)
                chunks.add(new ChunkPos(x, z));
        final int total = chunks.size();
        AsyncSemaphore working = new FairAsyncSemaphore(320);
        AtomicLong generatedCount = new AtomicLong();
        final Set<CompletableFuture<Void>> futures = chunks.stream()
                .map(pos -> working.acquire()
                        .thenComposeAsync(CompletableFuture::completedFuture, runnable -> {
                            if (world.getServer().isOnThread()) runnable.run();
                            else
                                ((IThreadedAnvilChunkStorage) world.getChunkManager().threadedAnvilChunkStorage).getMainThreadExecutor().execute(runnable);
                        })
                        .toCompletableFuture()
                        .thenCompose(unused -> getChunkAtAsync(world, pos).thenAccept(unused1 -> {
                                    generatedCount.incrementAndGet();
                                    working.release();
                                    eventListener.accept(new ChunkGeneratedEventInfo(generatedCount.get(), total, world));
                                })
                        )
                )
                .collect(Collectors.toSet());
        System.err.printf("Task for %s;%s started\n", world, world.getRegistryKey().getValue().toString());
        final CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        future.thenRun(() -> System.err.printf("Task for %s;%s completed\n", world, world.getRegistryKey().getValue().toString()));
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
        }).exceptionally(throwable -> {
            future.completeExceptionally(throwable);
            return null;
        });
        return future;
    }

    public record ChunkGeneratedEventInfo(long generatedCount, long totalCount, ServerWorld world) {
    }

    public static class PreGenEventListener implements Consumer<PreGenTask.ChunkGeneratedEventInfo> {

        private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00%");
        private final HashMap<ServerWorld, ChunkGeneratedEventInfo> inProgressWorlds = new HashMap<>();
        private long lastLog = System.currentTimeMillis();
        private long lastPrint = System.currentTimeMillis();

        @Override
        public synchronized void accept(PreGenTask.ChunkGeneratedEventInfo chunkGeneratedEventInfo) {
            if (chunkGeneratedEventInfo.generatedCount >= chunkGeneratedEventInfo.totalCount) {
                inProgressWorlds.remove(chunkGeneratedEventInfo.world);
            } else {
                inProgressWorlds.put(chunkGeneratedEventInfo.world, chunkGeneratedEventInfo);
            }

            Supplier<String> resultSupplier = Suppliers.memoize(() -> {
                StringBuilder result = new StringBuilder();
                for (ChunkGeneratedEventInfo value : inProgressWorlds.values()) {
                    result
                            .append(value.world.getRegistryKey().getValue())
                            .append(":")
                            .append(value.generatedCount)
                            .append("/")
                            .append(value.totalCount)
                            .append(",")
                            .append(DECIMAL_FORMAT.format(value.generatedCount / (double) value.totalCount))
                            .append(" ");
                }
                return result.toString();
            });

            final long timeMillis = System.currentTimeMillis();
            if (timeMillis >= lastLog + 5000L) {
                lastLog += 5000L;
                LOGGER.info("[noprogress] " + resultSupplier.get());
            }
            if (timeMillis >= lastPrint + 100L) {
                lastPrint += 100L;
                System.out.print("[noprint]" + resultSupplier.get() + "\n");
            }
        }

    }

}
