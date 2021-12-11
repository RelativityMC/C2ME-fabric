package com.ishland.c2me.mixin.threading.worldgen;

import com.ishland.c2me.common.threading.scheduler.SchedulerThread;
import net.minecraft.server.world.ChunkTicketManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChunkTicketManager.class)
public class MixinChunkTicketManager {

    @Inject(method = "tick", at = @At("RETURN"))
    private void onTick(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ()) SchedulerThread.INSTANCE.notifyPriorityChange();
    }

}
