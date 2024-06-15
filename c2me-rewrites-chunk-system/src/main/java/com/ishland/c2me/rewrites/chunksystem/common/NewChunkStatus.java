package com.ishland.c2me.rewrites.chunksystem.common;

import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.rewrites.chunksystem.common.statuses.ReadFromDisk;
import com.ishland.c2me.rewrites.chunksystem.common.statuses.VanillaWorldGenerationDelegate;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.ishland.flowsched.scheduler.ItemStatus;
import com.ishland.flowsched.scheduler.KeyStatusPair;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.ChunkGenerationSteps;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CompletionStage;
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

            final NewChunkStatus newChunkStatus = new VanillaWorldGenerationDelegate(statuses.size(), status);
            statuses.add(newChunkStatus);
            map.put(status, newChunkStatus);
        }

        return map;
    }

    public static NewChunkStatus fromVanillaLevel(int level) {
        return vanillaLevelToStatus[MathHelper.clamp(level, 0, vanillaLevelToStatus.length - 1)];
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
