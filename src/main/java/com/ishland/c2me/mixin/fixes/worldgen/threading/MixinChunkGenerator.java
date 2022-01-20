package com.ishland.c2me.mixin.fixes.worldgen.threading;

import com.google.common.collect.Lists;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StrongholdConfig;
import net.minecraft.world.gen.chunk.StructuresConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Random;

@Mixin(ChunkGenerator.class)
public abstract class MixinChunkGenerator {

    @Mutable
    @Shadow @Final private List<ChunkPos> strongholds;

    @Shadow public abstract MultiNoiseUtil.MultiNoiseSampler getMultiNoiseSampler();

    @Shadow @Final private StructuresConfig structuresConfig;

    @Shadow @Final protected BiomeSource populationSource;

    @Shadow
    private static boolean canPlaceStrongholdInBiome(Biome biome) {
        throw new AbstractMethodError();
    }

    @Shadow @Final private long worldSeed;

    @Inject(method = "<init>(Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/gen/chunk/StructuresConfig;J)V", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
//        this.strongholds = Collections.synchronizedList(strongholds);
    }

    /**
     * @author ishland
     * @reason wrapping synchronization around this, TODO try not overwrite
     */
    @Overwrite
    private void generateStrongholdPositions() {
        final List<ChunkPos> strongholds = this.strongholds;
        StrongholdConfig strongholdConfig = this.structuresConfig.getStronghold();
        if (strongholds.isEmpty() && strongholdConfig != null && strongholdConfig.getCount() != 0) {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (strongholds) {
                if (strongholds.isEmpty()) {
                    if (this.strongholds.isEmpty()) {
                        System.out.println("Initializing stronghold positions, this may take a while");
                        // TODO [VanillaCopy]
//                        StrongholdConfig strongholdConfig = this.structuresConfig.getStronghold();
                        List<Biome> list = Lists.<Biome>newArrayList();

                        for(Biome biome : this.populationSource.getBiomes()) {
                            if (canPlaceStrongholdInBiome(biome)) {
                                list.add(biome);
                            }
                        }

                        int i = strongholdConfig.getDistance();
                        int biome = strongholdConfig.getCount();
                        int j = strongholdConfig.getSpread();
                        Random random = new Random();
                        random.setSeed(this.worldSeed);
                        double d = random.nextDouble() * Math.PI * 2.0;
                        int k = 0;
                        int l = 0;

                        for(int m = 0; m < biome; ++m) {
                            double e = (double)(4 * i + i * l * 6) + (random.nextDouble() - 0.5) * (double)i * 2.5;
                            int n = (int)Math.round(Math.cos(d) * e);
                            int o = (int)Math.round(Math.sin(d) * e);
                            BlockPos blockPos = this.populationSource
                                    .locateBiome(
                                            ChunkSectionPos.getOffsetPos(n, 8), 0, ChunkSectionPos.getOffsetPos(o, 8), 112, list::contains, random, this.getMultiNoiseSampler()
                                    );
                            if (blockPos != null) {
                                n = ChunkSectionPos.getSectionCoord(blockPos.getX());
                                o = ChunkSectionPos.getSectionCoord(blockPos.getZ());
                            }

                            this.strongholds.add(new ChunkPos(n, o));
                            d += Math.PI * 2 / (double)j;
                            ++k;
                            if (k == j) {
                                ++l;
                                k = 0;
                                j += 2 * j / (l + 1);
                                j = Math.min(j, biome - m);
                                d += random.nextDouble() * Math.PI * 2.0;
                            }
                        }
                        System.out.println("Stronghold positions initialized");
                    }
                }
            }
        }
    }

}
