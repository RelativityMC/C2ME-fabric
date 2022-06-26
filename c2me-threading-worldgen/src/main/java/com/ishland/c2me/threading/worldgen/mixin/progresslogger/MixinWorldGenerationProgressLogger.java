package com.ishland.c2me.threading.worldgen.mixin.progresslogger;

import net.minecraft.server.WorldGenerationProgressLogger;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.chunk.ChunkStatus;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldGenerationProgressLogger.class)
public class MixinWorldGenerationProgressLogger {

    @Shadow
    @Final
    private static Logger LOGGER;
    @Shadow
    @Final
    private int totalCount;
    private volatile ChunkPos spawnPos = null;
    private volatile int radius = 0;
    private volatile int chunkStatusTransitions = 0;
    private int chunkStatuses = 0;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(int radius, CallbackInfo info) {
        ChunkStatus status = ChunkStatus.FULL;
        this.radius = radius;
        chunkStatuses = 0;
        chunkStatusTransitions = 0;
        while ((status = status.getPrevious()) != ChunkStatus.EMPTY)
            chunkStatuses++;
        chunkStatuses++;
    }

    @Inject(method = "start(Lnet/minecraft/util/math/ChunkPos;)V", at = @At("RETURN"))
    private void onStart(ChunkPos spawnPos, CallbackInfo ci) {
        this.spawnPos = spawnPos;
    }

    @Inject(method = "setChunkStatus", at = @At("HEAD"))
    private void onSetChunkStatus(ChunkPos pos, ChunkStatus status, CallbackInfo ci) {
        if (status != null && (this.spawnPos == null || pos.getChebyshevDistance(spawnPos) <= radius)) this.chunkStatusTransitions++;
    }

    /**
     * @author ishland
     * @reason replace impl
     */
    @Overwrite
    public int getProgressPercentage() {
        // LOGGER.info("{} / {}", chunkStatusTransitions, totalCount * chunkStatuses);
        return MathHelper.floor((float) this.chunkStatusTransitions * 100.0F / (float) (this.totalCount * chunkStatuses));
    }

}
