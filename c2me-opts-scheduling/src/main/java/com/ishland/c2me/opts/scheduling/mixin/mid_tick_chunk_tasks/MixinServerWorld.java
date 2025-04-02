package com.ishland.c2me.opts.scheduling.mixin.mid_tick_chunk_tasks;

import com.ishland.c2me.opts.scheduling.common.ServerMidTickTask;
import net.minecraft.class_10961;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public class MixinServerWorld {

    @Shadow @Final private class_10961 field_58288;

    @Inject(method = {"tickBlock", "tickFluid"}, at = @At("RETURN"), require = 2)
    private void onPostTickBlockAndFluid(CallbackInfo info) {
        ((ServerMidTickTask) this.field_58288.method_68961()).executeTasksMidTick((ServerWorld) (Object) this);
    }

}
