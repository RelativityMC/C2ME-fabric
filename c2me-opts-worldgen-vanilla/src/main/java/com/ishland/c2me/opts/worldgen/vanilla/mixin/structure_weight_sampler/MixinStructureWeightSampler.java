/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.opts.worldgen.vanilla.mixin.structure_weight_sampler;

import net.minecraft.structure.JigsawJunction;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.gen.StructureWeightSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

@Mixin(StructureWeightSampler.class)
public abstract class MixinStructureWeightSampler {

    @Shadow @Final private List<StructureWeightSampler.Piece> pieces;

    @Shadow @Final private List<JigsawJunction> junctions;

    @Shadow
    private static float getStructureWeight(int x, int y, int z, int yy) {
        throw new AbstractMethodError();
    }

    @Shadow @Final private @Nullable BlockBox boundingBox;

    @Shadow
    private static float getMagnitudeWeight(float dx, float dy, float dz) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Unique
    private StructureWeightSampler.Piece[] c2me$pieceArray;

    @Unique
    private JigsawJunction[] c2me$junctionArray;

    @Unique
    private void c2me$initArrays() {
        this.c2me$pieceArray = this.pieces.toArray(StructureWeightSampler.Piece[]::new);
        this.c2me$junctionArray = this.junctions.toArray(JigsawJunction[]::new);
    }

    /**
     * @author ishland
     * @reason optimize impl
     */
    @Overwrite
    public float sample(final int blockX, final int blockY, final int blockZ) {
        if (this.c2me$pieceArray == null || this.c2me$junctionArray == null) {
            this.c2me$initArrays();
        }

        float d = 0.0F;

        for (StructureWeightSampler.Piece piece : this.c2me$pieceArray) {
            BlockBox blockBox = piece.box();
            int m = Math.max(0, Math.max(blockBox.getMinX() - blockX, blockX - blockBox.getMaxX()));
            int n = Math.max(0, Math.max(blockBox.getMinZ() - blockZ, blockZ - blockBox.getMaxZ()));
            int o = blockBox.getMinY() + piece.groundLevelDelta();
            int p = blockY - o;

            d += switch (piece.terrainAdjustment()) { // 2 switch statement merged
                case NONE -> 0.0F;
                case BURY -> getMagnitudeWeight(m, p / 2.0F, n);
                case BEARD_THIN -> getStructureWeight(m, p, n, p) * 0.8F;
                case BEARD_BOX -> getStructureWeight(m, Math.max(0, Math.max(o - blockY, blockY - blockBox.getMaxY())), n, p) * 0.8F;
                case ENCAPSULATE -> getMagnitudeWeight(m / 2.0F, Math.max(0, Math.max(blockBox.getMinY() - blockY, blockY - blockBox.getMaxY())) / 2.0F, n / 2.0F) * 0.8F;
            };
        }

        for (JigsawJunction jigsawJunction : this.c2me$junctionArray) {
            int r = blockX - jigsawJunction.getSourceX();
            int l = blockY - jigsawJunction.getSourceGroundY();
            int m = blockZ - jigsawJunction.getSourceZ();
            d += getStructureWeight(r, l, m, l) * 0.4F;
        }

        return d;
    }

}
