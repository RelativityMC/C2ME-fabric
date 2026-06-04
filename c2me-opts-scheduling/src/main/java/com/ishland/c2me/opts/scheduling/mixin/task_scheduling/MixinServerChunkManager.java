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

package com.ishland.c2me.opts.scheduling.mixin.task_scheduling;

import com.ishland.c2me.opts.scheduling.common.DuckChunkHolder;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LightType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(ServerChunkManager.class)
public abstract class MixinServerChunkManager {

    @Shadow @Nullable protected abstract ChunkHolder getChunkHolder(long pos);

    @Shadow @Final private ServerChunkManager.MainThreadExecutor mainThreadExecutor;

    @Shadow @Final private Set<ChunkHolder> chunksToBroadcastUpdate;

    /**
     * @author ishland
     * @reason reduce scheduling overhead with mainInvokingExecutor
     */
    @Overwrite
    public void onLightUpdate(LightType type, ChunkSectionPos pos) {
        ChunkHolder chunkHolder = this.getChunkHolder(pos.toChunkPos().toLong()); // thread-safe
        if (chunkHolder != null) {
            ((DuckChunkHolder) chunkHolder).c2me$queueLightSectionDirty(type, pos.getSectionY());
            if (((DuckChunkHolder) chunkHolder).c2me$shouldScheduleUndirty()) {
                this.mainThreadExecutor.execute(() -> {
                    if (((DuckChunkHolder) chunkHolder).c2me$undirtyLight()) {
                        this.chunksToBroadcastUpdate.add(chunkHolder);
                    }
                });
            }
        }
    }

}
