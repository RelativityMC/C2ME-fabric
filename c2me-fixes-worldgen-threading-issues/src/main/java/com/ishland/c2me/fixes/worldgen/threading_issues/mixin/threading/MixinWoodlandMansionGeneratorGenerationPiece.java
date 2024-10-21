package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import com.ishland.c2me.fixes.worldgen.threading_issues.asm.MakeVolatile;
import net.minecraft.structure.WoodlandMansionGenerator;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WoodlandMansionGenerator.GenerationPiece.class)
public class MixinWoodlandMansionGeneratorGenerationPiece {

    @MakeVolatile
    @Shadow public BlockRotation rotation;

    @MakeVolatile
    @Shadow public BlockPos position;

    @MakeVolatile
    @Shadow public String template;

}
