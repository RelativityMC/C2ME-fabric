package org.yatopiamc.c2me.mixin.threading.chunkio;

import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.SerializingRegionBasedStorage;
import net.minecraft.world.storage.StorageIoWorker;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.yatopiamc.c2me.common.threading.chunkio.C2MECachedRegionStorage;
import org.yatopiamc.c2me.common.threading.chunkio.ISerializingRegionBasedStorage;

import java.io.File;

@Mixin(SerializingRegionBasedStorage.class)
public abstract class MixinSerializingRegionBasedStorage implements ISerializingRegionBasedStorage {

    @Shadow
    protected abstract <T> void update(ChunkPos pos, DynamicOps<T> dynamicOps, @Nullable T data);

    @Override
    public void update(ChunkPos pos, CompoundTag tag) {
        this.update(pos, NbtOps.INSTANCE, tag);
    }

    @Redirect(method = "<init>", at = @At(value = "NEW", target = "net/minecraft/world/storage/StorageIoWorker"))
    private StorageIoWorker onStorageIoInit(File file, boolean bl, String string) {
        return new C2MECachedRegionStorage(file, bl, string);
    }
}
