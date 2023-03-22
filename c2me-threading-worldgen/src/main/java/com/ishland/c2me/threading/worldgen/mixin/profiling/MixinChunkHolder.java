package com.ishland.c2me.threading.worldgen.mixin.profiling;

import com.ishland.c2me.threading.worldgen.common.profiling.IVanillaJfrProfiler;
import com.mojang.datafixers.util.Either;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiling.jfr.Finishable;
import net.minecraft.util.profiling.jfr.FlightProfiler;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

@Mixin(ChunkHolder.class)
public abstract class MixinChunkHolder {


    @Shadow public abstract ChunkPos getPos();

    @Shadow @Final private HeightLimitView world;

    @Inject(method = "getChunkAt", at = @At("RETURN"))
    private void postGetChunkAt(ChunkStatus targetStatus, ThreadedAnvilChunkStorage chunkStorage, CallbackInfoReturnable<CompletableFuture<Either<Chunk, ChunkHolder.Unloaded>>> cir) {
        if (FlightProfiler.INSTANCE instanceof IVanillaJfrProfiler profiler && this.world instanceof ServerWorld serverWorld) {
            final Finishable finishable = profiler.startChunkLoadSchedule(this.getPos(), serverWorld.getRegistryKey(), targetStatus.getId());
            if (finishable != null) {
                cir.getReturnValue().exceptionally(unused -> null).thenRun(finishable::finish);
            }
        }
    }

}
