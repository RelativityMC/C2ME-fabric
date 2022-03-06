package com.ishland.c2me.opts.chunkio.mixin.hide_sync_disk_writes_behind_flag;

import net.minecraft.world.storage.RegionBasedStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RegionBasedStorage.class)
public class MixinRegionBasedStorage {

    @Mutable
    @Shadow @Final private boolean dsync;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onPostInit(CallbackInfo info) {
        this.dsync = Boolean.parseBoolean(System.getProperty("com.ishland.c2me.chunkio.syncDiskWrites", "false"));
    }

}
