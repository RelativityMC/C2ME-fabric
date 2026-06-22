package com.ishland.c2me.opts.worldgen.vanilla.mixin.structure_weight_sampler;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.structure.JigsawJunction;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.gen.StructureWeightSampler;
import net.minecraft.world.gen.StructureTerrainAdaptation;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(StructureWeightSampler.class)
public abstract class MixinStructureWeightSampler {

    @Unique
    private static final int C2ME$BOUNDS_STRIDE = 6;

    @Unique
    private static final int C2ME$MAGNITUDE_INFLUENCE = 5;

    @Unique
    private static final int C2ME$BEARD_NEGATIVE_INFLUENCE = 12;

    @Unique
    private static final int C2ME$BEARD_POSITIVE_INFLUENCE = 11;

    @Shadow @Final private ObjectListIterator<StructureWeightSampler.Piece> pieceIterator;

    @Shadow @Final private ObjectListIterator<JigsawJunction> junctionIterator;

    @Shadow
    private static double getStructureWeight(int x, int y, int z, int yy) {
        throw new AbstractMethodError();
    }

    @Unique
    private StructureWeightSampler.Piece[] c2me$pieceArray;

    @Unique
    private JigsawJunction[] c2me$junctionArray;

    @Unique
    private int[] c2me$pieceBounds;

    @Unique
    private int[] c2me$junctionBounds;

    @Unique
    private int c2me$minX;

    @Unique
    private int c2me$maxX;

    @Unique
    private int c2me$minY;

    @Unique
    private int c2me$maxY;

    @Unique
    private int c2me$minZ;

    @Unique
    private int c2me$maxZ;

    @Unique
    private boolean c2me$hasInfluence;

    @Unique
    private void c2me$initArrays() {
        this.c2me$pieceArray = Iterators.toArray(this.pieceIterator, StructureWeightSampler.Piece.class);
        this.pieceIterator.back(Integer.MAX_VALUE);
        this.c2me$junctionArray = Iterators.toArray(this.junctionIterator, JigsawJunction.class);
        this.junctionIterator.back(Integer.MAX_VALUE);
        this.c2me$initInfluenceBounds();
    }

    /**
     * @author ishland
     * @reason optimize impl
     */
    @Overwrite
    public double sample(DensityFunction.NoisePos pos) {
        if (this.c2me$pieceArray == null || this.c2me$junctionArray == null) {
            this.c2me$initArrays();
        }

        int j = pos.blockY();
        if (!this.c2me$hasInfluence || j < this.c2me$minY || j > this.c2me$maxY) {
            return 0.0;
        }

        int i = pos.blockX();
        int k = pos.blockZ();
        if (i < this.c2me$minX || i > this.c2me$maxX || k < this.c2me$minZ || k > this.c2me$maxZ) {
            return 0.0;
        }

        double d = 0.0;

        for (int pieceIndex = 0, boundsIndex = 0; pieceIndex < this.c2me$pieceArray.length; ++pieceIndex, boundsIndex += C2ME$BOUNDS_STRIDE) {
            if (c2me$isOutsideBounds(this.c2me$pieceBounds, boundsIndex, i, j, k)) continue;
            StructureWeightSampler.Piece piece = this.c2me$pieceArray[pieceIndex];
            BlockBox blockBox = piece.box();
            int l = piece.groundLevelDelta();
            int m = Math.max(0, Math.max(blockBox.getMinX() - i, i - blockBox.getMaxX()));
            int n = Math.max(0, Math.max(blockBox.getMinZ() - k, k - blockBox.getMaxZ()));
            int o = blockBox.getMinY() + l;
            int p = j - o;

            d += switch (piece.terrainAdjustment()) { // 2 switch statement merged
                case NONE -> 0.0;
                case BURY -> getMagnitudeWeight(m, (double)p / 2.0, n);
                case BEARD_THIN -> getStructureWeight(m, p, n, p) * 0.8;
                case BEARD_BOX -> getStructureWeight(m, Math.max(0, Math.max(o - j, j - blockBox.getMaxY())), n, p) * 0.8;
                case ENCAPSULATE -> getMagnitudeWeight((double)m / 2.0, (double)Math.max(0, Math.max(blockBox.getMinY() - j, j - blockBox.getMaxY())) / 2.0, (double)n / 2.0) * 0.8;
            };
        }

        for (int junctionIndex = 0, boundsIndex = 0; junctionIndex < this.c2me$junctionArray.length; ++junctionIndex, boundsIndex += C2ME$BOUNDS_STRIDE) {
            if (c2me$isOutsideBounds(this.c2me$junctionBounds, boundsIndex, i, j, k)) continue;
            JigsawJunction jigsawJunction = this.c2me$junctionArray[junctionIndex];
            int r = i - jigsawJunction.getSourceX();
            int l = j - jigsawJunction.getSourceGroundY();
            int m = k - jigsawJunction.getSourceZ();
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

    @Unique
    private void c2me$initInfluenceBounds() {
        this.c2me$pieceBounds = new int[this.c2me$pieceArray.length * C2ME$BOUNDS_STRIDE];
        this.c2me$junctionBounds = new int[this.c2me$junctionArray.length * C2ME$BOUNDS_STRIDE];
        this.c2me$resetGlobalBounds();

        for (int i = 0, base = 0; i < this.c2me$pieceArray.length; ++i, base += C2ME$BOUNDS_STRIDE) {
            if (this.c2me$setPieceBounds(this.c2me$pieceArray[i], this.c2me$pieceBounds, base)) {
                this.c2me$includeBounds(this.c2me$pieceBounds, base);
            }
        }

        for (int i = 0, base = 0; i < this.c2me$junctionArray.length; ++i, base += C2ME$BOUNDS_STRIDE) {
            this.c2me$setJunctionBounds(this.c2me$junctionArray[i], this.c2me$junctionBounds, base);
            this.c2me$includeBounds(this.c2me$junctionBounds, base);
        }
    }

    @Unique
    private boolean c2me$setPieceBounds(StructureWeightSampler.Piece piece, int[] bounds, int base) {
        BlockBox box = piece.box();
        StructureTerrainAdaptation adjustment = piece.terrainAdjustment();
        int groundY = box.getMinY() + piece.groundLevelDelta();

        return switch (adjustment) {
            case NONE -> false;
            case BURY -> {
                c2me$setBounds(bounds, base,
                        box.getMinX() - C2ME$MAGNITUDE_INFLUENCE, box.getMaxX() + C2ME$MAGNITUDE_INFLUENCE,
                        groundY - C2ME$BEARD_POSITIVE_INFLUENCE, groundY + C2ME$BEARD_POSITIVE_INFLUENCE,
                        box.getMinZ() - C2ME$MAGNITUDE_INFLUENCE, box.getMaxZ() + C2ME$MAGNITUDE_INFLUENCE);
                yield true;
            }
            case BEARD_THIN -> {
                c2me$setBounds(bounds, base,
                        box.getMinX() - C2ME$BEARD_POSITIVE_INFLUENCE, box.getMaxX() + C2ME$BEARD_POSITIVE_INFLUENCE,
                        groundY - C2ME$BEARD_NEGATIVE_INFLUENCE, groundY + C2ME$BEARD_POSITIVE_INFLUENCE,
                        box.getMinZ() - C2ME$BEARD_POSITIVE_INFLUENCE, box.getMaxZ() + C2ME$BEARD_POSITIVE_INFLUENCE);
                yield true;
            }
            case BEARD_BOX -> {
                c2me$setBounds(bounds, base,
                        box.getMinX() - C2ME$BEARD_POSITIVE_INFLUENCE, box.getMaxX() + C2ME$BEARD_POSITIVE_INFLUENCE,
                        groundY - C2ME$BEARD_POSITIVE_INFLUENCE, box.getMaxY() + C2ME$BEARD_POSITIVE_INFLUENCE,
                        box.getMinZ() - C2ME$BEARD_POSITIVE_INFLUENCE, box.getMaxZ() + C2ME$BEARD_POSITIVE_INFLUENCE);
                yield true;
            }
            case ENCAPSULATE -> {
                c2me$setBounds(bounds, base,
                        box.getMinX() - C2ME$BEARD_POSITIVE_INFLUENCE, box.getMaxX() + C2ME$BEARD_POSITIVE_INFLUENCE,
                        box.getMinY() - C2ME$BEARD_POSITIVE_INFLUENCE, box.getMaxY() + C2ME$BEARD_POSITIVE_INFLUENCE,
                        box.getMinZ() - C2ME$BEARD_POSITIVE_INFLUENCE, box.getMaxZ() + C2ME$BEARD_POSITIVE_INFLUENCE);
                yield true;
            }
        };
    }

    @Unique
    private void c2me$setJunctionBounds(JigsawJunction junction, int[] bounds, int base) {
        c2me$setBounds(bounds, base,
                junction.getSourceX() - C2ME$BEARD_NEGATIVE_INFLUENCE,
                junction.getSourceX() + C2ME$BEARD_POSITIVE_INFLUENCE,
                junction.getSourceGroundY() - C2ME$BEARD_NEGATIVE_INFLUENCE,
                junction.getSourceGroundY() + C2ME$BEARD_POSITIVE_INFLUENCE,
                junction.getSourceZ() - C2ME$BEARD_NEGATIVE_INFLUENCE,
                junction.getSourceZ() + C2ME$BEARD_POSITIVE_INFLUENCE);
    }

    @Unique
    private static void c2me$setBounds(int[] bounds, int base, int minX, int maxX, int minY, int maxY, int minZ, int maxZ) {
        bounds[base] = minX;
        bounds[base + 1] = maxX;
        bounds[base + 2] = minY;
        bounds[base + 3] = maxY;
        bounds[base + 4] = minZ;
        bounds[base + 5] = maxZ;
    }

    @Unique
    private void c2me$resetGlobalBounds() {
        this.c2me$minX = Integer.MAX_VALUE;
        this.c2me$maxX = Integer.MIN_VALUE;
        this.c2me$minY = Integer.MAX_VALUE;
        this.c2me$maxY = Integer.MIN_VALUE;
        this.c2me$minZ = Integer.MAX_VALUE;
        this.c2me$maxZ = Integer.MIN_VALUE;
        this.c2me$hasInfluence = false;
    }

    @Unique
    private void c2me$includeBounds(int[] bounds, int base) {
        this.c2me$minX = Math.min(this.c2me$minX, bounds[base]);
        this.c2me$maxX = Math.max(this.c2me$maxX, bounds[base + 1]);
        this.c2me$minY = Math.min(this.c2me$minY, bounds[base + 2]);
        this.c2me$maxY = Math.max(this.c2me$maxY, bounds[base + 3]);
        this.c2me$minZ = Math.min(this.c2me$minZ, bounds[base + 4]);
        this.c2me$maxZ = Math.max(this.c2me$maxZ, bounds[base + 5]);
        this.c2me$hasInfluence = true;
    }

    @Unique
    private static boolean c2me$isOutsideBounds(int[] bounds, int base, int x, int y, int z) {
        return x < bounds[base] || x > bounds[base + 1]
                || y < bounds[base + 2] || y > bounds[base + 3]
                || z < bounds[base + 4] || z > bounds[base + 5];
    }

}
