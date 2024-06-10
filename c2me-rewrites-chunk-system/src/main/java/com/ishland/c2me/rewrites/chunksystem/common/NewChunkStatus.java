package com.ishland.c2me.rewrites.chunksystem.common;

import com.ishland.c2me.base.common.scheduler.LockTokenImpl;
import com.ishland.c2me.base.common.scheduler.ScheduledTask;
import com.ishland.c2me.base.common.scheduler.SchedulingManager;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.rewrites.chunksystem.common.statuses.ReadFromDisk;
import com.ishland.flowsched.executor.LockToken;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.ishland.flowsched.scheduler.ItemStatus;
import com.ishland.flowsched.scheduler.KeyStatusPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationContext;
import net.minecraft.world.chunk.ChunkGenerationStep;
import net.minecraft.world.chunk.ChunkGenerationSteps;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.GenerationDependencies;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Represents the status of a chunk in the chunk system.
 *
 * @implNote Subclasses should be immutable and should not have any non-final fields.
 */
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
        NEW = new NewChunkStatus(statuses.size(), ChunkStatus.EMPTY) {
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
        DISK = new ReadFromDisk(statuses.size());
        statuses.add(DISK);
        VANILLA_WORLDGEN_PIPELINE = Collections.unmodifiableMap(generateFromVanillaChunkStatus(statuses));
        SERVER_ACCESSIBLE = new NewChunkStatus(statuses.size(), ChunkStatus.FULL) {
            @Override
            public CompletionStage<Void> upgradeToThis(ChunkLoadingContext context) {
                return ChunkGenerationSteps.LOADING.get(ChunkStatus.FULL)
                        .run(((IThreadedAnvilChunkStorage) context.tacs()).getGenerationContext(), context.chunks(), context.holder().getItem().get().chunk())
                        .whenComplete((chunk1, throwable) -> {
                            if (chunk1 != null) {
                                context.holder().getItem().set(new ChunkState(chunk1));
                            }
                        }).thenAccept(__ -> {});
            }

            @Override
            public CompletionStage<Void> downgradeFromThis(ChunkLoadingContext context) {
                return null;
            }
        };
        statuses.add(SERVER_ACCESSIBLE);
        BLOCK_TICKING = new NewChunkStatus(statuses.size(), ChunkStatus.FULL) {
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
        ENTITY_TICKING = new NewChunkStatus(statuses.size(), ChunkStatus.FULL) {
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
        for (ChunkStatus status : ChunkStatus.createOrderedList()) {
            if (status == ChunkStatus.EMPTY || status == ChunkStatus.FULL) continue;
            int lockingRadius = (status == ChunkStatus.FEATURES || status == ChunkStatus.LIGHT) ? 1 : 0;
            final KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] genDeps = getDependencyFromStep(ChunkGenerationSteps.GENERATION.get(status));
            final KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] loadDeps = getDependencyFromStep(ChunkGenerationSteps.LOADING.get(status));

            final NewChunkStatus newChunkStatus = new NewChunkStatus(statuses.size(), status) {
                @Override
                public CompletionStage<Void> upgradeToThis(ChunkLoadingContext context) {
                    final ChunkGenerationContext chunkGenerationContext = ((IThreadedAnvilChunkStorage) context.tacs()).getGenerationContext();
                    Chunk chunk = context.holder().getItem().get().chunk();
                    if (chunk.getStatus().isAtLeast(status)) {
                        return ChunkGenerationSteps.LOADING.get(status)
                                .run(((IThreadedAnvilChunkStorage) context.tacs()).getGenerationContext(), context.chunks(), chunk)
                                .whenComplete((chunk1, throwable) -> {
                                    if (chunk1 != null) {
                                        context.holder().getItem().set(new ChunkState(chunk1));
                                    }
                                }).thenAccept(__ -> {});
                    } else {
                        final ChunkGenerationStep step = ChunkGenerationSteps.GENERATION.get(status);

                        return NewChunkStatus.runTaskWithLock(chunk.getPos(), step.blockStateWriteRadius(), context.schedulingManager(),
                                () -> step.run(chunkGenerationContext, context.chunks(), chunk)
                                            .whenComplete((chunk1, throwable) -> {
                                                if (chunk1 != null) {
                                                    context.holder().getItem().set(new ChunkState(chunk1));
                                                }
                                            }).thenAccept(__ -> {})
                        );
                    }
                }

                @Override
                public CompletionStage<Void> downgradeFromThis(ChunkLoadingContext context) {
                    return CompletableFuture.completedFuture(null);
                }

                @Override
                public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependencies(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
                    final Chunk chunk = holder.getItem().get().chunk();
                    if (chunk == null) return genDeps;
                    if (chunk.getStatus().isAtLeast(status)) {
                        return loadDeps;
                    } else {
                        return genDeps;
                    }
                }
            };
            statuses.add(newChunkStatus);
            map.put(status, newChunkStatus);
        }

        return map;
    }

    private static <T> CompletableFuture<T> runTaskWithLock(ChunkPos target, int radius, SchedulingManager schedulingManager, Supplier<CompletableFuture<T>> action) {
        ObjectArrayList<LockToken> lockTargets = new ObjectArrayList<>((2 * radius + 1) * (2 * radius + 1) + 1);
        for (int x = target.x - radius; x <= target.x + radius; x++)
            for (int z = target.z - radius; z <= target.z + radius; z++)
                lockTargets.add(new LockTokenImpl(schedulingManager.getId(), ChunkPos.toLong(x, z), LockTokenImpl.Usage.WORLDGEN));

//        if (threadingType == SINGLE_THREADED) {
//            lockTargets.add(new LockTokenImpl(schedulingManager.getId(), ChunkPos.MARKER, LockTokenImpl.Usage.WORLDGEN));
//        }

        final ScheduledTask<T> task = new ScheduledTask<>(
                target.toLong(),
                action,
                lockTargets.toArray(LockToken[]::new));
        schedulingManager.enqueue(task);
        return task.getFuture();
    }

    private static KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependencyFromStep(ChunkGenerationStep step) {
        ArrayList<KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>> deps = new ArrayList<>();
        final GenerationDependencies directDependencies = step.directDependencies();
        for (int x = -directDependencies.getMaxLevel(); x <= directDependencies.getMaxLevel(); x++) {
            for (int z = -directDependencies.getMaxLevel(); z <= directDependencies.getMaxLevel(); z++) {
                if (x == 0 && z == 0) continue;
                final KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext> dep = new KeyStatusPair<>(new ChunkPos(x, z), fromVanillaStatus(directDependencies.get(Math.max(x, z))));
                deps.add(dep);
            }
        }

        return deps.toArray(KeyStatusPair[]::new);
    }

    public static NewChunkStatus fromVanillaLevel(int level) {
        return vanillaLevelToStatus[Math.clamp(level, 0, vanillaLevelToStatus.length - 1)];
    }

    public static NewChunkStatus fromVanillaStatus(ChunkStatus status) {
        return fromVanillaLevel(ChunkLevels.getLevelFromStatus(status));
    }

    private final int ordinal;
    private final ChunkStatus effectiveVanillaStatus;

    protected NewChunkStatus(int ordinal, ChunkStatus effectiveVanillaStatus) {
        this.ordinal = ordinal;
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
        return new KeyStatusPair[0];
    }
}
