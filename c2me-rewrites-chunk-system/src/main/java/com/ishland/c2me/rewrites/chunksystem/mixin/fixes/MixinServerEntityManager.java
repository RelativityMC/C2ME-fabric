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

package com.ishland.c2me.rewrites.chunksystem.mixin.fixes;

import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.world.ServerEntityManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntityManager.class)
public abstract class MixinServerEntityManager {

    @Mutable
    @Shadow @Final private LongSet pendingUnloads;

    @Shadow protected abstract boolean unload(long chunkPos);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void replacePendingUnloads(CallbackInfo ci) {
        this.pendingUnloads = new LongLinkedOpenHashSet(this.pendingUnloads);
    }

    /**
     * @author ishland
     * @reason use alternative method for unloading
     */
    @Overwrite
    private void unloadChunks() {
        LongSet pendingUnloads = this.pendingUnloads;
        if (!(pendingUnloads instanceof LongLinkedOpenHashSet)) {
            // set is replaced by someone else, replace it again
            pendingUnloads = this.pendingUnloads = new LongLinkedOpenHashSet(pendingUnloads);
        }

        // apparently vanilla also have a `this.trackingStatuses.get(pos) != EntityTrackingStatus.HIDDEN` check?????
        // removed here because chunks in pendingUnloads should always satisfy that constraint
        // if it does not, you have other serious problems
        LongLinkedOpenHashSet linkedOpenHashSet = (LongLinkedOpenHashSet) pendingUnloads;
        while (!linkedOpenHashSet.isEmpty()) {
            long pos = linkedOpenHashSet.removeFirstLong();
            this.unload(pos);
        }
    }

}
