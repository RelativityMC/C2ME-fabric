package com.ishland.c2me.rewrites.chunksystem.mixin;

import com.ishland.c2me.rewrites.chunksystem.common.ducks.IPOIUnloading;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.poi.PointOfInterestStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PointOfInterestStorage.class)
public abstract class MixinPointOfInterestStorage implements IPOIUnloading {

    @Shadow @Final private LongSet preloadedChunks;

    @Override
    public boolean c2me$shouldUnloadPoi(ChunkPos pos) {
        return !this.preloadedChunks.contains(pos.toLong());
    }
}
