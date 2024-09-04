package com.ishland.c2me.opts.dfc.mixin;

import com.ishland.c2me.opts.dfc.common.ducks.IArrayCacheCapable;
import com.ishland.c2me.opts.dfc.common.util.ArrayCache;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChunkNoiseSampler.class)
public class MixinChunkNoiseSampler implements IArrayCacheCapable {

    private final ArrayCache c2me$arrayCache = new ArrayCache();

    @Override
    public ArrayCache c2me$getArrayCache() {
        return this.c2me$arrayCache != null ? this.c2me$arrayCache : new ArrayCache();
    }
}
