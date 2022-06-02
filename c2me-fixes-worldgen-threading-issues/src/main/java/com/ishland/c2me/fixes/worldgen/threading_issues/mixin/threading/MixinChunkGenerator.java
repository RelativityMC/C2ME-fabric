package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.placement.ConcentricRingsStructurePlacement;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Mixin(ChunkGenerator.class)
public abstract class MixinChunkGenerator {

//    @Mutable
//    @Shadow @Final private List<ChunkPos> strongholds;
//
//    @Shadow public abstract MultiNoiseUtil.MultiNoiseSampler getMultiNoiseSampler();
//
//    @Shadow @Final private StructuresConfig structuresConfig;
//
//    @Shadow @Final protected BiomeSource populationSource;
//
//    @Shadow
//    private static boolean canPlaceStrongholdInBiome(Biome biome) {
//        throw new AbstractMethodError();
//    }
//
//    @Shadow @Final private long worldSeed;
//
//    @Shadow protected abstract void generateStrongholdPositions();
//
//    @Inject(method = "<init>(Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/gen/chunk/StructuresConfig;J)V", at = @At("RETURN"))
//    private void onInit(CallbackInfo info) {
////        this.strongholds = Collections.synchronizedList(strongholds);
//    }
//
//
//    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/chunk/ChunkGenerator;generateStrongholdPositions()V"))
//    private void synchronizeGenerateStrongholdPositions(ChunkGenerator instance) {
//        final List<ChunkPos> strongholds = this.strongholds;
//        StrongholdConfig strongholdConfig = this.structuresConfig.getStronghold();
//        if (strongholds.isEmpty() && strongholdConfig != null && strongholdConfig.getCount() != 0) {
//            //noinspection SynchronizationOnLocalVariableOrMethodParameter
//            synchronized (strongholds) {
//                if (strongholds.isEmpty()) {
//                    if (this.strongholds.isEmpty()) {
//                        System.out.println("Initializing stronghold positions, this may take a while");
//                        this.generateStrongholdPositions();
//                        System.out.println("Stronghold positions initialized");
//                    }
//                }
//            }
//        }
//    }

    @Shadow private boolean hasComputedStructurePlacements;

    @Shadow protected abstract void computeStructurePlacements(NoiseConfig arg);

    @Shadow @Final private Map<ConcentricRingsStructurePlacement, CompletableFuture<List<ChunkPos>>> concentricRingPositions;

    /**
     * @author ishland
     * @reason synchronize stronghold position generation
     */
    @Overwrite
    public void computeStructurePlacementsIfNeeded(NoiseConfig arg) {
        if (!this.hasComputedStructurePlacements) {
            synchronized (this) {
                if (!this.hasComputedStructurePlacements) {
                    System.out.println("Initializing stronghold positions, this may take a while");
                    this.computeStructurePlacements(arg);
                    this.hasComputedStructurePlacements = true;
                    CompletableFuture.allOf(this.concentricRingPositions.values().toArray(CompletableFuture[]::new))
                            .thenRun(() -> System.out.println("Stronghold positions initialized"));
                }
            }
        }

    }

}
