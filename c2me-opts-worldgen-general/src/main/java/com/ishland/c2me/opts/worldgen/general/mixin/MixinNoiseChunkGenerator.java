package com.ishland.c2me.opts.worldgen.general.mixin;

import com.ishland.c2me.opts.worldgen.general.common.INoiseHeightmapUpdateState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
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

    // the noise loop scans columns from high y to low y, so once a column's surface is
    // established the remaining lower trackUpdate calls cannot change it and are skipped
    @WrapOperation(
            method = "populateNoise(Lnet/minecraft/world/gen/chunk/Blender;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/chunk/Chunk;II)Lnet/minecraft/world/chunk/Chunk;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Heightmap;trackUpdate(IIILnet/minecraft/block/BlockState;)Z"),
            require = 2
    )
    private boolean skipNoiseHeightmapTrackUpdate(Heightmap instance, int x, int y, int z, BlockState state, Operation<Boolean> original, @Local(argsOnly = true) Chunk chunk) {
        long[] doneBits = ((INoiseHeightmapUpdateState) chunk).c2me$noiseHeightmapDoneBits(instance);
        if (doneBits == null) {
            return original.call(instance, x, y, z, state);
        }
        int index = z << 4 | x;
        int word = index >>> 6;
        long mask = 1L << index;
        if ((doneBits[word] & mask) != 0L) {
            return false;
        }
        boolean updated = original.call(instance, x, y, z, state);
        if (updated) {
            doneBits[word] |= mask;
        }
        return updated;
    }

    @Inject(
            method = "populateNoise(Lnet/minecraft/world/gen/chunk/Blender;Lnet/minecraft/world/gen/StructureAccessor;Lnet/minecraft/world/gen/noise/NoiseConfig;Lnet/minecraft/world/chunk/Chunk;II)Lnet/minecraft/world/chunk/Chunk;",
            at = @At("RETURN")
    )
    private void clearHeightmapUpdateState(Blender blender, StructureAccessor structureAccessor, NoiseConfig noiseConfig, Chunk chunk, int minimumCellY, int cellHeight, CallbackInfoReturnable<Chunk> cir) {
        ((INoiseHeightmapUpdateState) chunk).c2me$clearNoiseHeightmapUpdateState();
    }
}
