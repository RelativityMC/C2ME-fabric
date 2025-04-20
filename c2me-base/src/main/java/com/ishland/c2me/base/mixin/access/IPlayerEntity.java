package com.ishland.c2me.base.mixin.access;

import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PlayerEntity.class)
public interface IPlayerEntity {

    @Accessor
    boolean getLoaded();

}
