package com.ishland.c2me.opts.chunk_serializer.common;

import com.ishland.c2me.opts.chunk_serializer.mixin.PalettedContainerAccessor;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.PalettedContainer;

public final class MixinUtil {
    static <T> PalettedContainerAccessor<T> of(PalettedContainer<T> container) {
        return (PalettedContainerAccessor<T>) container;
    }

    static <T> PalettedContainerAccessor.PalettedContainerSerializedAccessor<T> of(
            PalettedContainer.Serialized<T> container) {
        return (PalettedContainerAccessor.PalettedContainerSerializedAccessor<T>) (Object) container;
    }
}
