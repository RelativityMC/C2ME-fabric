package com.ishland.c2me.opts.chunkio.mixin.async_chunk_on_player_login;

import com.ishland.c2me.opts.chunkio.common.async_chunk_on_player_login.IAsyncChunkPlayer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class MixinServerPlayerEntity implements IAsyncChunkPlayer {

    @Unique
    private NbtCompound playerData = null;

    @Unique
    private boolean chunkLoadCompleted = false;

    @Override
    public void setPlayerData(NbtCompound nbtCompound) {
        this.playerData = nbtCompound;
    }

    @Override
    public NbtCompound getPlayerData() {
        return this.playerData;
    }

    @Override
    public boolean isChunkLoadCompleted() {
        return this.chunkLoadCompleted;
    }

    @Override
    public void onChunkLoadComplete() {
        this.chunkLoadCompleted = true;
    }

    @Inject(
            method = {
                    "playerTick"
            },
            at = @At("HEAD"),
            cancellable = true
    )
    private void suppressActionsDuringChunkLoad(CallbackInfo ci) {
        if (!this.chunkLoadCompleted) ci.cancel();
    }
}
