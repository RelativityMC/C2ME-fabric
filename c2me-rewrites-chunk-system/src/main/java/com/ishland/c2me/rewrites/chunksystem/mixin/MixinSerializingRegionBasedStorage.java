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

package com.ishland.c2me.rewrites.chunksystem.mixin;

import com.ishland.c2me.rewrites.chunksystem.common.Config;
import com.ishland.c2me.rewrites.chunksystem.common.ducks.IPOIUnloading;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.storage.SerializingRegionBasedStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(SerializingRegionBasedStorage.class)
public abstract class MixinSerializingRegionBasedStorage<R, P> implements IPOIUnloading {

    @Shadow @Final protected HeightLimitView world;

    @Shadow @Final private Long2ObjectMap<Optional<R>> loadedElements;

    @Shadow public abstract void saveChunk(ChunkPos pos);

    @Shadow @Final private Object lock;

    @Shadow @Final private LongSet loadedChunks;

    @Shadow @Final private Long2ObjectMap<CompletableFuture<Optional<SerializingRegionBasedStorage.LoadResult<P>>>> pendingLoads;

    @Override
    public void c2me$unloadPoi(ChunkPos pos) {
        if (!Config.allowPOIUnloading) return;

        if (!this.c2me$shouldUnloadPoi(pos)) {
            return;
        }

        this.saveChunk(pos);
        for (int i = this.world.getBottomSectionCoord(); i <= this.world.getTopSectionCoord(); i++) {
            this.loadedElements.remove(ChunkSectionPos.asLong(pos.x(), i, pos.z()));
        }
        synchronized (this.lock) {
            this.loadedChunks.remove(pos.toLong());
            this.pendingLoads.remove(pos.toLong());
        }
    }

}
