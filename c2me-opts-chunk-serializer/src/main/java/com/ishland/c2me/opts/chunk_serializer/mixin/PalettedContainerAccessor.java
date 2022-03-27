package com.ishland.c2me.opts.chunk_serializer.mixin;

import net.minecraft.util.collection.IndexedIterable;
import net.minecraft.world.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;
import java.util.Optional;
import java.util.stream.LongStream;


@SuppressWarnings("InterfaceMayBeAnnotatedFunctional")
@Mixin(PalettedContainer.class)
public interface PalettedContainerAccessor<T> {
    @Invoker
    PalettedContainer.Serialized<T> invokeWrite(IndexedIterable<T> idList, PalettedContainer.PaletteProvider provider);

    @Mixin(PalettedContainer.Serialized.class)
    interface PalettedContainerSerializedAccessor<T> {
        @Accessor
        List<T> getPaletteEntries();

        @Accessor
        Optional<LongStream> getStorage();
    }
}
