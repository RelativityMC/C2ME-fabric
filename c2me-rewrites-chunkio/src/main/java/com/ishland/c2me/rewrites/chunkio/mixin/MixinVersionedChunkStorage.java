package com.ishland.c2me.rewrites.chunkio.mixin;

import com.ishland.c2me.base.common.scheduler.PriorityUtils;
import com.ishland.c2me.base.mixin.access.IThreadedAnvilChunkStorage;
import com.ishland.c2me.rewrites.chunkio.common.C2MEStorageVanillaInterface;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.StorageIoWorker;
import net.minecraft.world.storage.VersionedChunkStorage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.file.Path;
import java.util.function.IntSupplier;
import java.util.function.LongFunction;

@Mixin(VersionedChunkStorage.class)
public class MixinVersionedChunkStorage {

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/world/storage/StorageIoWorker"))
    private StorageIoWorker redirectStorageIoWorker(Path directory, boolean dsync, String name) {
        LongFunction<IntSupplier> priorityProvider;
        //noinspection ConstantConditions
        if ((Object) this instanceof ThreadedAnvilChunkStorage tacs) {
            priorityProvider = pos -> PriorityUtils.getChunkPriority(((IThreadedAnvilChunkStorage) tacs).getWorld(), new ChunkPos(pos));
        } else {
            priorityProvider = null;
        }
        //noinspection ConstantConditions
        return new C2MEStorageVanillaInterface(directory, dsync, name, priorityProvider);
    }

}
