package com.ishland.c2me.threading.worldgen.mixin;

import com.google.common.base.Preconditions;
import com.ishland.c2me.base.common.scheduler.SchedulerThread;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.world.ChunkPosDistanceLevelPropagator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkTicketManager.DistanceFromNearestPlayerTracker.class)
public abstract class MixinChunkTicketManagerDistanceFromNearestPlayerTracker extends ChunkPosDistanceLevelPropagator {

    protected MixinChunkTicketManagerDistanceFromNearestPlayerTracker(int i, int j, int k) {
        super(i, j, k);
    }

    @Redirect(method = "updateLevels", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ChunkTicketManager$DistanceFromNearestPlayerTracker;applyPendingUpdates(I)I"))
    private int redirectUpdate(ChunkTicketManager.DistanceFromNearestPlayerTracker instance, int i) {
        //noinspection ConstantConditions
        Preconditions.checkArgument(instance == (Object) this);
        final int updates = this.applyPendingUpdates(Integer.MAX_VALUE);
        if (!((Object) this instanceof ChunkTicketManager.NearbyChunkTicketUpdater)) return updates;
        if (Integer.MAX_VALUE - updates != 0) {
            SchedulerThread.INSTANCE.notifyPriorityChange();
        }
        return updates;
    }

}
