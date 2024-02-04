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

    @Shadow public abstract void markForLightUpdate(LightType lightType, int y);

    @Shadow @Final private LightingProvider lightingProvider;
    private AtomicIntegerArray[] c2me$dirtyLightSections;
    private final AtomicBoolean c2me$scheduledLightUndirty = new AtomicBoolean(false);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(ChunkPos pos, int level, HeightLimitView world, LightingProvider lightingProvider, ChunkHolder.LevelUpdateListener levelUpdateListener, ChunkHolder.PlayersWatchingChunkProvider playersWatchingChunkProvider, CallbackInfo ci) {
        c2me$dirtyLightSections = new AtomicIntegerArray[LightType.values().length];
        for (int i = 0; i < c2me$dirtyLightSections.length; i++) {
            c2me$dirtyLightSections[i] = new AtomicIntegerArray(this.lightingProvider.getTopY() - this.lightingProvider.getBottomY() + 1);
        }
    }

    @Override
    public void c2me$queueLightSectionDirty(LightType lightType, int sectionY) {
        if (sectionY >= this.lightingProvider.getBottomY() && sectionY <= this.lightingProvider.getTopY())
            this.c2me$dirtyLightSections[lightType.ordinal()].set(sectionY - this.lightingProvider.getBottomY(), 1);
    }

    @Override
    public boolean c2me$shouldScheduleUndirty() {
        return this.c2me$scheduledLightUndirty.compareAndSet(false, true);
    }

    @Override
    public void c2me$undirtyLight() {
        if (!this.c2me$scheduledLightUndirty.compareAndSet(true, false)) {
            return;
        }
        AtomicIntegerArray[] me$dirtyLightSections = this.c2me$dirtyLightSections;
        final int bottomY = this.lightingProvider.getBottomY();
        for (int __i = 0, me$dirtyLightSectionsLength = me$dirtyLightSections.length; __i < me$dirtyLightSectionsLength; __i++) {
            AtomicIntegerArray section = me$dirtyLightSections[__i];
            LightType lightType = LightType.values()[__i];
            for (int j = 0; j < section.length(); j++) {
                if (section.compareAndSet(j, 1, 0)) {
                    this.markForLightUpdate(lightType, j + bottomY);
                }
            }
        }

    }

}
