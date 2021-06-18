package com.ishland.c2me.mixin.util.math;

import com.ishland.c2me.common.util.IBiomeArray;
import net.minecraft.world.biome.source.BiomeArray;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BiomeArray.class)
public class MixinBiomeArray implements IBiomeArray {


    @Shadow @Final private int field_28126; // bottomY

    @Shadow @Final private int field_28127; // (height - 1)

    @Override
    public boolean isWithinRange(int y) {
        return y >= field_28126 && y <= field_28127 + field_28126;
    }
}
