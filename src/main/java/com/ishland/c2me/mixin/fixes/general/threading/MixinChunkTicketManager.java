package com.ishland.c2me.mixin.fixes.general.threading;

import com.ibm.asyncutil.locks.AsyncReadWriteLock;
import com.ishland.c2me.common.fixes.general.threading.IChunkTicketManager;
import net.minecraft.server.world.ChunkTicketManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ChunkTicketManager.class)
public class MixinChunkTicketManager implements IChunkTicketManager {

    @Unique
    private AsyncReadWriteLock ticketLock = AsyncReadWriteLock.createFair();

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void onInit(CallbackInfo info) {
        this.ticketLock = AsyncReadWriteLock.createFair();
    }

    @Unique
    @Override
    public AsyncReadWriteLock getTicketLock() {
        return this.ticketLock;
    }


}
