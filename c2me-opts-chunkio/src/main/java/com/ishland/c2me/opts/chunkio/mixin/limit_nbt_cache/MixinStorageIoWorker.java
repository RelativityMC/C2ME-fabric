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

package com.ishland.c2me.opts.chunkio.mixin.limit_nbt_cache;

import com.ishland.c2me.opts.chunkio.common.Config;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.StorageIoWorker;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.Map;
import java.util.SequencedMap;

@Mixin(value = StorageIoWorker.class, priority = 990)
public abstract class MixinStorageIoWorker {

    @Shadow @Final private SequencedMap<ChunkPos, StorageIoWorker.Result> results;

    @Shadow protected abstract void write(ChunkPos pos, StorageIoWorker.Result result);

    @Shadow protected abstract void writeRemainingResults();

    @Shadow @Final private static Logger LOGGER;

    @Inject(method = "method_64029", at = @At("HEAD"))
    private void preTask(CallbackInfo ci) {
        checkHardLimit();
    }

    @Inject(method = "writeResult", at = @At("HEAD"))
    private void onWriteResult(CallbackInfo ci) {
        if (!this.results.isEmpty()) {
            checkHardLimit();
            if (this.results.size() >= Config.chunkDataCacheSoftLimit) {
                int writeFrequency = Math.min(1, (this.results.size() - (int) Config.chunkDataCacheSoftLimit) / 16);
                for (int i = 0; i < writeFrequency; i++) {
                    writeResult0();
                }
            }
        }
    }

    @Unique
    private void checkHardLimit() {
        if (this.results.size() >= Config.chunkDataCacheLimit) {
            LOGGER.warn("Chunk data cache size exceeded hard limit ({} >= {}), forcing writes to disk (you can increase chunkDataCacheLimit in c2me.toml)", this.results.size(), Config.chunkDataCacheLimit);
            while (this.results.size() >= Config.chunkDataCacheSoftLimit * 0.75) { // using chunkDataCacheSoftLimit is intentional
                writeResult0();
            }
        }
    }

    @Unique
    private void writeResult0() {
        // TODO [VanillaCopy] writeResult
        Iterator<Map.Entry<ChunkPos, StorageIoWorker.Result>> iterator = this.results.entrySet().iterator();
        if (iterator.hasNext()) {
            Map.Entry<ChunkPos, StorageIoWorker.Result> entry = iterator.next();
            iterator.remove();
            this.write(entry.getKey(), entry.getValue());
        }
    }

}
