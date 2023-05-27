package com.ishland.c2me.base.mixin.access;

import net.minecraft.structure.StructurePiecesList;
import net.minecraft.structure.StructureStart;
import net.minecraft.world.gen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StructureStart.class)
public interface IStructureStart {
    @Accessor
    Structure getStructure();

    @Accessor
    StructurePiecesList getChildren();

    @Accessor
    int getReferences();
}
