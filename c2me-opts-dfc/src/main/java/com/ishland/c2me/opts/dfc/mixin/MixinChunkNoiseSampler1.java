package com.ishland.c2me.opts.dfc.mixin;

import com.ishland.c2me.base.mixin.access.IChunkNoiseSampler;
import com.ishland.c2me.opts.dfc.common.ducks.IArrayCacheCapable;
import com.ishland.c2me.opts.dfc.common.ducks.ICoordinatesFilling;
import com.ishland.c2me.opts.dfc.common.util.ArrayCache;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/world/gen/chunk/ChunkNoiseSampler$1")
public class MixinChunkNoiseSampler1 implements IArrayCacheCapable, ICoordinatesFilling {
    @Shadow
    @Final
    ChunkNoiseSampler field_36595;

    @Override
    public ArrayCache c2me$getArrayCache() {
        return ((IArrayCacheCapable) this.field_36595).c2me$getArrayCache();
    }

    @Override
    public void c2me$fillCoordinates(int[] x, int[] y, int[] z) {
        for (int i = 0; i < ((IChunkNoiseSampler) this.field_36595).getVerticalCellCount() + 1; i++) {
            x[i] = ((IChunkNoiseSampler) this.field_36595).getStartBlockX() + ((IChunkNoiseSampler) this.field_36595).getCellBlockX();
            y[i] = (i + ((IChunkNoiseSampler) this.field_36595).getMinimumCellY()) * ((IChunkNoiseSampler) this.field_36595).getVerticalCellBlockCount();
            z[i] = ((IChunkNoiseSampler) this.field_36595).getStartBlockZ() + ((IChunkNoiseSampler) this.field_36595).getCellBlockZ();
        }
    }
}
