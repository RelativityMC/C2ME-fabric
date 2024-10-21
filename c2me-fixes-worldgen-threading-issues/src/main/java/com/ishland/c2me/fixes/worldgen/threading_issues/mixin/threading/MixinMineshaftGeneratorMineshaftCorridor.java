package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import com.ishland.c2me.fixes.worldgen.threading_issues.asm.MakeVolatile;
import net.minecraft.structure.MineshaftGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MineshaftGenerator.MineshaftCorridor.class)
public class MixinMineshaftGeneratorMineshaftCorridor {

    @MakeVolatile
    @Shadow private boolean hasSpawner;

}
