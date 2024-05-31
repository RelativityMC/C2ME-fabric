package com.ishland.c2me.base.mixin.access;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.Executor;

@Mixin(ChunkHolder.class)
public interface IChunkHolder {

    @Invoker
    void invokeUpdateFutures(ServerChunkLoadingManager chunkStorage, Executor executor);

}
