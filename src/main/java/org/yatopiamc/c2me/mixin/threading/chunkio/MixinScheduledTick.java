package org.yatopiamc.c2me.mixin.threading.chunkio;

import net.minecraft.world.ScheduledTick;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.concurrent.atomic.AtomicLong;

@Mixin(ScheduledTick.class)
public class MixinScheduledTick {

    private static final AtomicLong COUNTER = new AtomicLong(0);
    @Mutable
    @Shadow
    @Final
    private long id;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo info) {
        this.id = COUNTER.getAndIncrement();
    }

}
