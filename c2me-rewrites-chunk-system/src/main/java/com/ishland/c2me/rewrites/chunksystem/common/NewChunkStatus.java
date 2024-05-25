package com.ishland.c2me.rewrites.chunksystem.common;

import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.ishland.flowsched.scheduler.ItemStatus;
import com.ishland.flowsched.scheduler.KeyStatusPair;
import com.ishland.flowsched.util.Assertions;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationContext;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.IntStream;

public abstract class NewChunkStatus implements ItemStatus<ChunkPos, ChunkState, ChunkLoadingContext> {

    public static final NewChunkStatus[] ALL_STATUSES;

    public static final NewChunkStatus NEW;
    public static final NewChunkStatus DISK;
    public static final Map<ChunkStatus, NewChunkStatus> VANILLA_WORLDGEN_PIPELINE;
    public static final NewChunkStatus SERVER_ACCESSIBLE;
    public static final NewChunkStatus BLOCK_TICKING;
    public static final NewChunkStatus ENTITY_TICKING;
    public static final NewChunkStatus[] vanillaLevelToStatus;

    static {
        ArrayList<NewChunkStatus> statuses = new ArrayList<>();
        NEW = new NewChunkStatus(statuses.size(), new KeyStatusPair[0], 0, ChunkStatus.EMPTY) {
            @Override
            public CompletionStage<Void> upgradeToThis(ChunkLoadingContext context) {
                throw new UnsupportedOperationException();
            }

            @Override
            public CompletionStage<Void> downgradeFromThis(ChunkLoadingContext context) {
                throw new UnsupportedOperationException();
            }
        };
        statuses.add(NEW);
        DISK = new NewChunkStatus(statuses.size(), new KeyStatusPair[0], 0, ChunkStatus.EMPTY) {
            @Override
            public CompletionStage<Void> upgradeToThis(ChunkLoadingContext context) {
                return null;
            }

            @Override
            public CompletionStage<Void> downgradeFromThis(ChunkLoadingContext context) {
                return null;
            }
        };
        statuses.add(DISK);
        VANILLA_WORLDGEN_PIPELINE = Collections.unmodifiableMap(generateFromVanillaChunkStatus(statuses));
        SERVER_ACCESSIBLE = new NewChunkStatus(statuses.size(), new KeyStatusPair[0], 0, ChunkStatus.FULL) {
            @Override
            public CompletionStage<Void> upgradeToThis(ChunkLoadingContext context) {
                return null;
            }

            @Override
            public CompletionStage<Void> downgradeFromThis(ChunkLoadingContext context) {
                return null;
            }
        };
        statuses.add(SERVER_ACCESSIBLE);
        BLOCK_TICKING = new NewChunkStatus(statuses.size(), new KeyStatusPair[0], 0, ChunkStatus.FULL) {
            @Override
            public CompletionStage<Void> upgradeToThis(ChunkLoadingContext context) {
                return null;
            }

            @Override
            public CompletionStage<Void> downgradeFromThis(ChunkLoadingContext context) {
                return null;
            }
        };
        statuses.add(BLOCK_TICKING);
        ENTITY_TICKING = new NewChunkStatus(statuses.size(), new KeyStatusPair[0], 0, ChunkStatus.FULL) {
            @Override
            public CompletionStage<Void> upgradeToThis(ChunkLoadingContext context) {
                return null;
            }

            @Override
            public CompletionStage<Void> downgradeFromThis(ChunkLoadingContext context) {
                return null;
            }
        };
        statuses.add(ENTITY_TICKING);

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
        ALL_STATUSES = statuses.toArray(NewChunkStatus[]::new);
    }

    private static Map<ChunkStatus, NewChunkStatus> generateFromVanillaChunkStatus(ArrayList<NewChunkStatus> statuses) {
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

            final NewChunkStatus newChunkStatus = new NewChunkStatus(statuses.size(), dependencies.toArray(KeyStatusPair[]::new), lockingRadius, status) {
                @Override
                public CompletionStage<Void> upgradeToThis(ChunkLoadingContext context) {
                    final ChunkGenerationContext chunkGenerationContext = ((IThreadedAnvilChunkStorage) context.tacs()).getChunkGenerationContext();
                    Chunk chunk = context.chunks().get(context.chunks().size() / 2);
                    if (chunk.getStatus().isAtLeast(status)) {
                        return status.runLoadTask(chunkGenerationContext, null, chunk)
                                .whenComplete((chunk1, throwable) -> {
                                    if (chunk1 != null) {
                                        context.holder().getItem().set(new ChunkState(chunk1));
                                    }
                                }).thenAccept(__ -> {
                                });
                    } else {
                        return status.runGenerationTask(chunkGenerationContext, Runnable::run, null, context.chunks())
                                .whenComplete((chunk1, throwable) -> {
                                    if (chunk1 != null) {
                                        context.holder().getItem().set(new ChunkState(chunk1));
                                    }
                                }).thenAccept(__ -> {
                                });
                    }
                }

                @Override
                public CompletionStage<Void> downgradeFromThis(ChunkLoadingContext context) {
                    return CompletableFuture.completedFuture(null);
                }
            };
            statuses.add(newChunkStatus);
            map.put(status, newChunkStatus);
        }

        return map;
    }

    public static NewChunkStatus fromVanillaLevel(int level) {
        return vanillaLevelToStatus[Math.clamp(level, 0, vanillaLevelToStatus.length - 1)];
    }

    public static NewChunkStatus fromVanillaStatus(ChunkStatus status) {
        return fromVanillaLevel(ChunkLevels.getLevelFromStatus(status));
    }

    private final int ordinal;
    private final KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] dependencies;
    private final int lockingRadius;
    private final ChunkStatus effectiveVanillaStatus;

    protected NewChunkStatus(int ordinal, KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] dependencies, int lockingRadius, ChunkStatus effectiveVanillaStatus) {
        this.ordinal = ordinal;
        this.dependencies = dependencies;
        this.lockingRadius = lockingRadius;
        this.effectiveVanillaStatus = effectiveVanillaStatus;
    }

    @Override
    public ItemStatus<ChunkPos, ChunkState, ChunkLoadingContext>[] getAllStatuses() {
        return ALL_STATUSES;
    }

    @Override
    public int ordinal() {
        return this.ordinal;
    }

    public ChunkStatus getEffectiveVanillaStatus() {
        return this.effectiveVanillaStatus;
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependencies(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        return this.dependencies;
    }
}
