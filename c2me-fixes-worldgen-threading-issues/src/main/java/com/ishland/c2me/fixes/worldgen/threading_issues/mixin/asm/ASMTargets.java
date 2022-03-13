package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.asm;

import net.minecraft.structure.MineshaftGenerator;
import net.minecraft.structure.NetherFortressGenerator;
import net.minecraft.structure.OceanMonumentGenerator;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.ShiftableStructurePiece;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.SwampHutGenerator;
import net.minecraft.structure.WoodlandMansionGenerator;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = {
        MineshaftGenerator.MineshaftCorridor.class,
        NetherFortressGenerator.BridgePlatform.class,
        NetherFortressGenerator.CorridorLeftTurn.class,
        NetherFortressGenerator.CorridorRightTurn.class,
        NetherFortressGenerator.Start.class,
        OceanMonumentGenerator.PieceSetting.class,
        OceanMonumentGenerator.Base.class,
        PoolStructurePiece.class,
        StrongholdGenerator.ChestCorridor.class,
        StrongholdGenerator.PortalRoom.class,
        StrongholdGenerator.Start.class,
        ShiftableStructurePiece.class,
        SwampHutGenerator.class,
        WoodlandMansionGenerator.GenerationPiece.class,
})
public class ASMTargets {
}
