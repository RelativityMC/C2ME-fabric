package com.ishland.c2me.opts.worldgen.general.mixin;

import com.ishland.c2me.opts.worldgen.general.common.INoiseHeightmapUpdateState;
import net.minecraft.block.BlockState;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.NoiseChunkGenerator;
import net.minecraft.world.gen.noise.NoiseConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseChunkGenerator.class)
public class MixinNoiseChunkGenerator {

    @Inject(
            method = "populateNoise(Lnet/minecraft/world/gen/chunk/Blender;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/chunk/Chunk;II)Lnet/minecraft/world/chunk/Chunk;",
            at = @At("HEAD")
    )
    private void initHeightmapUpdateState(Blender blender, StructureAccessor structureAccessor, NoiseConfig noiseConfig, Chunk chunk, int minimumCellY, int cellHeight, CallbackInfoReturnable<Chunk> cir) {
        ((INoiseHeightmapUpdateState) chunk).c2me$initNoiseHeightmapUpdateState();
    }

    @Redirect(
            method = "populateNoise(Lnet/minecraft/world/gen/chunk/Blender;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/chunk/Chunk;II)Lnet/minecraft/world/chunk/Chunk;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Heightmap;trackUpdate(IIILnet/minecraft/block/BlockState;)Z"),
            require = 2
    )
    private boolean skipNoiseHeightmapTrackUpdate(Heightmap instance, int x, int y, int z, BlockState state, Blender blender, StructureAccessor structureAccessor, NoiseConfig noiseConfig, Chunk chunk, int minimumCellY, int cellHeight) {
        return ((INoiseHeightmapUpdateState) chunk).c2me$trackNoiseHeightmapUpdate(instance, x, y, z, state);
    }

    @Inject(
            method = "populateNoise(Lnet/minecraft/world/gen/chunk/Blender;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/chunk/Chunk;II)Lnet/minecraft/world/chunk/Chunk;",
            at = @At("RETURN")
    )
    private void clearHeightmapUpdateState(Blender blender, StructureAccessor structureAccessor, NoiseConfig noiseConfig, Chunk chunk, int minimumCellY, int cellHeight, CallbackInfoReturnable<Chunk> cir) {
        ((INoiseHeightmapUpdateState) chunk).c2me$clearNoiseHeightmapUpdateState();
    }
}
