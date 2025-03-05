package com.ishland.c2me.base.mixin.access;

import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(NbtList.class)
public interface INbtList {

    @Invoker("getValueType")
    byte invokeGetSerializedType();

}
