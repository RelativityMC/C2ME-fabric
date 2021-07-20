package com.ishland.c2me.asm.targets;

import net.minecraft.structure.MineshaftGenerator;
import net.minecraft.structure.NetherFortressGenerator;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({
        MineshaftGenerator.MineshaftCorridor.class,
        NetherFortressGenerator.BridgePlatform.class,
        NetherFortressGenerator.CorridorLeftTurn.class,
        NetherFortressGenerator.CorridorRightTurn.class,
        NetherFortressGenerator.Start.class
})
public class ASMMixinTargets {
}
