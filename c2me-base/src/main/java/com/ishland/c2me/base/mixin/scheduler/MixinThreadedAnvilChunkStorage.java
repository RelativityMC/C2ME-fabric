package com.ishland.c2me.base.mixin.scheduler;

import com.ishland.c2me.base.common.GlobalExecutors;
import com.ishland.c2me.base.common.scheduler.IVanillaChunkManager;
import com.ishland.c2me.base.common.scheduler.SchedulingManager;
import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ThreadedAnvilChunkStorage.class)
public class MixinThreadedAnvilChunkStorage implements IVanillaChunkManager {

    private final SchedulingManager c2me$schedulingManager = new SchedulingManager(GlobalExecutors.asyncScheduler);

    @Override
    public SchedulingManager c2me$getSchedulingManager() {
        return this.c2me$schedulingManager;
    }

    @Inject(method = "setLevel", at = @At("RETURN"))
    private void onUpdateLevel(long pos, int level, ChunkHolder holder, int i, CallbackInfoReturnable<ChunkHolder> cir) {
        this.c2me$schedulingManager.updatePriorityFromLevel(pos, level);
    }

}
