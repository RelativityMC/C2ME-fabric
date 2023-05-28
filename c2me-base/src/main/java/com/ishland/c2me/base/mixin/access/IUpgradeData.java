package com.ishland.c2me.base.mixin.access;

import net.minecraft.util.math.EightWayDirection;
import net.minecraft.world.chunk.UpgradeData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.EnumSet;

@Mixin(UpgradeData.class)
public interface IUpgradeData {
    @Accessor
    int[][] getCenterIndicesToUpgrade();

    @Accessor
    EnumSet<EightWayDirection> getSidesToUpgrade();
}
