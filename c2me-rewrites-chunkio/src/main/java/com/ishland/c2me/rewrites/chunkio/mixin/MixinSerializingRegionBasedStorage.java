package com.ishland.c2me.rewrites.chunkio.mixin;

import com.ishland.c2me.base.common.scheduler.PriorityUtils;
import com.ishland.c2me.rewrites.chunkio.common.C2MEStorageVanillaInterface;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.storage.SerializingRegionBasedStorage;
import net.minecraft.world.storage.StorageIoWorker;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.nio.file.Path;
import java.util.function.IntSupplier;
import java.util.function.LongFunction;

@Mixin(SerializingRegionBasedStorage.class)
public class MixinSerializingRegionBasedStorage {

    @Shadow @Final protected HeightLimitView world;

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/world/storage/StorageIoWorker"))
    private StorageIoWorker redirectStorageIoWorker(Path directory, boolean dsync, String name) {
        LongFunction<IntSupplier> priorityProvider;
        if (this.world instanceof ServerWorld serverWorld) {
            priorityProvider = pos -> PriorityUtils.getChunkPriority(serverWorld, new ChunkPos(pos));
        } else {
            priorityProvider = null;
        }
        return new C2MEStorageVanillaInterface(directory, dsync, name, priorityProvider);
    }

}
