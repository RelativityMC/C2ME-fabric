package com.ishland.c2me.threading.worldgen.mixin.progresslogger;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntMaps;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
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
    private Long2IntMap map = Long2IntMaps.synchronize(new Long2IntOpenHashMap(), this);
    private int chunkStatuses = 0;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(int radius, CallbackInfo info) {
        ChunkStatus status = ChunkStatus.FULL;
        this.radius = (int) ((Math.sqrt(radius) - 1) / 2); // radius is actually total count
        chunkStatuses = 0;
        chunkStatusTransitions = 0;
        while ((status = status.getPrevious()) != ChunkStatus.EMPTY)
            chunkStatuses++;
        chunkStatuses++;
    }

    @Inject(method = "start(Lnet/minecraft/util/math/ChunkPos;)V", at = @At("RETURN"))
    private void onStart(ChunkPos spawnPos, CallbackInfo ci) {
        this.spawnPos = spawnPos;
        synchronized (this) {
            for (Long2IntMap.Entry entry : this.map.long2IntEntrySet()) {
                if (entry.getIntValue() > 0) {
                    final ChunkPos pos = new ChunkPos(entry.getLongKey());
                    if (pos.getChebyshevDistance(spawnPos) <= radius) this.chunkStatusTransitions += entry.getIntValue();
                }
            }
        }
    }

    @Inject(method = "setChunkStatus", at = @At("HEAD"))
    private void onSetChunkStatus(ChunkPos pos, ChunkStatus status, CallbackInfo ci) {
        synchronized (this) {
            if (this.spawnPos == null) {
                final int i = this.map.getOrDefault(pos.toLong(), 0);
                this.map.put(pos.toLong(), i + 1);
            } else {
                if (status != null && pos.getChebyshevDistance(spawnPos) <= radius) this.chunkStatusTransitions++;
            }
        }
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
