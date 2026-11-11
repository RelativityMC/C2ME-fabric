/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * This file is a part of Concurrent Chunk Management Engine,
 * a mod designed to improve the chunk performance of Minecraft.
 *
 * Copyright (c) 2021-2026 ishland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
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
