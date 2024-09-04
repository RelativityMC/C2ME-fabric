package com.ishland.c2me.opts.dfc.mixin;

import com.ishland.c2me.opts.dfc.common.ducks.IArrayCacheCapable;
import com.ishland.c2me.opts.dfc.common.util.ArrayCache;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/world/gen/chunk/ChunkNoiseSampler$1")
public class MixinChunkNoiseSampler1 implements IArrayCacheCapable {
    @Shadow
    @Final
    ChunkNoiseSampler field_36595;

    @Override
    public ArrayCache c2me$getArrayCache() {
        return ((IArrayCacheCapable) this.field_36595).c2me$getArrayCache();
    }
}
