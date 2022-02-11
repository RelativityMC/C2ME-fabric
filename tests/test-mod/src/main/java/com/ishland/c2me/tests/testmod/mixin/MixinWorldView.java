package com.ishland.c2me.tests.testmod.mixin;

import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.WorldView;
import net.minecraft.world.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorldView.class)
public interface MixinWorldView {

    @Shadow RegistryEntry<Biome> getGeneratorStoredBiome(int biomeX, int biomeY, int biomeZ);

    /**
     * @author ishland
     * @reason async biome locate
     */
    @Overwrite
    default RegistryEntry<Biome> getBiomeForNoiseGen(int biomeX, int biomeY, int biomeZ) {
        return this.getGeneratorStoredBiome(biomeX, biomeY, biomeZ);
    }

}
