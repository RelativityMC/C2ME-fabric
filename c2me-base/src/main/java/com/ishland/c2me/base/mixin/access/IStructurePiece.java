package com.ishland.c2me.base.mixin.access;

import net.minecraft.structure.StructurePiece;
import net.minecraft.structure.StructurePieceType;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StructurePiece.class)
public interface IStructurePiece {

    @Accessor
    StructurePieceType getType();

    @Accessor
    BlockBox getBoundingBox();

    @Accessor
    Direction getFacing();

    @Accessor
    BlockMirror getMirror();

    @Accessor
    BlockRotation getRotation();

    @Accessor
    int getChainLength();
}
