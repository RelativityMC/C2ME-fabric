package com.ishland.c2me.base.mixin.access;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(BlockEntity.class)
public interface IBlockEntity {

    @Invoker
    void invokeWriteNbt(NbtCompound nbt);

}
