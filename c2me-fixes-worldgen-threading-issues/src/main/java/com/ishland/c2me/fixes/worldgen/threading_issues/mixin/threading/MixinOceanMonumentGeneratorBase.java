package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import com.ishland.c2me.fixes.worldgen.threading_issues.asm.MakeVolatile;
import net.minecraft.structure.OceanMonumentGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(OceanMonumentGenerator.Base.class)
public class MixinOceanMonumentGeneratorBase {

    @MakeVolatile
    @Shadow private OceanMonumentGenerator.PieceSetting entryPieceSetting;

    @MakeVolatile
    @Shadow private OceanMonumentGenerator.PieceSetting coreRoomPieceSetting;

}
