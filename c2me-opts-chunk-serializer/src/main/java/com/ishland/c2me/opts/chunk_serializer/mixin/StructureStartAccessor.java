package com.ishland.c2me.opts.chunk_serializer.mixin;

import net.minecraft.structure.StructurePiecesList;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.feature.ConfiguredStructureFeature;
import net.minecraft.world.gen.feature.StructureFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StructureStart.class)
public interface StructureStartAccessor {
    @Accessor
    ConfiguredStructureFeature<?,?> getFeature();

    @Accessor
    StructurePiecesList getChildren();

    @Accessor
    ChunkPos getPos();

    @Accessor
    int getReferences();
}
