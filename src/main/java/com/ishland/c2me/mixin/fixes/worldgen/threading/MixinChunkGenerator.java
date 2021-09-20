package com.ishland.c2me.mixin.fixes.worldgen.threading;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.source.util.MultiNoiseUtil;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(ChunkGenerator.class)
public abstract class MixinChunkGenerator {

    @Mutable
    @Shadow @Final private List<ChunkPos> strongholds;

    @Shadow protected abstract void generateStrongholdPositions();

    @Shadow public abstract MultiNoiseUtil.MultiNoiseSampler method_38276();

    @Inject(method = "<init>(Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/gen/chunk/StructuresConfig;J)V", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.strongholds = Collections.synchronizedList(strongholds);
        if (this.method_38276() != null) {
            generateStrongholdPositions(); // early init
        } else {
            System.out.println("Delaying init for stronghold positions");
        }
    }

}
