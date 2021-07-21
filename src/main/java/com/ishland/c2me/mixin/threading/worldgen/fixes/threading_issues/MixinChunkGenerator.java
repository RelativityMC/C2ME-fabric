package com.ishland.c2me.mixin.threading.worldgen.fixes.threading_issues;

import net.minecraft.util.math.ChunkPos;
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
public class MixinChunkGenerator {

    @Mutable
    @Shadow @Final private List<ChunkPos> strongholds;

    @Inject(method = "<init>(Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/biome/source/BiomeSource;Lnet/minecraft/world/gen/chunk/StructuresConfig;J)V", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        this.strongholds = Collections.synchronizedList(strongholds);
    }

}
