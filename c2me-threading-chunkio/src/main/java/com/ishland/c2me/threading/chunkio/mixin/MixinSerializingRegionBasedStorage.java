package com.ishland.c2me.threading.chunkio.mixin;

import com.ishland.c2me.threading.chunkio.common.ISerializingRegionBasedStorage;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
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

    @Shadow @Final private DynamicRegistryManager registryManager;

    @Shadow protected abstract void update(ChunkPos pos, RegistryOps<NbtElement> ops, @Nullable NbtCompound nbt);

    @Override
    public void update(ChunkPos pos, NbtCompound tag) {
        this.update(pos, RegistryOps.of(NbtOps.INSTANCE, this.registryManager), tag);
    }

}
