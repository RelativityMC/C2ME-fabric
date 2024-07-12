package com.ishland.c2me.rewrites.chunksystem.mixin;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.ChunkState;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkHolderVanillaInterface;
import com.ishland.c2me.rewrites.chunksystem.common.TheChunkSystem;
import com.ishland.flowsched.scheduler.ItemHolder;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerChunkLoadingManager.class)
public class MixinThreadedAnvilChunkStorage {

    @Shadow @Final private ServerWorld world;
    private TheChunkSystem newSystem;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        newSystem = new TheChunkSystem(
                new ThreadFactoryBuilder()
                        .setNameFormat("chunksystem-" + this.world.getRegistryKey().getValue().toUnderscoreSeparatedString())
                        .build(),
                (ServerChunkLoadingManager) (Object) this
        );
    }

    /**
     * @author ishland
     * @reason replace chunk system
     */
    @Overwrite
    @Nullable
    public ChunkHolder setLevel(long pos, int level, @Nullable ChunkHolder holder, int i) {
        return this.newSystem.vanillaIf$setLevel(pos, level);
    }

    /**
     * @author ishlan
     * @reason replace chunk system
     */
    @Overwrite
    @Nullable
    public ChunkHolder getCurrentChunkHolder(long pos) {
        final ItemHolder<ChunkPos, ChunkState, ChunkLoadingContext, NewChunkHolderVanillaInterface> holder = this.newSystem.getHolder(new ChunkPos(pos));
        return holder != null ? holder.getUserData().get() : null;
    }

    /**
     * @author ishlan
     * @reason replace chunk system
     */
    @Overwrite
    @Nullable
    public ChunkHolder getChunkHolder(long pos) {
        return this.getCurrentChunkHolder(pos);
    }

    @ModifyReturnValue(method = "shouldDelayShutdown", at = @At("RETURN"))
    private boolean delayShutdown(boolean original) {
        return original || this.newSystem.itemCount() != 0;
    }

}
