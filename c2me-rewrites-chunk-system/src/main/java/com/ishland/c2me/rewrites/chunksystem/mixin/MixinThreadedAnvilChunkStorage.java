package com.ishland.c2me.rewrites.chunksystem.mixin;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ishland.c2me.rewrites.chunksystem.common.TheChunkSystem;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ServerChunkLoadingManager;
import net.minecraft.server.world.ServerWorld;
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
    ChunkHolder setLevel(long pos, int level, @Nullable ChunkHolder holder, int i) {
        return this.newSystem.vanillaIf$setLevel(pos, level);
    }

}
