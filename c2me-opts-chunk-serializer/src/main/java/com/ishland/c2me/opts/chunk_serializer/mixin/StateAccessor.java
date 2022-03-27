package com.ishland.c2me.opts.chunk_serializer.mixin;

import com.mojang.serialization.MapCodec;
import net.minecraft.state.State;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(State.class)
public interface StateAccessor<S> {
    @Accessor
    MapCodec<S> getCodec();
}
