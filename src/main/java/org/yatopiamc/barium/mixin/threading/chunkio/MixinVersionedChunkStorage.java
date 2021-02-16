package org.yatopiamc.barium.mixin.threading.chunkio;

import net.minecraft.world.storage.StorageIoWorker;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.yatopiamc.barium.common.threading.chunkio.BariumCachedRegionStorage;

import java.io.File;

@Mixin(VersionedChunkStorage.class)
public class MixinVersionedChunkStorage {

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/world/storage/StorageIoWorker"))
    private StorageIoWorker onStorageIoInit(File file, boolean bl, String string) {
        return new BariumCachedRegionStorage(file, bl, string);
    }

}
