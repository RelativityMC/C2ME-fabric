package com.ishland.c2me.asm.targets;

import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.structure.MineshaftGenerator;
import net.minecraft.structure.NetherFortressGenerator;
import net.minecraft.structure.OceanMonumentGenerator;
import net.minecraft.structure.PoolStructurePiece;
import net.minecraft.structure.StrongholdGenerator;
import net.minecraft.structure.StructurePieceWithDimensions;
import net.minecraft.structure.SwampHutGenerator;
import net.minecraft.structure.WoodlandMansionGenerator;
import org.spongepowered.asm.mixin.Mixin;

@Mixin({
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
        StructurePieceWithDimensions.class,
        SwampHutGenerator.class,
        WoodlandMansionGenerator.GenerationPiece.class,
        ServerChunkManager.class
})
public class ASMMixinTargets {
}
