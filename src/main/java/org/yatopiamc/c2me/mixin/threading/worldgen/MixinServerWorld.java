package org.yatopiamc.c2me.mixin.threading.worldgen;

import com.ibm.asyncutil.locks.AsyncLock;
import com.ibm.asyncutil.locks.AsyncNamedLock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.yatopiamc.c2me.common.threading.worldgen.IWorldGenLockable;

@Mixin(ServerWorld.class)
public class MixinServerWorld implements IWorldGenLockable {

    private volatile AsyncLock worldGenSingleThreadedLock = null;
    private volatile AsyncNamedLock<ChunkPos> worldGenChunkLock = null;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initWorldGenSingleThreadedLock(CallbackInfo ci) {
        worldGenSingleThreadedLock = AsyncLock.createFair();
        worldGenChunkLock = AsyncNamedLock.createFair();
    }

    @Override
    public AsyncLock getWorldGenSingleThreadedLock() {
        return worldGenSingleThreadedLock;
    }

    @Override
    public AsyncNamedLock<ChunkPos> getWorldGenChunkLock() {
        return worldGenChunkLock;
    }
}
