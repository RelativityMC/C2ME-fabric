package org.yatopiamc.c2me.mixin.threading.chunkio;

import net.minecraft.world.storage.StorageIoWorker;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.yatopiamc.c2me.common.threading.chunkio.C2MECachedRegionStorage;

import java.io.File;

@Mixin(VersionedChunkStorage.class)
public class MixinVersionedChunkStorage {

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/world/storage/StorageIoWorker"))
    private StorageIoWorker onStorageIoInit(File file, boolean bl, String string) {
        return new C2MECachedRegionStorage(file, bl, string);
    }

}
