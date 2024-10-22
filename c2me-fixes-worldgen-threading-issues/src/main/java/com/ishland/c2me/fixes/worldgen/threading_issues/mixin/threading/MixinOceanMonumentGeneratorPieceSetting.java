package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import com.ishland.c2me.fixes.worldgen.threading_issues.asm.MakeVolatile;
import net.minecraft.structure.OceanMonumentGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(OceanMonumentGenerator.PieceSetting.class)
public class MixinOceanMonumentGeneratorPieceSetting {

    @MakeVolatile
    @Shadow private boolean used;

    @MakeVolatile
    @Shadow private boolean field_14484;

    @MakeVolatile
    @Shadow private int field_14483;

}
