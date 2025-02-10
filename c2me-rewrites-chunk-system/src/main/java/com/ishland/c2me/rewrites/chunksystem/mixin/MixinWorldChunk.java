package com.ishland.c2me.rewrites.chunksystem.mixin;

import com.ishland.c2me.rewrites.chunksystem.common.ducks.WorldChunkExtension;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(WorldChunk.class)
public class MixinWorldChunk implements WorldChunkExtension {

    @Unique
    private boolean c2me$blockTicking;

    @Override
    public boolean c2me$isBlockTicking() {
        return this.c2me$blockTicking;
    }

    @Override
    public void c2me$setBlockTicking(boolean blockTicking) {
        this.c2me$blockTicking = blockTicking;
    }

    @WrapMethod(method = "canTickBlockEntity")
    private boolean wrapCanTickBlockEntity(BlockPos pos, Operation<Boolean> original) {
        return this.c2me$isBlockTicking() && original.call(pos);
    }

}
