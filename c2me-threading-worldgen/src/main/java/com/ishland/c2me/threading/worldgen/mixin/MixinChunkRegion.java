package com.ishland.c2me.threading.worldgen.mixin;

import com.ishland.c2me.threading.worldgen.common.Config;
import com.ishland.c2me.threading.worldgen.common.IChunkStatus;
import com.ishland.c2me.threading.worldgen.common.debug.StacktraceRecorder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.slf4j.Logger;
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
    private Chunk centerPos;
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
        if (Config.reduceLockRadius && chunkStatus != null) {
            final int reducedTaskRadius = ((IChunkStatus) chunkStatus).getReducedTaskRadius();
            lowerReducedCorner = new ChunkPos(centerPos.getPos().x - reducedTaskRadius, centerPos.getPos().z - reducedTaskRadius);
            upperReducedCorner = new ChunkPos(centerPos.getPos().x + reducedTaskRadius, centerPos.getPos().z + reducedTaskRadius);
        } else {
            lowerReducedCorner = lowerCorner;
            upperReducedCorner = upperCorner;
        }
    }

    @Inject(method = "getChunk(IILnet/minecraft/world/chunk/ChunkStatus;Z)Lnet/minecraft/world/chunk/Chunk;", at = @At("HEAD"))
    private void onGetChunk(int chunkX, int chunkZ, ChunkStatus leastStatus, boolean create, CallbackInfoReturnable<Chunk> cir) {
        if (Config.reduceLockRadius && !isInsideReducedTaskRadius(chunkX, chunkZ) && status != ChunkStatus.STRUCTURE_REFERENCES) {
            StacktraceRecorder.record();
        }
    }

    @Inject(method = "isChunkLoaded", at = @At("HEAD"))
    private void onIsChunkLoaded(int chunkX, int chunkZ, CallbackInfoReturnable<Boolean> cir) {
        if (Config.reduceLockRadius && !isInsideReducedTaskRadius(chunkX, chunkZ) && status != ChunkStatus.STRUCTURE_REFERENCES) {
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
