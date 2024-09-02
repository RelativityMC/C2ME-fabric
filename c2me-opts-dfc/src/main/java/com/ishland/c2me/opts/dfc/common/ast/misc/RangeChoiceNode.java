package com.ishland.c2me.opts.dfc.common.ast.misc;

import com.ishland.c2me.opts.dfc.common.AstTransformer;
import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.EvalType;

import java.util.Objects;

public class RangeChoiceNode implements AstNode {

    private final AstNode input;
    private final double minInclusive;
    private final double maxExclusive;
    private final AstNode whenInRange;
    private final AstNode whenOutOfRange;

    public RangeChoiceNode(AstNode input, double minInclusive, double maxExclusive, AstNode whenInRange, AstNode whenOutOfRange) {
        this.input = Objects.requireNonNull(input);
        this.minInclusive = minInclusive;
        this.maxExclusive = maxExclusive;
        this.whenInRange = Objects.requireNonNull(whenInRange);
        this.whenOutOfRange = Objects.requireNonNull(whenOutOfRange);
    }

    @Override
    public double evalSingle(int x, int y, int z, EvalType type) {
        double v = this.input.evalSingle(x, y, z, type);
        return v >= this.minInclusive && v < this.maxExclusive ? this.whenInRange.evalSingle(x, y, z, type) : this.whenOutOfRange.evalSingle(x, y, z, type);
    }

    @Override
    public void evalMulti(double[] res, int[] x, int[] y, int[] z, EvalType type) {
        this.input.evalMulti(res, x, y, z, type);

        for (int i = 0; i < res.length; i ++) {
            double v = res[i];
            if (v >= this.minInclusive && v < this.maxExclusive) {
                res[i] = this.whenInRange.evalSingle(x[i], y[i], z[i], type);
            } else {
                res[i] = this.whenOutOfRange.evalSingle(x[i], y[i], z[i], type);
            }
        }

//        int numInRange = 0;
//        for (int i = 0; i < x.length; i++) {
//            double v = res[i];
//            if (v >= this.minInclusive && v < this.maxExclusive) {
//                numInRange++;
//            }
//        }
//        int numOutOfRange = res.length - numInRange;
//
//        if (numInRange == 0) {
//            this.whenOutOfRange.evalMulti(res, x, y, z);
//        } else if (numInRange == res.length) {
//            this.whenInRange.evalMulti(res, x, y, z);
//        } else {
//            int idx1 = 0;
//            int[] i1 = new int[numInRange];
//            double[] res1 = new double[numInRange];
//            int[] x1 = new int[numInRange];
//            int[] y1 = new int[numInRange];
//            int[] z1 = new int[numInRange];
//            int idx2 = 0;
//            int[] i2 = new int[numOutOfRange];
//            double[] res2 = new double[numOutOfRange];
//            int[] x2 = new int[numOutOfRange];
//            int[] y2 = new int[numOutOfRange];
//            int[] z2 = new int[numOutOfRange];
//            for (int i = 0; i < res.length; i++) {
//                double v = res[i];
//                if (v >= this.minInclusive && v < this.maxExclusive) {
//                    int index = idx1++;
//                    i1[index] = i;
//                    x1[index] = x[i];
//                    y1[index] = y[i];
//                    z1[index] = z[i];
//                } else {
//                    int index = idx2++;
//                    i2[index] = i;
//                    x2[index] = x[i];
//                    y2[index] = y[i];
//                    z2[index] = z[i];
//                }
//            }
//            this.whenInRange.evalMulti(res1, x1, y1, z1);
//            this.whenOutOfRange.evalMulti(res2, x2, y2, z2);
//            for (int i = 0; i < numInRange; i++) {
//                res[i1[i]] = res1[i];
//            }
//            for (int i = 0; i < numOutOfRange; i++) {
//                res2[i2[i]] = res2[i];
//            }
//        }
    }

    @Override
    public AstNode[] getChildren() {
        return new AstNode[]{this.input, this.whenInRange, this.whenOutOfRange};
    }

    @Override
    public AstNode transform(AstTransformer transformer) {
        AstNode input = this.input.transform(transformer);
        AstNode whenInRange = this.whenInRange.transform(transformer);
        AstNode whenOutOfRange = this.whenOutOfRange.transform(transformer);
        if (this.input == input && this.whenInRange == whenInRange && this.whenOutOfRange == whenOutOfRange) {
            return transformer.transform(this);
        } else {
            return transformer.transform(new RangeChoiceNode(input, minInclusive, maxExclusive, whenInRange, whenOutOfRange));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RangeChoiceNode that = (RangeChoiceNode) o;
        return Double.compare(minInclusive, that.minInclusive) == 0 && Double.compare(maxExclusive, that.maxExclusive) == 0 && Objects.equals(input, that.input) && Objects.equals(whenInRange, that.whenInRange) && Objects.equals(whenOutOfRange, that.whenOutOfRange);
    }

    @Override
    public int hashCode() {
        int result = 1;

        result = 31 * result + this.getClass().hashCode();
        result = 31 * result + this.input.hashCode();
        result = 31 * result + Double.hashCode(this.minInclusive);
        result = 31 * result + Double.hashCode(this.maxExclusive);
        result = 31 * result + this.whenInRange.hashCode();
        result = 31 * result + this.whenOutOfRange.hashCode();

        return result;
    }
}
