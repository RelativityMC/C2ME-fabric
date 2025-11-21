package com.ishland.c2me.notickvd.mixin.smooth_sending_rate;

import com.ishland.c2me.notickvd.common.smooth_sending_rate.ExponentialMovingAverage;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.network.ChunkDataSender;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.chunk.WorldChunk;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ChunkDataSender.class)
public class MixinChunkDataSender {

    @Shadow
    @Final
    private LongSet chunks;

    @Shadow
    private float pending;
    @Shadow
    @Final
    private boolean local;
    @Shadow
    private float desiredBatchSize;
    @Unique
    private ExponentialMovingAverage c2me$ema;
    @Unique
    private ExponentialMovingAverage c2me$emaActual;
    @Unique
    private int c2me$lastTickSent = 0;

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void onInit(boolean local, CallbackInfo ci) {
        this.c2me$ema = new ExponentialMovingAverage(1.0 / 20.0, 10.0, 4.0);
        this.c2me$emaActual = new ExponentialMovingAverage(1.0 / 20.0, 0.1, 0.1);
    }

    @Inject(method = "sendChunkBatches", at = @At(value = "HEAD"))
    private void onTick(ServerPlayerEntity player, CallbackInfo ci) {
        this.c2me$ema.onTick(this.chunks.size());
        this.c2me$emaActual.onTick(this.c2me$lastTickSent);
//        player.sendMessage(Text.of(String.format("Current target sending rate: %.1f cps, %.1f cps, actual: %.1f cps", this.c2me$getTargetSendingRate() * 20.0, this.desiredBatchSize * 20.0, this.c2me$emaActual.getCurrent() * 20.0)), true);
        this.c2me$lastTickSent = 0;
    }

    @Redirect(method = "sendChunkBatches", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ChunkDataSender;desiredBatchSize:F", opcode = Opcodes.GETFIELD), require = 2)
    private float redirectDesiredBatchSize(ChunkDataSender instance) {
        assert instance == (Object) this;
        return this.local ? (float) this.c2me$getTargetSendingRate() : Math.min(this.desiredBatchSize, (float) this.c2me$getTargetSendingRate());
    }

    @Redirect(method = "makeBatch", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ChunkDataSender;local:Z", opcode = Opcodes.GETFIELD))
    private boolean applyClampingToLocal(ChunkDataSender instance) {
        return false;
    }

    @ModifyReturnValue(method = "makeBatch", at = @At("RETURN"))
    private List<WorldChunk> onBatch(List<WorldChunk> original) {
        this.c2me$lastTickSent = original.size();
        return original;
    }

    @Unique
    private double c2me$getTargetSendingRate() {
        return (this.c2me$ema.getCurrent()) / 10.0F;
    }

}
