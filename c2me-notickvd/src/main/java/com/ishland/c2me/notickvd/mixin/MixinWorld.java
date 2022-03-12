package com.ishland.c2me.notickvd.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(World.class)
public class MixinWorld {

    @Shadow @Final public boolean isClient;

    @Inject(method = "setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;II)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder$LevelType;isAfter(Lnet/minecraft/server/world/ChunkHolder$LevelType;)Z"), locals = LocalCapture.CAPTURE_FAILHARD)
    private void redirectTickingStatus(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir, WorldChunk worldChunk) {
        if (!this.isClient && worldChunk != null && worldChunk.getLevelType() == ChunkHolder.LevelType.BORDER) {
            ((ServerWorld) (Object) this).getChunkManager().markForUpdate(pos);
        }
    }

}
