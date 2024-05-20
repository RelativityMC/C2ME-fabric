package com.ishland.c2me.rewrites.chunksystem.common;

import com.ishland.flowsched.scheduler.ItemHolder;
import com.ishland.flowsched.scheduler.ItemStatus;
import com.ishland.flowsched.scheduler.KeyStatusPair;
import com.ishland.flowsched.util.Assertions;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class NewChunkStatus implements ItemStatus<ChunkPos, ChunkState, ChunkLoadingContext> {

    public static final NewChunkStatus NEW;
    public static final NewChunkStatus DISK;
    public static final Map<ChunkStatus, NewChunkStatus> VANILLA_WORLDGEN_PIPELINE;
    public static final NewChunkStatus SERVER_ACCESSIBLE;
    public static final NewChunkStatus BLOCK_TICKING;
    public static final NewChunkStatus ENTITY_TICKING;
    public static final NewChunkStatus[] vanillaLevelToStatus;

    static {
        AtomicInteger index = new AtomicInteger(0);
        NEW = new NewChunkStatus(index.getAndIncrement(), new KeyStatusPair[0], 0) {
            @Override
            public CompletionStage<Void> upgradeToThis(ChunkLoadingContext context) {
                return null;
            }

            @Override
            public CompletionStage<Void> downgradeFromThis(ChunkLoadingContext context) {
                return null;
            }
        };
        DISK = new NewChunkStatus(index.getAndIncrement(), new KeyStatusPair[0], 0) {
            @Override
            public CompletionStage<Void> upgradeToThis(ChunkLoadingContext context) {
                return null;
            }

            @Override
            public CompletionStage<Void> downgradeFromThis(ChunkLoadingContext context) {
                return null;
            }
        };
        VANILLA_WORLDGEN_PIPELINE = Collections.unmodifiableMap(generateFromVanillaChunkStatus(index));
        vanillaLevelToStatus = IntStream.range(0, ChunkLevels.INACCESSIBLE + 2)
                .mapToObj(level -> switch (ChunkLevels.getType(level)) {
                    case INACCESSIBLE -> {
                        final ChunkStatus vanillaStatus = ChunkLevels.getStatus(level);
                        if (vanillaStatus == ChunkStatus.EMPTY) {
                            if (level > ChunkLevels.INACCESSIBLE) {
                                yield NEW;
                            } else {
                                yield DISK;
                            }
                        } else {
                            yield VANILLA_WORLDGEN_PIPELINE.get(vanillaStatus);
                        }
                    }
                    case FULL -> SERVER_ACCESSIBLE;
                    case BLOCK_TICKING -> BLOCK_TICKING;
                    case ENTITY_TICKING -> ENTITY_TICKING;
                }).toArray(NewChunkStatus[]::new);
    }

    private static Map<ChunkStatus, NewChunkStatus> generateFromVanillaChunkStatus(AtomicInteger index) {
        Map<ChunkStatus, NewChunkStatus> map = new Reference2ReferenceOpenHashMap<>();
        ReferenceOpenHashSet<ItemStatus<ChunkPos, ChunkState, ChunkLoadingContext>> depVisited = new ReferenceOpenHashSet<>();
        for (ChunkStatus status : ChunkStatus.createOrderedList()) {
            if (status == ChunkStatus.EMPTY || status == ChunkStatus.FULL) continue;
            ArrayList<KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>> dependencies = new ArrayList<>();
            for (int x = -status.getTaskMargin(); x <= status.getTaskMargin(); x ++) {
                for (int z = -status.getTaskMargin(); z <= status.getTaskMargin(); z ++) {
                    if (x == 0 && z == 0) continue;
                    final NewChunkStatus depStatus = map.get(ChunkStatus.byDistanceFromFull(ChunkStatus.getDistanceFromFull(status) + Math.max(Math.abs(x), Math.abs(z))));
                    Assertions.assertTrue(depStatus != null, "Dependency not found");
                    if (!depVisited.contains(depStatus)) {
                        dependencies.add(new KeyStatusPair<>(new ChunkPos(x, z), depStatus));
                    }
                }
            }
            for (KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext> dependency : dependencies) {
                depVisited.add(dependency.status());
            }
            int lockingRadius = (status == ChunkStatus.FEATURES || status == ChunkStatus.LIGHT) ? 1 : 0;

            map.put(status, new NewChunkStatus(index.getAndIncrement(), dependencies.toArray(KeyStatusPair[]::new), lockingRadius) {
                @Override
                public CompletionStage<Void> upgradeToThis(ChunkLoadingContext context) {
                    Chunk chunk = context.chunks().get(context.chunks().size() / 2);
                    if (chunk.getStatus().isAtLeast(status)) {
                        return status.runLoadTask(context.context(), null, chunk)
                                .whenComplete((chunk1, throwable) -> {
                                    if (chunk1 != null) {
                                        context.holder().getItem().set(new ChunkState(chunk1));
                                    }
                                }).thenAccept(__ -> {});
                    } else {
                        return status.runGenerationTask(context.context(), Runnable::run, null, context.chunks())
                                .whenComplete((chunk1, throwable) -> {
                                    if (chunk1 != null) {
                                        context.holder().getItem().set(new ChunkState(chunk1));
                                    }
                                }).thenAccept(__ -> {});
                    }
                }

                @Override
                public CompletionStage<Void> downgradeFromThis(ChunkLoadingContext context) {
                    return CompletableFuture.completedFuture(null);
                }
            });
        }

        return map;
    }

    private final int ordinal;
    private final KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] dependencies;
    private final int lockingRadius;

    protected NewChunkStatus(int ordinal, KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] dependencies, int lockingRadius) {
        this.ordinal = ordinal;
        this.dependencies = dependencies;
        this.lockingRadius = lockingRadius;
    }

    @Override
    public ItemStatus<ChunkPos, ChunkState, ChunkLoadingContext>[] getAllStatuses() {
        return new ItemStatus[0];
    }

    @Override
    public int ordinal() {
        return 0;
    }

    @Override
    public Collection<KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>> getDependencies(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext> holder) {
        return List.of();
    }
}
