package com.ishland.c2me.base.mixin.access;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerChunkLoadingManager.class)
public interface IThreadedAnvilChunkStorage {

    @Accessor
    ServerWorld getWorld();

    @Invoker
    boolean invokeUpdateHolderMap();

    @Accessor
    Long2ObjectLinkedOpenHashMap<ChunkHolder> getChunkHolders();

    @Accessor
    Long2ObjectLinkedOpenHashMap<ChunkHolder> getCurrentChunkHolders();

    @Invoker
    boolean invokeSave(ChunkHolder chunkHolder);

    @Accessor
    ThreadExecutor<Runnable> getMainThreadExecutor();

}
