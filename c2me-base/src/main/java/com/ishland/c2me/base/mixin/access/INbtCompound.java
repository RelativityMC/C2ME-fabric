package com.ishland.c2me.base.mixin.access;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(NbtCompound.class)
public interface INbtCompound {

    @Invoker
    Map<String, NbtElement> invokeToMap();

}
