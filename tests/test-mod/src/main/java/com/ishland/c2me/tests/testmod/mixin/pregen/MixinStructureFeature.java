package com.ishland.c2me.tests.testmod.mixin.pregen;

import net.minecraft.world.gen.structure.StructureType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(StructureType.class)
public class MixinStructureFeature {

//    @Redirect(method = "locateStructure", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/WorldView;getBiomeAccess()Lnet/minecraft/world/biome/source/BiomeAccess;"), require = 0)
//    private BiomeAccess redirectBiomeAccess(WorldView worldView) {
//        if (worldView instanceof ServerWorld serverWorld) {
//            return new BiomeAccess(serverWorld.getChunkManager().getChunkGenerator().getBiomeSource(), serverWorld.getSeed(), serverWorld.getDimension().getBiomeAccessType());
//        }
//        return worldView.getBiomeAccess();
//    }

}
