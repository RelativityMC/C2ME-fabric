package com.ishland.c2me.compatibility.mixin.targets;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = {
        "draylar.horizon.world.RockySurfaceBuilder",
        "draylar.horizon.world.MinersHorizonChunkGenerator"
})
public class ASMMinersHorizonTargets {
}
