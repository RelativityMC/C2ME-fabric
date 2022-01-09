package com.ishland.c2me.mixin.optimization.reduce_allocs.chunkserialization;

import net.minecraft.structure.StructurePiecesList;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StructureStart.class)
public interface StructureStartAccessor {
    @Accessor
    StructureFeature<?> getFeature();

    @Accessor
    StructurePiecesList getChildren();

    @Accessor
    ChunkPos getPos();

    @Accessor
    int getReferences();
}
