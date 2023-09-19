package com.ishland.c2me.notickvd.mixin;

import net.minecraft.block.Block;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(WorldChunk.class)
public class MixinWorldChunk {

    @ModifyArg(method = "runPostProcessing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;I)Z"))
    private int notifyListenersWhenPostProcessing(int flags) {
//        if (true) return flags;
        flags &= ~Block.NO_REDRAW; // clear NO_REDRAW
        flags |= Block.NOTIFY_LISTENERS; // set NOTIFY_LISTENERS
        return flags;
    }

}
