package com.ishland.c2me.opts.worldgen.vanilla.mixin.structure_weight_sampler;

import com.google.common.collect.Iterators;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import net.minecraft.structure.JigsawJunction;
import net.minecraft.util.math.BlockBox;
import net.minecraft.world.gen.StructureWeightSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(StructureWeightSampler.class)
public abstract class MixinStructureWeightSampler {

    @Shadow @Final private ObjectListIterator<StructureWeightSampler.Piece> pieceIterator;

    @Shadow @Final private ObjectListIterator<JigsawJunction> junctionIterator;

    @Shadow
    private static double getMagnitudeWeight(double x, double y, double z) {
        throw new AbstractMethodError();
    }

    @Shadow
    private static double getStructureWeight(int x, int y, int z, int yy) {
        throw new AbstractMethodError();
    }

    @Unique
    private StructureWeightSampler.Piece[] c2me$pieceArray;

    @Unique
    private JigsawJunction[] c2me$junctionArray;

    @Unique
    private void c2me$initArrays() {
        this.c2me$pieceArray = Iterators.toArray(this.pieceIterator, StructureWeightSampler.Piece.class);
        this.pieceIterator.back(Integer.MAX_VALUE);
        this.c2me$junctionArray = Iterators.toArray(this.junctionIterator, JigsawJunction.class);
        this.junctionIterator.back(Integer.MAX_VALUE);
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

        int i = pos.blockX();
        int j = pos.blockY();
        int k = pos.blockZ();
        double d = 0.0;

        for (StructureWeightSampler.Piece piece : this.c2me$pieceArray) {
            BlockBox blockBox = piece.box();
            int l = piece.groundLevelDelta();
            int m = Math.max(0, Math.max(blockBox.getMinX() - i, i - blockBox.getMaxX()));
            int n = Math.max(0, Math.max(blockBox.getMinZ() - k, k - blockBox.getMaxZ()));
            int o = blockBox.getMinY() + l;
            int p = j - o;

//            int q = switch (piece.terrainAdjustment()) {
//                case NONE -> 0;
//                case BURY, BEARD_THIN -> p;
//                case BEARD_BOX -> Math.max(0, Math.max(o - j, j - blockBox.getMaxY()));
//                case ENCAPSULATE -> Math.max(0, Math.max(blockBox.getMinY() - j, j - blockBox.getMaxY()));
//            };
//
//            d += switch (piece.terrainAdjustment()) {
//                case NONE -> 0.0;
//                case BURY -> getMagnitudeWeight(m, (double)q / 2.0, n);
//                case BEARD_THIN, BEARD_BOX -> getStructureWeight(m, q, n, p) * 0.8;
//                case ENCAPSULATE -> getMagnitudeWeight((double)m / 2.0, (double)q / 2.0, (double)n / 2.0) * 0.8;
//            };

            d += switch (piece.terrainAdjustment()) { // 2 switch statement merged
                case NONE -> 0.0;
                case BURY -> getMagnitudeWeight(m, (double)p / 2.0, n);
                case BEARD_THIN -> getStructureWeight(m, p, n, p) * 0.8;
                case BEARD_BOX -> getStructureWeight(m, Math.max(0, Math.max(o - j, j - blockBox.getMaxY())), n, p) * 0.8;
                case ENCAPSULATE -> getMagnitudeWeight((double)m / 2.0, (double)Math.max(0, Math.max(blockBox.getMinY() - j, j - blockBox.getMaxY())) / 2.0, (double)n / 2.0) * 0.8;
            };
        }

        for (JigsawJunction jigsawJunction : this.c2me$junctionArray) {
            int r = i - jigsawJunction.getSourceX();
            int l = j - jigsawJunction.getSourceGroundY();
            int m = k - jigsawJunction.getSourceZ();
            d += getStructureWeight(r, l, m, l) * 0.4;
        }

        return d;
    }

}
