package com.ishland.c2me.tests.testmod;

import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.collect.Sets;
import com.ibm.asyncutil.locks.AsyncSemaphore;
import com.ibm.asyncutil.locks.FairAsyncSemaphore;
import com.ishland.c2me.tests.testmod.mixin.IChunkGenerator;
import com.ishland.c2me.tests.testmod.mixin.IServerChunkManager;
import com.ishland.c2me.tests.testmod.mixin.IThreadedAnvilChunkStorage;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkTicketType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.gen.feature.StructureFeature;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class PreGenTask {

    private static final Logger LOGGER = LogManager.getLogger("C2ME Test");
    private static final ChunkTicketType<Unit> TICKET = ChunkTicketType.create("c2metest", (unit, unit2) -> 0);

    private static final int PREGEN_SPAWN_RADIUS = 32;
    private static final int PREGEN_RADIUS = 12;
    private static final int SEARCH_RADIUS = 512 * 16;

    public static CompletableFuture<Void> runPreGen(ServerWorld world, Consumer<ChunkGeneratedEventInfo> eventListener) {
        Preconditions.checkNotNull(eventListener);
        System.err.printf("Starting pre-generation in %s;%s\n", world.toString(), world.getRegistryKey().getValue().toString());
        final BlockPos spawnPos = world.getSpawnPos();
        final Set<ChunkPos> chunksHashed = Sets.newConcurrentHashSet();
        final List<ChunkPos> chunks = Collections.synchronizedList(new ArrayList<>());
        chunks.addAll(createPreGenChunks(new ChunkPos(spawnPos), PREGEN_SPAWN_RADIUS, chunksHashed::add));
        final AtomicInteger locatedBiomes = new AtomicInteger();
        final AtomicInteger locatedStructures = new AtomicInteger();
        final List<Biome> biomes = world.getChunkManager().getChunkGenerator().getBiomeSource().getBiomes();
        final Set<StructureFeature<?>> structureFeatures = StructureFeature.STRUCTURES.values().stream()
                .filter(structureFeature -> ((IChunkGenerator) world.getChunkManager().getChunkGenerator()).getPopulationSource().hasStructureFeature(structureFeature))
                .collect(Collectors.toSet());
        final Registry<Biome> biomeRegistry = (Registry<Biome>) Registry.REGISTRIES.get(Registry.BIOME_KEY.getValue());
        final CompletableFuture<Void> biomeFuture = CompletableFuture.allOf(biomes.stream()
                .map(biome -> CompletableFuture.runAsync(() -> {
                    final BlockPos blockPos = world.locateBiome(biome, spawnPos, SEARCH_RADIUS, 8);
                    locatedBiomes.incrementAndGet();
                    if (blockPos != null) {
                        final ChunkPos chunkPos = new ChunkPos(blockPos);
                        chunks.addAll(createPreGenChunks(chunkPos, PREGEN_RADIUS, chunksHashed::add));
                        return;
                    }
                    LOGGER.info("Unable to locate biome {}", biomeRegistry.getId(biome));
                })).distinct().toArray(CompletableFuture[]::new));
        final CompletableFuture<Void> structureFuture = CompletableFuture.allOf(structureFeatures.stream()
                .map(structureFeature -> CompletableFuture.runAsync(() -> {
                    final BlockPos blockPos = world.locateStructure(structureFeature, spawnPos, SEARCH_RADIUS, false);
                    locatedStructures.incrementAndGet();
                    if (blockPos != null) {
                        final ChunkPos chunkPos = new ChunkPos(blockPos);
                        chunks.addAll(createPreGenChunks(chunkPos, PREGEN_RADIUS, chunksHashed::add));
                        return;
                    }
                    LOGGER.info("Unable to locate structure {}", structureFeature.getName());
                })).distinct().toArray(CompletableFuture[]::new));
        final CompletableFuture<Void> locateFuture = CompletableFuture.allOf(biomeFuture, structureFuture);
        while (!locateFuture.isDone() && world.getServer().isRunning()) {
            System.out.printf("[noprint]Locating: Biomes: %d / %d, Structures: %d / %d\n", locatedBiomes.get(), biomes.size(), locatedStructures.get(), structureFeatures.size());
            world.getChunkManager().executeQueuedTasks();
            try {
                //noinspection BusyWait
                Thread.sleep(40);
            } catch (InterruptedException ignored) {
            }
        }
        if (!world.getServer().isRunning()) return CompletableFuture.completedFuture(null);
        chunksHashed.clear();
        final int total = chunks.size();
        LOGGER.info("Total chunks: {}", total);
        AsyncSemaphore working = new FairAsyncSemaphore(320);
        AtomicLong generatedCount = new AtomicLong();
        final Set<CompletableFuture<Void>> futures = chunks.stream()
                .map(pos -> working.acquire()
                        .toCompletableFuture()
                        .thenComposeAsync(unused -> getChunkAtAsync(world, pos).thenAccept(unused1 -> {
                                    generatedCount.incrementAndGet();
                                    working.release();
                                    eventListener.accept(new ChunkGeneratedEventInfo(generatedCount.get(), total, world));
                                }),
                                runnable -> {
                                    if (world.getServer().isOnThread()) runnable.run();
                                    else
                                        ((IThreadedAnvilChunkStorage) world.getChunkManager().threadedAnvilChunkStorage).getMainThreadExecutor().execute(runnable);
                                }
                        )
                )
                .collect(Collectors.toSet());
        System.err.printf("Task for %s;%s started\n", world, world.getRegistryKey().getValue().toString());
        final CompletableFuture<Void> future = CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
        future.thenRun(() -> {
            eventListener.accept(new ChunkGeneratedEventInfo(total + 1, total, world));
            System.err.printf("Task for %s;%s completed\n", world, world.getRegistryKey().getValue().toString());
        });
        return future;
    }

    @NotNull
    private static ArrayList<ChunkPos> createPreGenChunks(ChunkPos center, int radius, Predicate<ChunkPos> shouldAdd) {
        final ArrayList<ChunkPos> chunks = new ArrayList<>((radius * 2 + 1) * (radius * 2 + 1));
        for (int x = center.x - radius; x <= center.x + radius; x++)
            for (int z = center.z - radius; z <= center.z + radius; z++) {
                final ChunkPos chunkPos = new ChunkPos(x, z);
                if (shouldAdd.test(chunkPos)) {
                    chunks.add(chunkPos);
                }
            }
        chunks.sort(Comparator.comparingInt(one -> one.getChebyshevDistance(center)));
        return chunks;
    }

    private static CompletableFuture<Void> getChunkAtAsync(ServerWorld world, ChunkPos pos) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        world.getChunkManager().addTicket(TICKET, pos, 0, Unit.INSTANCE);
        ((IServerChunkManager) world.getChunkManager()).invokeTick();
        final ChunkHolder chunkHolder = ((IThreadedAnvilChunkStorage) world.getChunkManager().threadedAnvilChunkStorage).invokeGetChunkHolder(pos.toLong());
        Preconditions.checkNotNull(chunkHolder, "chunkHolder is null");
        chunkHolder.getChunkAt(ChunkStatus.FULL, world.getChunkManager().threadedAnvilChunkStorage).thenAccept(either -> {
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
