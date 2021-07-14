package com.ishland.c2me.mixin.optimization.worldgen.global_biome_cache;

import com.ishland.c2me.common.optimization.worldgen.global_biome_cache.BiomeCache;
import com.ishland.c2me.common.optimization.worldgen.global_biome_cache.IGlobalBiomeCache;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.source.BiomeArray;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.CompletableFuture;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedChunkAnvilStorage {

    @Shadow
    protected abstract CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> loadChunk(ChunkPos pos);

    @Shadow
    @Final
    private ChunkGenerator chunkGenerator;

    @Redirect(method = "getChunk", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;loadChunk(Lnet/minecraft/util/math/ChunkPos;)Ljava/util/concurrent/CompletableFuture;"))
    private CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>> redirectLoadChunk(ThreadedAnvilChunkStorage threadedAnvilChunkStorage, ChunkPos pos) {
        if (chunkGenerator.getBiomeSource() instanceof IGlobalBiomeCache source)
            return this.loadChunk(pos).thenApplyAsync(either -> {
                either.left().ifPresent(chunk -> {
                    final BiomeArray biomeArray = source.preloadBiomes(chunk, pos, chunk.getBiomeArray());
                    if (chunk instanceof ProtoChunk protoChunk) protoChunk.setBiomes(biomeArray);
                });
                return either;
            }, BiomeCache.EXECUTOR);
        else
            return this.loadChunk(pos);
    }

}
