package com.ishland.c2me.notickvd.mixin;

import com.ishland.c2me.base.mixin.access.IServerChunkManager;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.notickvd.common.ChunkLevelManagerExtension;
import net.minecraft.class_10961;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MixinMinecraftServer {

    @Shadow @Nullable protected class_10961 field_59588;

    @Inject(method = "method_70559", at = @At(value = "INVOKE_STRING", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;)V", args = "ldc=Saving worlds"))
    private void stopNoTickVD(class_10961 arg, CallbackInfo ci) {
        for (ServerWorld world : arg.method_68997()) {
            ((ChunkLevelManagerExtension) ((IThreadedAnvilChunkStorage) world.getChunkManager().chunkLoadingManager).getLevelManager()).c2me$closeNoTickVD();
        }
    }

}
