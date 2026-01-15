package com.ishland.c2me;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.ibm.asyncutil.util.Combinators;
import com.ishland.c2me.base.common.metrics.ChunkLoadingMetrics;
import com.ishland.c2me.base.common.metrics.MetricsConfig;
import com.ishland.c2me.base.common.network.ChunkLoadingMetricsPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.LocalRandom;
import net.minecraft.world.storage.ChunkCompressionFormat;

public class C2MEMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("C2ME");

    @Override
    public void onInitialize() {
        if (Boolean.getBoolean("com.ishland.c2me.runCompressionBenchmark")) {
            LOGGER.info("Benchmarking chunk stream speed");
            LOGGER.info("Warming up");
            for (int i = 0; i < 3; i++) {
                runBenchmark("GZIP", ChunkCompressionFormat.GZIP, true);
                runBenchmark("DEFLATE", ChunkCompressionFormat.DEFLATE, true);
                runBenchmark("UNCOMPRESSED", ChunkCompressionFormat.UNCOMPRESSED, true);
            }
            runBenchmark("GZIP", ChunkCompressionFormat.GZIP, false);
            runBenchmark("DEFLATE", ChunkCompressionFormat.DEFLATE, false);
            runBenchmark("UNCOMPRESSED", ChunkCompressionFormat.UNCOMPRESSED, false);
        }
        if (Boolean.getBoolean("com.ishland.c2me.runConsistencyTest")) {
            consistencyTest();
        }

        MetricsConfig.init();

        // Setup metrics broadcasting
        setupMetricsBroadcasting();
    }

    private void setupMetricsBroadcasting() {
        if (!MetricsConfig.enabled) {
            return;
        }

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            // Broadcast metrics every 500ms
            Thread broadcaster = new Thread(() -> {
                while (server.isRunning()) {
                    try {
                        long intervalMs = Math.max(50, MetricsConfig.broadcastIntervalMs);
                        Thread.sleep(intervalMs);

                        var snapshot = ChunkLoadingMetrics.getInstance().getSnapshot();
                        var payload = new ChunkLoadingMetricsPayload(snapshot);

                        // Send to all players
                        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                            ServerPlayNetworking.send(player, payload);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    } catch (Exception e) {
                        LOGGER.warn("Error broadcasting chunk loading metrics", e);
                    }
                }
            }, "C2ME Metrics Broadcaster");
            broadcaster.setDaemon(true);
            broadcaster.start();
        });
    }

    private void runBenchmark(String name, ChunkCompressionFormat version, boolean suppressLog) {
        try {
            final DecimalFormat decimalFormat = new DecimalFormat("0.###");
            if (!suppressLog) LOGGER.info("Generating 128MB random data");
            final byte[] bytes = new byte[128 * 1024 * 1024];
            new Random().nextBytes(bytes);
            if (!suppressLog) LOGGER.info("Starting benchmark for {}", name);
            final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            {
                final OutputStream wrappedOutputStream = version.wrap(outputStream);
                long startTime = System.nanoTime();
                wrappedOutputStream.write(bytes);
                wrappedOutputStream.close();
                long endTime = System.nanoTime();
                if (!suppressLog) LOGGER.info("{} write speed: {} MB/s ({} MB/s compressed)", name, decimalFormat.format((bytes.length / 1024.0 / 1024.0) / ((endTime - startTime) / 1_000_000_000.0)), decimalFormat.format((outputStream.size() / 1024.0 / 1024.0) / ((endTime - startTime) / 1_000_000_000.0)));
                if (!suppressLog) LOGGER.info("{} compression ratio: {} %", name, decimalFormat.format(outputStream.size() / (double) bytes.length * 100.0));
            }
            {
                final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
                final InputStream wrappedInputStream = version.wrap(inputStream);
                long startTime = System.nanoTime();
                final byte[] readAllBytes = wrappedInputStream.readAllBytes();
                wrappedInputStream.close();
                long endTime = System.nanoTime();
                if (!suppressLog) LOGGER.info("{} read speed: {} MB/s ({} MB/s compressed)", name, decimalFormat.format((readAllBytes.length / 1024.0 / 1024.0) / ((endTime - startTime) / 1_000_000_000.0)), decimalFormat.format((outputStream.size() / 1024.0 / 1024.0) / ((endTime - startTime) / 1_000_000_000.0)));
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void consistencyTest() {
        int taskSize = 512;
        AtomicIntegerArray array = new AtomicIntegerArray(taskSize);
        final List<CompletableFuture<Integer>> futures = IntStream.range(0, taskSize)
                .mapToObj(value -> CompletableFuture.supplyAsync(() -> {
                    final ChunkRandom chunkRandom = new ChunkRandom(new LocalRandom(System.nanoTime()));
                    chunkRandom.skip(4096);
                    final int i = chunkRandom.nextInt();
                    array.set(value, i);
                    return i;
                }))
                .toList();
        final List<Integer> join = Combinators.collect(futures, Collectors.toList()).toCompletableFuture().join();
        for (int i = 0; i < taskSize; i++) {
            if (array.get(i) != join.get(i))
                throw new IllegalArgumentException("Mismatch at index " + i);
        }
    }
}
