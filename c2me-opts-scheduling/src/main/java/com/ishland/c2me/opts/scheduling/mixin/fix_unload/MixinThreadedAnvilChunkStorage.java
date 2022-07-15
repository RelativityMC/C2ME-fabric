package com.ishland.c2me.opts.scheduling.mixin.fix_unload;

import com.ishland.c2me.base.common.structs.LongHashSet;
import com.ishland.c2me.base.common.util.ShouldKeepTickingUtils;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.thread.ThreadExecutor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

@Mixin(ThreadedAnvilChunkStorage.class)
public abstract class MixinThreadedAnvilChunkStorage {

    @Shadow @Final private ThreadExecutor<Runnable> mainThreadExecutor;

    @Shadow protected abstract void unloadChunks(BooleanSupplier shouldKeepTicking);

    @Mutable
    @Shadow @Final private LongSet unloadedChunks;

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/poi/PointOfInterestStorage;tick(Ljava/util/function/BooleanSupplier;)V"))
    private BooleanSupplier redirectTickPointOfInterestStorageTick(BooleanSupplier shouldKeepTicking) {
        return ShouldKeepTickingUtils.minimumTicks(shouldKeepTicking, 32);
    }

    @ModifyArg(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ThreadedAnvilChunkStorage;unloadChunks(Ljava/util/function/BooleanSupplier;)V"))
    private BooleanSupplier redirectTickUnloadChunks(BooleanSupplier shouldKeepTicking) {
        return ShouldKeepTickingUtils.minimumTicks(shouldKeepTicking, 32);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.unloadedChunks = new LongHashSet();
    }

}
