package com.ishland.c2me.common.util;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.ishland.c2me.common.config.C2MEConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.ProtoChunk;

public class ChunkPriorityUtils {
    public static Cache<ChunkPos, Integer> playerDistanceCache = CacheBuilder.newBuilder().maximumSize((long) C2MEConfig.globalExecutorParallelism * ChunkStatus.getMaxDistanceFromFull()).concurrencyLevel(C2MEConfig.globalExecutorParallelism).build();

    public static int getChunkPriority(World world, Chunk chunk) {
        MinecraftServer server = world.getServer();
        int viewDistance = server == null ? 1 : server.getPlayerManager().getViewDistance();

        ChunkPos chunkPos = chunk.getPos();
        Integer closestPlayerDistance = playerDistanceCache.getIfPresent(chunkPos);


        if (closestPlayerDistance == null) {
            for (PlayerEntity player : world.getPlayers()) {
                int x = Math.abs(chunkPos.x - player.getChunkPos().x);
                int z = Math.abs(chunkPos.z - player.getChunkPos().z);
                if (x <= viewDistance && z <= viewDistance) {
                    closestPlayerDistance = x + z;
                }
            }
            if (closestPlayerDistance == null) {
                closestPlayerDistance = Integer.MAX_VALUE;
            }
        }

        playerDistanceCache.put(chunkPos, closestPlayerDistance);

        int distance = ChunkStatus.getDistanceFromFull(chunk.getStatus());
        if (distance == 1) {
            playerDistanceCache.invalidate(chunkPos);
        }

        return (viewDistance / closestPlayerDistance) - distance;
    }
}
