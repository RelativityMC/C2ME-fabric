package com.ishland.c2me.asm.targets;

import net.minecraft.structure.MineshaftGenerator;
import net.minecraft.structure.NetherFortressGenerator;
import net.minecraft.structure.OceanMonumentGenerator;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({
        MineshaftGenerator.MineshaftCorridor.class,
        NetherFortressGenerator.BridgePlatform.class,
        NetherFortressGenerator.CorridorLeftTurn.class,
        NetherFortressGenerator.CorridorRightTurn.class,
        NetherFortressGenerator.Start.class,
        OceanMonumentGenerator.PieceSetting.class,
        OceanMonumentGenerator.Base.class
})
public class ASMMixinTargets {
}
