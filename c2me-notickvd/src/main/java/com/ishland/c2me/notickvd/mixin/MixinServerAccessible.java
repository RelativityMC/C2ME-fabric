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

}
