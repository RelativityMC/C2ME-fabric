/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.rewrites.chunksystem.common.statuses;

import com.ishland.c2me.base.common.threadstate.ThreadInstrumentation;
import com.ishland.c2me.base.common.util.RxJavaUtils;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.base.mixin.access.IVersionedChunkStorage;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkState;
import com.ishland.c2me.rewrites.chunksystem.common.async_chunkio.ChunkIoMainThreadTaskUtils;
import com.ishland.c2me.rewrites.chunksystem.common.threadstate.ChunkTaskWork;
import com.ishland.flowsched.scheduler.Cancellable;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.ishland.flowsched.scheduler.KeyStatusPair;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ProtoChunk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadFromDiskAsync extends ReadFromDisk {

    private static final Logger LOGGER = LoggerFactory.getLogger("ReadFromDiskAsync");

    public ReadFromDiskAsync(int ordinal) {
        super(ordinal);
    }

    @Override
    public Completable upgradeToThis(ChunkLoadingContext context, Cancellable cancellable) {
        final Single<ProtoChunk> single = invokeAsyncLoad(context)
                .retryWhen(RxJavaUtils.retryWithExponentialBackoff(3, 200))
                .onErrorResumeNext(throwable -> {
                    LOGGER.error("Failed to load chunk {} fully asynchronously, falling back to normal loading", context.holder().getKey(), throwable);
                    return invokeVanillaLoad(context)
                            .retryWhen(RxJavaUtils.retryWithExponentialBackoff(3, 200, new RuntimeException("Failed to load chunk fully asynchronously, falling back to normal loading", throwable)));
                });
        return finalizeLoading(context, single);
    }

    protected @NonNull Single<ProtoChunk> invokeAsyncLoad(ChunkLoadingContext context) {
        return invokeInitialChunkRead(context)
                .map(chunkSerializer -> {
                    try (var ignored = ThreadInstrumentation.getCurrent().begin(new ChunkTaskWork(context, this, true))) {
                        final ReferenceArrayList<Runnable> mainThreadQueue = new ReferenceArrayList<>();
                        if (chunkSerializer.isPresent()) {
                            ChunkIoMainThreadTaskUtils.push(mainThreadQueue);
                            try {
                                return Pair.of(
                                        chunkSerializer.get().convert(
                                                ((IThreadedAnvilChunkStorage) context.tacs()).getWorld(),
                                                ((IThreadedAnvilChunkStorage) context.tacs()).getPointOfInterestStorage(),
                                                ((IVersionedChunkStorage) context.tacs()).invokeGetStorageKey(),
                                                context.holder().getKey()
                                        ),
                                        mainThreadQueue
                                );
                            } finally {
                                ChunkIoMainThreadTaskUtils.pop(mainThreadQueue);
                            }
                        } else {
                            return Pair.of(createEmptyProtoChunk(context), mainThreadQueue);
                        }
                    }
                })
                .flatMap(pair -> postChunkLoading(context, pair.first()).toSingleDefault(pair))
                .observeOn(Schedulers.from(((IThreadedAnvilChunkStorage) context.tacs()).getMainThreadExecutor()))
                .map(pair -> {
                    try (var ignored = ThreadInstrumentation.getCurrent().begin(new ChunkTaskWork(context, this, true))) {
                        ChunkIoMainThreadTaskUtils.drainQueue(pair.second());
                    }
                    return pair.first();
                });
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependenciesToRemove(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        return EMPTY_DEPENDENCIES;
    }

    @Override
    public KeyStatusPair<ChunkPos, ChunkState, ChunkLoadingContext>[] getDependenciesToAdd(ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, ?> holder) {
        return EMPTY_DEPENDENCIES;
    }
}
