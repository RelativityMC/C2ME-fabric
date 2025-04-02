package com.ishland.c2me.opts.scheduling.mixin.mid_tick_chunk_tasks;

import com.ishland.c2me.opts.scheduling.common.ServerMidTickTask;
import net.minecraft.class_10961;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(World.class)
public abstract class MixinWorld {

    @Shadow @Final public boolean isClient;

    @Shadow @Nullable public abstract class_10961 method_69071();

    @Inject(method = "tickEntity", at = @At("TAIL"))
    private void onPostTickEntity(CallbackInfo ci) {
        class_10961 theSecondHalfOfServer = this.method_69071();
        final MinecraftServer server = theSecondHalfOfServer != null ? theSecondHalfOfServer.method_68961() : null;
        if (!this.isClient && server != null) {
            ((ServerMidTickTask) server).executeTasksMidTick((ServerWorld) (Object) this);
        }
    }

}
