package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.chunk.ChunkGenerationStep;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.function.Supplier;

@Mixin(ChunkRegion.class)
public class MixinChunkRegion {

    @Shadow
    @Final
    private static Logger LOGGER;

    @Shadow
    @Final
    private ChunkGenerationStep generationStep;

    @Shadow
    private @Nullable Supplier<String> currentlyGeneratingStructureName;

    @ModifyVariable(method = "breakBlock", at = @At("HEAD"), argsOnly = true)
    private boolean preventDropItem(final boolean drop, final BlockPos pos, final boolean drop1, final @Nullable Entity breakingEntity, final int maxUpdateDepth) {
        if (drop) {
            LOGGER.error("Detected breakBlock item drop on pos {}, status: {}, currently generating: {}",
                    pos, this.generationStep.targetStatus(), this.currentlyGeneratingStructureName == null ? "unknown": this.currentlyGeneratingStructureName.get());
        }
        return false;
    }

}
