package com.ishland.c2me.mixin.threading.worldgen;

import com.ishland.c2me.common.config.C2MEConfig;
import com.ishland.c2me.common.threading.worldgen.IChunkStatus;
import com.ishland.c2me.common.threading.worldgen.debug.StacktraceRecorder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ChunkRegion.class)
public class MixinChunkRegion {

    @Shadow
    @Final
    private ChunkPos centerPos;
    @Shadow
    @Final
    private ChunkPos lowerCorner;
    @Shadow
    @Final
    private ChunkPos upperCorner;

    @Shadow @Final private static Logger LOGGER;
    @Shadow @Final private ChunkStatus status;
    private ChunkPos lowerReducedCorner = null;
    private ChunkPos upperReducedCorner = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(ServerWorld world, List<Chunk> list, ChunkStatus chunkStatus, int placementRadius, CallbackInfo ci) {
        if (C2MEConfig.threadedWorldGenConfig.reduceLockRadius) {
            final int reducedTaskRadius = ((IChunkStatus) chunkStatus).getReducedTaskRadius();
            lowerReducedCorner = new ChunkPos(centerPos.x - reducedTaskRadius, centerPos.z - reducedTaskRadius);
            upperReducedCorner = new ChunkPos(centerPos.x + reducedTaskRadius, centerPos.z + reducedTaskRadius);
        } else {
            lowerReducedCorner = lowerCorner;
            upperReducedCorner = upperCorner;
        }
    }

    @Inject(method = "getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;", at = @At("HEAD"))
    private void onGetChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create, CallbackInfoReturnable<Chunk> cir) {
        if (C2MEConfig.threadedWorldGenConfig.reduceLockRadius && !isInsideReducedTaskRadius(chunkX, chunkZ) && status != ChunkStatus.STRUCTURE_REFERENCES) {
            StacktraceRecorder.record();
        }
    }

    @Inject(method = "isChunkLoaded", at = @At("HEAD"))
    private void onIsChunkLoaded(int chunkX, int chunkZ, CallbackInfoReturnable<Boolean> cir) {
        if (C2MEConfig.threadedWorldGenConfig.reduceLockRadius && !isInsideReducedTaskRadius(chunkX, chunkZ) && status != ChunkStatus.STRUCTURE_REFERENCES) {
            StacktraceRecorder.record();
        }
    }

    @Unique
    private boolean isInsideReducedTaskRadius(int chunkX, int chunkZ) {
        return chunkX >= this.lowerReducedCorner.x &&
                chunkX <= this.upperReducedCorner.x &&
                chunkZ >= this.lowerReducedCorner.z &&
                chunkZ <= this.upperReducedCorner.z;
    }

}
