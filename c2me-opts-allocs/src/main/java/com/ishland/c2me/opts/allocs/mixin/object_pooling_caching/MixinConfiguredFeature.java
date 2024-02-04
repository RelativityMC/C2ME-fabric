package com.ishland.c2me.opts.allocs.mixin.object_pooling_caching;

import com.ishland.c2me.opts.allocs.common.PooledFeatureContext;
import com.ishland.flowsched.structs.SimpleObjectPool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(ConfiguredFeature.class)
public class MixinConfiguredFeature<FC extends FeatureConfig, F extends Feature<FC>> {

    @Shadow @Final public F feature;

    @Shadow @Final public FC config;

    /**
     * @author ishland
     * @reason pool FeatureContext
     */
    @Overwrite
    public boolean generate(StructureWorldAccess world, ChunkGenerator chunkGenerator, Random random, BlockPos origin) {
        if (!world.isValidForSetBlock(origin)) return false;
        final SimpleObjectPool<PooledFeatureContext<?>> pool = PooledFeatureContext.POOL.get();
        final PooledFeatureContext<FC> context = (PooledFeatureContext<FC>) pool.alloc();
        try {
            context.reInit(Optional.empty(), world, chunkGenerator, random, origin, this.config);
            return this.feature.generate(context);
        } finally {
            pool.release(context);
        }
    }

}
