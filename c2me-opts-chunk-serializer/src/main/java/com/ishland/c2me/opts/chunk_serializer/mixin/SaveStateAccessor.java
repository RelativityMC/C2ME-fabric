package com.ishland.c2me.opts.chunk_serializer.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(targets = "ca.spottedleaf.starlight.common.light.SWMRNibbleArray$SaveState")
public interface SaveStateAccessor {
    @Accessor
    byte[] getData();

    @Accessor
    int getState();
}
