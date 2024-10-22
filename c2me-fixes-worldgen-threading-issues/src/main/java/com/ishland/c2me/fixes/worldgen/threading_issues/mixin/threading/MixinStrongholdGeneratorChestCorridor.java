package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import com.ishland.c2me.fixes.worldgen.threading_issues.asm.MakeVolatile;
import net.minecraft.structure.StrongholdGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(StrongholdGenerator.ChestCorridor.class)
public class MixinStrongholdGeneratorChestCorridor {

    @MakeVolatile
    @Shadow private boolean chestGenerated;

}
