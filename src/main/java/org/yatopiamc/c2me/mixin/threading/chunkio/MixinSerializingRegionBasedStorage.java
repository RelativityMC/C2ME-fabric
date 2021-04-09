package org.yatopiamc.c2me.mixin.threading.chunkio;

import com.mojang.serialization.DynamicOps;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.SerializingRegionBasedStorage;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.yatopiamc.c2me.common.threading.chunkio.ISerializingRegionBasedStorage;

@Mixin(SerializingRegionBasedStorage.class)
public abstract class MixinSerializingRegionBasedStorage implements ISerializingRegionBasedStorage {

    @Shadow
    protected abstract <T> void update(ChunkPos pos, DynamicOps<T> dynamicOps, @Nullable T data);

    @Override
    public void update(ChunkPos pos, NbtCompound tag) {
        this.update(pos, NbtOps.INSTANCE, tag);
    }

}
