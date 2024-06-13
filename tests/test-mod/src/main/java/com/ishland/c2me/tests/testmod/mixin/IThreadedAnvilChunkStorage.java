package com.ishland.c2me.tests.testmod.mixin;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerChunkLoadingManager.class)
public interface IThreadedAnvilChunkStorage {

    @Accessor
    ThreadExecutor<Runnable> getMainThreadExecutor();

    @Invoker
    ChunkHolder invokeGetChunkHolder(long pos);

}
