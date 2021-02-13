package org.yatopiamc.barium.mixin.threading;

import com.ibm.asyncutil.locks.AsyncLock;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yatopiamc.barium.common.mixininterface.IServerWorld;

@Mixin(ServerWorld.class)
public class MixinServerWorld implements IServerWorld {

    private volatile AsyncLock worldGenSingleThreadedLock = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initWorldGenSingleThreadedLock(CallbackInfo ci) {
        worldGenSingleThreadedLock = AsyncLock.createFair();
    }

    @Override
    public AsyncLock getWorldGenSingleThreadedLock() {
        return worldGenSingleThreadedLock;
    }
}
