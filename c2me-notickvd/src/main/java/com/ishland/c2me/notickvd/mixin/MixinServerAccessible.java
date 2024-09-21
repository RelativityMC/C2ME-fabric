package com.ishland.c2me.notickvd.mixin;

import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.notickvd.common.Config;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(targets = "com.ishland.c2me.rewrites.chunksystem.common.statuses.ServerAccessible")
public class MixinServerAccessible {

    /**
     * @author ishland
     * @reason send chunks
     */
    @Overwrite(remap = false)
    private static boolean needSendChunks() {
        return true;
    }

    @Dynamic
    @WrapOperation(method = "lambda$upgradeToThis$0", at = @At(value = "INVOKE", target = "Lcom/ishland/c2me/rewrites/chunksystem/common/statuses/ServerAccessible;sendChunkToPlayer(Lnet/minecraft/server/world/ServerChunkLoadingManager;Lcom/ishland/flowsched/scheduler/ItemHolder;)V", remap = true), remap = false)
    private static void wrapSendChunks(ServerChunkLoadingManager tacs, ItemHolder<?, ?, ?, ?> holder, Operation<Void> original) {
        if (Config.compatibilityMode) {
            ((IThreadedAnvilChunkStorage) tacs).getMainThreadExecutor().submit(() -> original.call(tacs, holder));
        } else {
            original.call(tacs, holder);
        }
    }

}
