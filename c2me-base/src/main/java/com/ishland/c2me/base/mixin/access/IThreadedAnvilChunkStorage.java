package com.ishland.c2me.base.mixin.access;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerLightingProvider;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.thread.ThreadExecutor;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkGenerationContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.concurrent.CompletableFuture;

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

    @Accessor
    ServerLightingProvider getLightingProvider();

    @Accessor
    ChunkGenerationContext getGenerationContext();

    @Invoker
    CompletableFuture<Chunk> invokeLoadChunk(ChunkPos pos);

}
