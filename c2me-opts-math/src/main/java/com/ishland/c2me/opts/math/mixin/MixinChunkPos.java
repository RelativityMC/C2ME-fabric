package com.ishland.c2me.opts.math.mixin;

import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ChunkPos.class)
public class MixinChunkPos {

    @Shadow @Final public int x;

    @Shadow @Final public int z;

    /**
     * @author ishland
     * @reason use standard impl of equals
     */
    @Overwrite
    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ChunkPos) obj;
        return this.x == that.x && this.z == that.z;
    }
}
