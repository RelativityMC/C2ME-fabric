package com.ishland.c2me.mixin.fixes.worldgen.threading;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.StrongholdConfig;
import net.minecraft.world.gen.chunk.StructuresConfig;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

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

    @Shadow protected abstract void generateStrongholdPositions();

    @Inject(method = "<init>(Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/gen/chunk/StructuresConfig;J)V", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
//        this.strongholds = Collections.synchronizedList(strongholds);
    }


    @Redirect(method = "*", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/gen/chunk/ChunkGenerator;generateStrongholdPositions()V"))
    private void synchronizeGenerateStrongholdPositions(ChunkGenerator instance) {
        final List<ChunkPos> strongholds = this.strongholds;
        StrongholdConfig strongholdConfig = this.structuresConfig.getStronghold();
        if (strongholds.isEmpty() && strongholdConfig != null && strongholdConfig.getCount() != 0) {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (strongholds) {
                if (strongholds.isEmpty()) {
                    if (this.strongholds.isEmpty()) {
                        System.out.println("Initializing stronghold positions, this may take a while");
                        this.generateStrongholdPositions();
                        System.out.println("Stronghold positions initialized");
                    }
                }
            }
        }
    }

}
