/*
 * All Rights Reserved
 *
 * Copyright (c) 2025-2026 ishland
 *
 * All rights reserved. Do not redistribute.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.opts.accel.opencl.common.chunksystem_integration;

import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.opts.accel.opencl.common.ducks.TACSExtension;
import com.ishland.c2me.opts.accel.opencl.common.gen.CLServerBatchedBiomeNoiseContext;
import com.ishland.c2me.opts.accel.opencl.common.gen.CLServerWorldContext;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkState;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkStatus;
import com.ishland.c2me.rewrites.chunksystem.common.statuses.VanillaWorldGenerationDelegate;
import com.ishland.flowsched.scheduler.Cancellable;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.ishland.flowsched.scheduler.KeyStatusPair;
import com.ishland.flowsched.util.Assertions;
import io.reactivex.rxjava3.core.Completable;
import it.unimi.dsi.fastutil.longs.Long2ReferenceFunction;
import it.unimi.dsi.fastutil.longs.Long2ReferenceOpenHashMap;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.AbstractChunkHolder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationContext;
import net.minecraft.world.chunk.ChunkGenerationStep;
import net.minecraft.world.chunk.ChunkGenerationSteps;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import org.jetbrains.annotations.NotNull;

import java.lang.invoke.VarHandle;
import java.util.Objects;

public class BatchingBiomeNoiseStatus extends NewChunkStatus {

    private static BatchingBiomeNoiseStatus INSTANCE = null;

    public static BatchingBiomeNoiseStatus getInstance() {
        return Objects.requireNonNull(INSTANCE);
    }

    public static synchronized void setInstance(BatchingBiomeNoiseStatus instance) {
        Assertions.assertTrue(INSTANCE == null);
        VarHandle.fullFence();
        INSTANCE = instance;
        VarHandle.fullFence();
    }

    public BatchingBiomeNoiseStatus(int ordinal) {
        super(ordinal, ChunkStatus.STRUCTURE_REFERENCES); // the previous one
    }

    @Override
    public Completable upgradeToThis(ChunkLoadingContext context, Cancellable cancellable) {
        ChunkPos pos = context.holder().getKey();
        final Chunk startingChunk = context.holder().getItem().get().chunk();

        if (!CLServerBatchedBiomeNoiseContext.isAligned(pos.x(), pos.z()) || startingChunk.getStatus().isAtLeast(ChunkStatus.NOISE)) {
            return Completable.complete();
        }

        final ChunkGenerationContext chunkGenerationContext = ((IThreadedAnvilChunkStorage) context.tacs()).getGenerationContext();
        if (!(chunkGenerationContext.generator() instanceof NoiseChunkGenerator noiseChunkGenerator)) {
            return Completable.complete();
        }

        Long2ReferenceOpenHashMap<ChunkHolder> holderCache = new Long2ReferenceOpenHashMap<>();
        Long2ReferenceFunction<ChunkHolder> getHolder0 = posx -> context.theChunkSystem().getHolder(ChunkPos.fromLong(posx)).getUserData().get();
        Long2ReferenceFunction<ChunkHolder> getHolder = posx -> holderCache.computeIfAbsent(posx, getHolder0);
        BoundedRegionArray.Getter<AbstractChunkHolder> getHolder1 = (x1, z1) -> getHolder.get(ChunkPos.toLong(x1, z1));

        BoundedRegionArray<ProtoChunk> boundedRegionArray = new BoundedRegionArray<>(
                pos.x(),
                pos.z(),
                CLServerBatchedBiomeNoiseContext.BATCH_SIZE,
                CLServerBatchedBiomeNoiseContext.BATCH_SIZE,
                (x, z) -> (ProtoChunk) getHolder1.get(x, z).getUncheckedOrNull(ChunkStatus.STRUCTURE_REFERENCES)
        );

        BoundedRegionArray<ChunkRegion> chunkRegions = new BoundedRegionArray<>(
                pos.x(),
                pos.z(),
                CLServerBatchedBiomeNoiseContext.BATCH_SIZE,
                CLServerBatchedBiomeNoiseContext.BATCH_SIZE,
                (x, z) -> {
                    ChunkGenerationStep generationStep = ChunkGenerationSteps.GENERATION.get(ChunkStatus.BIOMES);
                    return new ChunkRegion(
                            chunkGenerationContext.world(),
                            BoundedRegionArray.create(x, z, generationStep.directDependencies().getMaxLevel(), getHolder1),
                            generationStep,
                            boundedRegionArray.get(x, z)
                    );
                }
        );

        for (int dx = 0; dx < CLServerBatchedBiomeNoiseContext.BATCH_SIZE; dx++) {
            for (int dz = 0; dz < CLServerBatchedBiomeNoiseContext.BATCH_SIZE; dz++) {
                ChunkRegion region = chunkRegions.get(pos.x() + dx, pos.z() + dz);
                if (Blender.getBlender(region) != Blender.getNoBlending()) {
                    return Completable.complete();
                }
            }
        }

        BoundedRegionArray<StructureAccessor> structureAccessors = new BoundedRegionArray<>(
                pos.x(),
                pos.z(),
                CLServerBatchedBiomeNoiseContext.BATCH_SIZE,
                CLServerBatchedBiomeNoiseContext.BATCH_SIZE,
                (x, z) -> chunkGenerationContext.world().getStructureAccessor().forRegion(chunkRegions.get(x, z))
        );

        boolean allPostNoise = true;
        for (int dx = 0; dx < CLServerBatchedBiomeNoiseContext.BATCH_SIZE; dx++) {
            for (int dz = 0; dz < CLServerBatchedBiomeNoiseContext.BATCH_SIZE; dz++) {
                ProtoChunk chunk = boundedRegionArray.get(pos.x() + dx, pos.z() + dz);
                if (chunk == null || !chunk.getStatus().isAtLeast(ChunkStatus.NOISE)) {
                    allPostNoise = false;
                    break;
                }
            }
            if (!allPostNoise) break;
        }
        if (allPostNoise) {
            return Completable.complete();
        }

        return Completable.defer(() -> {
            CLServerWorldContext clContext = ((TACSExtension) context.tacs()).c2me$getCLContext();
            return Completable.fromCompletionStage(VanillaWorldGenerationDelegate.runTaskWithLockArea(
                    pos.x(),
                    pos.z(),
                    CLServerBatchedBiomeNoiseContext.BATCH_SIZE,
                    CLServerBatchedBiomeNoiseContext.BATCH_SIZE,
                    context.schedulingManager(),
                    () -> {
                        CLServerBatchedBiomeNoiseContext batchedBiomeNoiseContext = new CLServerBatchedBiomeNoiseContext(pos, clContext, noiseChunkGenerator, chunkGenerationContext.world().getChunkManager().getNoiseConfig());
                        return batchedBiomeNoiseContext.execute(context, boundedRegionArray, structureAccessors);
                    }
            ));
        });
    }

    @Override
    public Completable postUpgradeToThis(ChunkLoadingContext context) {
        return Completable.complete();
    }

    @Override
    public Completable preDowngradeFromThis(ChunkLoadingContext context, Cancellable cancellable) {
        return Completable.complete();
    }

    @Override
    public Completable downgradeFromThis(ChunkLoadingContext context, Cancellable cancellable) {
        return Completable.complete();
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependencies(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        ChunkPos pos = holder.getKey();
        final Chunk chunk = holder.getItem().get().chunk();
        // depend on BATCH_SIZExBATCH_SIZE if aligned, nothing otherwise
        if (CLServerBatchedBiomeNoiseContext.isAligned(pos.x(), pos.z()) && !chunk.getStatus().isAtLeast(ChunkStatus.NOISE)) {
            return getGenDeps(pos);
        } else {
            return EMPTY_DEPENDENCIES;
        }
    }

    private KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext> @NotNull [] getGenDeps(ChunkPos pos) {
        KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] deps = new KeyStatusPair[(CLServerBatchedBiomeNoiseContext.BATCH_SIZE + 16) * (CLServerBatchedBiomeNoiseContext.BATCH_SIZE + 16) - 1];
        int index = 0;
        for (int dx = -8; dx < CLServerBatchedBiomeNoiseContext.BATCH_SIZE + 8; dx++) { // padding for structure_starts
            for (int dz = -8; dz < CLServerBatchedBiomeNoiseContext.BATCH_SIZE + 8; dz++) {
                if (dx == 0 && dz == 0) continue; // skip self
                if (dx >= 0 && dx < CLServerBatchedBiomeNoiseContext.BATCH_SIZE && dz >= 0 && dz < CLServerBatchedBiomeNoiseContext.BATCH_SIZE) {
                    deps[index++] = new KeyStatusPair<>(new ChunkPos(pos.x() + dx, pos.z() + dz), Objects.requireNonNull(this.getPrev()));
                } else {
                    deps[index++] = new KeyStatusPair<>(new ChunkPos(pos.x() + dx, pos.z() + dz), Objects.requireNonNull(NewChunkStatus.fromVanillaStatus(ChunkStatus.STRUCTURE_STARTS)));
                }
            }
        }
        Assertions.assertTrue(index == deps.length);
        return deps;
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependenciesToRemove(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        final KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] curDep = holder.getDependencies(this);
        if (curDep.length != 0) {
            final Chunk chunk = holder.getItem().get().chunk();
            if (chunk == null) return EMPTY_DEPENDENCIES;
            if (!chunk.getStatus().isAtLeast(ChunkStatus.NOISE)) return EMPTY_DEPENDENCIES;
            return getGenDeps(holder.getKey());
        }
        return EMPTY_DEPENDENCIES;
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependenciesToAdd(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        return EMPTY_DEPENDENCIES;
    }

    @Override
    public String toString() {
        return "c2me:batched_biome_noise";
    }

}
