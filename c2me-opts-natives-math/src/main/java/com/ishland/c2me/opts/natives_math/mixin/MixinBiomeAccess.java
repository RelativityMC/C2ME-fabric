package com.ishland.c2me.opts.natives_math.mixin;

import com.ishland.c2me.opts.natives_math.common.Bindings;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeAccess;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(BiomeAccess.class)
public class MixinBiomeAccess {

    @Shadow @Final private long seed;

    @Shadow @Final private BiomeAccess.Storage storage;

    /**
     * @author ishland
     * @reason replace impl
     */
    @Overwrite
    public RegistryEntry<Biome> getBiome(BlockPos pos) {
        int mask = Bindings.c2me_natives_biome_access_sample(this.seed, pos.getX(), pos.getY(), pos.getZ());

        return this.storage.getBiomeForNoiseGen(
                ((pos.getX() - 2) >> 2) + ((mask & 4) != 0 ? 1 : 0),
                ((pos.getY() - 2) >> 2) + ((mask & 2) != 0 ? 1 : 0),
                ((pos.getZ() - 2) >> 2) + ((mask & 1) != 0 ? 1 : 0)
        );
    }

}
