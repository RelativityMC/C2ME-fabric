package com.ishland.c2me.rewrites.chunkio.mixin;

import com.ishland.c2me.rewrites.chunkio.common.C2MEStorageVanillaInterface;
import net.minecraft.world.storage.RecreationStorage;
import net.minecraft.world.storage.StorageIoWorker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.file.Path;

@Mixin(RecreationStorage.class)
public class MixinRecreationStorage {

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "(Ljava/nio/file/Path;ZLjava/lang/String;)Lnet/minecraft/world/storage/StorageIoWorker;"))
    private StorageIoWorker redirectStorageIoWorker(Path directory, boolean dsync, String name) {
        return new C2MEStorageVanillaInterface(directory, dsync, name);
    }

}
