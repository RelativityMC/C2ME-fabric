package com.ishland.c2me.notickvd.mixin.servercore;

import com.bawnorton.mixinsquared.TargetHandler;
import com.ishland.c2me.base.common.theinterface.IFastChunkHolder;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerChunkManager.class)
public class MixinServerChunkManager {

    @TargetHandler(
            mixin = "me.wesley1808.servercore.mixin.optimizations.ticking.chunk.broadcast.ServerChunkCacheMixin",
            name = "servercore$broadcastChanges"
    )
    @Redirect(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkHolder;getWorldChunk()Lnet/minecraft/world/chunk/WorldChunk;"), require = 0)
    private WorldChunk includeAccessibleChunks(ChunkHolder instance) {
        if (instance instanceof IFastChunkHolder fastChunkHolder) {
            return fastChunkHolder.c2me$immediateWorldChunk();
        } else {
            return instance.getAccessibleFuture().getNow(ChunkHolder.UNLOADED_WORLD_CHUNK).orElse(null);
        }
    }

}
