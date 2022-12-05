package com.ishland.c2me.threading.chunkio.mixin;

import com.ishland.c2me.threading.chunkio.common.ISerializingRegionBasedStorage;
import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryOps;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.SerializingRegionBasedStorage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SerializingRegionBasedStorage.class)
public abstract class MixinSerializingRegionBasedStorage implements ISerializingRegionBasedStorage {

    @Shadow
    protected abstract <T> void update(ChunkPos pos, DynamicOps<T> dynamicOps, @Nullable T data);

    @Shadow @Final private DynamicRegistryManager dynamicRegistryManager;

    @Override
    public void update(ChunkPos pos, NbtCompound tag) {
        this.update(pos, RegistryOps.of(NbtOps.INSTANCE, this.dynamicRegistryManager), tag);
    }

}
