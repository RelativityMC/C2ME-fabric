package com.ishland.c2me.base.mixin.access;

import com.mojang.serialization.MapCodec;
import net.minecraft.state.State;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(State.class)
public interface IState<S> {
    @Accessor
    MapCodec<S> getCodec();
}
