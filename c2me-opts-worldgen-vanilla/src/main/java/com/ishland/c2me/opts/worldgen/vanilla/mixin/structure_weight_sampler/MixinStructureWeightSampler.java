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
    private static double getStructureWeight(int x, int y, int z, int yy) {
        throw new AbstractMethodError();
    }

    @Shadow @Final private @Nullable BlockBox boundingBox;
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
    public double sample(DensityFunction.NoisePos pos) {
        if (this.boundingBox == null) {
            return 0.0;
        }

        int x = pos.blockX();
        int y = pos.blockY();
        int z = pos.blockZ();

        if (!this.boundingBox.contains(x, y, z)) {
            return 0.0;
        }

        if (this.c2me$pieceArray == null || this.c2me$junctionArray == null) {
            this.c2me$initArrays();
        }
        double d = 0.0;


        for (StructureWeightSampler.Piece piece : this.c2me$pieceArray) {
            BlockBox blockBox = piece.box();
            int m = Math.max(0, Math.max(blockBox.getMinX() - x, x - blockBox.getMaxX()));
            int n = Math.max(0, Math.max(blockBox.getMinZ() - z, z - blockBox.getMaxZ()));
            int o = blockBox.getMinY() + piece.groundLevelDelta();
            int p = y - o;

            d += switch (piece.terrainAdjustment()) { // 2 switch statement merged
                case NONE -> 0.0;
                case BURY -> getMagnitudeWeight(m, (double)p / 2.0, n);
                case BEARD_THIN -> getStructureWeight(m, p, n, p) * 0.8;
                case BEARD_BOX -> getStructureWeight(m, Math.max(0, Math.max(o - y, y - blockBox.getMaxY())), n, p) * 0.8;
                case ENCAPSULATE -> getMagnitudeWeight((double)m / 2.0, (double)Math.max(0, Math.max(blockBox.getMinY() - y, y - blockBox.getMaxY())) / 2.0, (double)n / 2.0) * 0.8;
            };
        }

        for (JigsawJunction jigsawJunction : this.c2me$junctionArray) {
            int r = x - jigsawJunction.getSourceX();
            int l = y - jigsawJunction.getSourceGroundY();
            int m = z - jigsawJunction.getSourceZ();
            d += getStructureWeight(r, l, m, l) * 0.4;
        }

        return d;
    }

    /**
     * @author ishland
     * @reason optimize impl
     */
    @Overwrite
    private static double getMagnitudeWeight(double x, double y, double z) {
        double d = Math.sqrt(x * x + y * y + z * z);
        if (d > 6.0) {
            return 0.0;
        } else {
            return 1.0 - d / 6.0;
        }
    }

}
