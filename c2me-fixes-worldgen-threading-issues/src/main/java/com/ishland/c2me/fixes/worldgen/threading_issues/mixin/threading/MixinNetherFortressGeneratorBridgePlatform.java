package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import com.ishland.c2me.fixes.worldgen.threading_issues.asm.MakeVolatile;
import net.minecraft.structure.NetherFortressGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(NetherFortressGenerator.BridgePlatform.class)
public class MixinNetherFortressGeneratorBridgePlatform {

    @MakeVolatile
    @Shadow private boolean hasBlazeSpawner;

}
