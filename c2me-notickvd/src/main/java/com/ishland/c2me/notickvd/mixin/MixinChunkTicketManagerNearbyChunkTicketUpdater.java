package com.ishland.c2me.notickvd.mixin;

import com.ishland.c2me.base.mixin.access.IChunkTicketManagerDistanceFromNearestPlayerTracker;
import net.minecraft.server.world.ChunkTicketManager;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChunkTicketManager.NearbyChunkTicketUpdater.class)
public abstract class MixinChunkTicketManagerNearbyChunkTicketUpdater {

    @ModifyVariable(method = "setWatchDistance", at = @At("HEAD"), argsOnly = true)
    private int clampViewDistance(int watchDistance) {
        return MathHelper.clamp(watchDistance, 0, ((IChunkTicketManagerDistanceFromNearestPlayerTracker) this).getMaxDistance());
    }

}
