package com.ishland.c2me.compatibility.mixin.betternether;

import net.minecraft.item.BoneMealItem;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import paulevs.betternether.biomes.FloodedDeltas;
import paulevs.betternether.biomes.NetherMagmaLand;
import paulevs.betternether.biomes.NetherSoulPlain;
import paulevs.betternether.biomes.NetherWartForest;
import paulevs.betternether.blocks.BlockStalactite;
import paulevs.betternether.structures.StructureCaves;
import paulevs.betternether.structures.StructureFuncScatter;
import paulevs.betternether.structures.StructureObjScatter;
import paulevs.betternether.structures.StructurePath;
import paulevs.betternether.structures.StructureWorld;
import paulevs.betternether.structures.decorations.StructureCrystal;
import paulevs.betternether.structures.decorations.StructureStalactiteCeil;
import paulevs.betternether.structures.decorations.StructureStalactiteFloor;
import paulevs.betternether.structures.plants.StructureAnchorTreeBranch;
import paulevs.betternether.structures.plants.StructureAnchorTreeRoot;
import paulevs.betternether.structures.plants.StructureGiantMold;
import paulevs.betternether.structures.plants.StructureJellyfishMushroom;
import paulevs.betternether.structures.plants.StructureMedBrownMushroom;
import paulevs.betternether.structures.plants.StructureMedRedMushroom;
import paulevs.betternether.structures.plants.StructureMushroomFir;
import paulevs.betternether.structures.plants.StructureNeonEquisetum;
import paulevs.betternether.structures.plants.StructureNetherCactus;
import paulevs.betternether.structures.plants.StructureNetherSakura;
import paulevs.betternether.structures.plants.StructureNetherSakuraBush;
import paulevs.betternether.structures.plants.StructureRubeusBush;
import paulevs.betternether.structures.plants.StructureScatter;
import paulevs.betternether.structures.plants.StructureSmoker;
import paulevs.betternether.structures.plants.StructureSoulLily;
import paulevs.betternether.structures.plants.StructureSoulVein;
import paulevs.betternether.structures.plants.StructureTwistedVines;
import paulevs.betternether.structures.plants.StructureVanillaMushroom;
import paulevs.betternether.structures.plants.StructureVine;
import paulevs.betternether.structures.plants.StructureWartCap;
import paulevs.betternether.structures.plants.StructureWhisperingGourd;
import paulevs.betternether.structures.plants.StructureWillowBush;
import paulevs.betternether.world.BNWorldGenerator;
import paulevs.betternether.world.BiomeMap;
import paulevs.betternether.world.CityHelper;
import paulevs.betternether.world.structures.piece.CavePiece;
import paulevs.betternether.world.structures.piece.CityPiece;
import paulevs.betternether.world.structures.piece.DestructionPiece;

@Mixin({
        FloodedDeltas.class,
        NetherMagmaLand.class,
        NetherSoulPlain.class,
        NetherWartForest.class,
        BlockStalactite.class,
        BoneMealItem.class, // BoneMealMixin
        ChunkGenerator.class, // ChunkPopulateMixin
        StructureCaves.class,
        StructureFuncScatter.class,
        StructureObjScatter.class,
        StructurePath.class,
        StructureWorld.class,
        StructureCrystal.class,
        StructureStalactiteCeil.class,
        StructureStalactiteFloor.class,
        StructureAnchorTreeBranch.class,
        StructureAnchorTreeRoot.class,
        StructureGiantMold.class,
        StructureJellyfishMushroom.class,
        StructureMedBrownMushroom.class,
        StructureMedRedMushroom.class,
        StructureMushroomFir.class,
        StructureNeonEquisetum.class,
        StructureNetherCactus.class,
        StructureNetherSakura.class,
        StructureNetherSakuraBush.class,
        StructureRubeusBush.class,
        StructureScatter.class,
        StructureSmoker.class,
        StructureSoulLily.class,
        StructureSoulVein.class,
        StructureTwistedVines.class,
        StructureVanillaMushroom.class,
        StructureVine.class,
        StructureWartCap.class,
        StructureWhisperingGourd.class,
        StructureWillowBush.class,
        BNWorldGenerator.class,
        CityHelper.class,
        CavePiece.class,
        CityPiece.class,
        DestructionPiece.class,
        BiomeMap.class
})
public class MixinASMTargets {

}
