package com.ishland.c2me.mixin.optimization.chunkscheduling.mid_tick_chunk_tasks;

import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.ishland.c2me.common.optimization.chunkscheduling.ServerMidTickTask;

@Mixin(ServerTickScheduler.class)
public class MixinServerTickScheduler {

    @Shadow @Final public ServerWorld world;

    @Inject(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V", shift = At.Shift.AFTER))
    private void onPostActionTick(CallbackInfo ci) {
        ((ServerMidTickTask) this.world.getServer()).executeTasksMidTick();
    }

}
