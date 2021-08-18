package com.ishland.c2me.mixin.failsafe;

import com.ishland.c2me.C2MEMod;
import net.minecraft.structure.Structure;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.processor.StructureProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Structure.class)
public class MixinStructure {

    @Redirect(method = "process", at = @At(value = "INVOKE", target = "Lnet/minecraft/structure/processor/StructureProcessor;process(Lnet/minecraft/world/WorldView;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/structure/Structure$StructureBlockInfo;Lnet/minecraft/structure/Structure$StructureBlockInfo;Lnet/minecraft/structure/StructurePlacementData;)Lnet/minecraft/structure/Structure$StructureBlockInfo;"))
    private static Structure.StructureBlockInfo redirectProcess(StructureProcessor structureProcessor, WorldView world, BlockPos pos, BlockPos pivot, Structure.StructureBlockInfo structureBlockInfo, Structure.StructureBlockInfo structureBlockInfo2, StructurePlacementData data) {
        try {
            return structureProcessor.process(world, pos, pivot, structureBlockInfo, structureBlockInfo2, data);
        } catch (Throwable t) {
            C2MEMod.LOGGER.error("An recoverable error is detected by C2ME while generating structure <unknown>", t);
            C2MEMod.LOGGER.error("The structure may have some issues after generating");
            return structureBlockInfo2;
        }
    }

}
