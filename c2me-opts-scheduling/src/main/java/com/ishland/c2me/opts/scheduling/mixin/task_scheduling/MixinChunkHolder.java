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
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.light.LightingProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicIntegerArray;

@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder implements DuckChunkHolder {

    @Shadow public abstract boolean markForLightUpdate(LightType lightType, int y);

    @Shadow @Final private LightingProvider lightingProvider;
    private AtomicIntegerArray[] c2me$dirtyLightSections;
    private final AtomicBoolean c2me$scheduledLightUndirty = new AtomicBoolean(false);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(ChunkPos pos, int level, HeightLimitView world, LightingProvider lightingProvider, ChunkHolder.LevelUpdateListener levelUpdateListener, ChunkHolder.PlayersWatchingChunkProvider playersWatchingChunkProvider, CallbackInfo ci) {
        c2me$dirtyLightSections = new AtomicIntegerArray[LightType.values().length];
        for (int i = 0; i < c2me$dirtyLightSections.length; i++) {
            c2me$dirtyLightSections[i] = new AtomicIntegerArray(this.lightingProvider.getMaxSectionY() - this.lightingProvider.getMinSectionY() + 1);
        }
    }

    @Override
    public void c2me$queueLightSectionDirty(LightType lightType, int sectionY) {
        if (sectionY >= this.lightingProvider.getMinSectionY() && sectionY <= this.lightingProvider.getMaxSectionY())
            this.c2me$dirtyLightSections[lightType.ordinal()].set(sectionY - this.lightingProvider.getMinSectionY(), 1);
    }

    @Override
    public boolean c2me$shouldScheduleUndirty() {
        return this.c2me$scheduledLightUndirty.compareAndSet(false, true);
    }

    @Override
    public boolean c2me$undirtyLight() {
        if (!this.c2me$scheduledLightUndirty.compareAndSet(true, false)) {
            return false;
        }
        boolean hasDirtyLight = false;
        AtomicIntegerArray[] me$dirtyLightSections = this.c2me$dirtyLightSections;
        final int bottomY = this.lightingProvider.getMinSectionY();
        for (int __i = 0, me$dirtyLightSectionsLength = me$dirtyLightSections.length; __i < me$dirtyLightSectionsLength; __i++) {
            AtomicIntegerArray section = me$dirtyLightSections[__i];
            LightType lightType = LightType.values()[__i];
            for (int j = 0; j < section.length(); j++) {
                if (section.compareAndSet(j, 1, 0)) {
                    hasDirtyLight |= this.markForLightUpdate(lightType, j + bottomY);
                }
            }
        }
        return hasDirtyLight;
    }

}
