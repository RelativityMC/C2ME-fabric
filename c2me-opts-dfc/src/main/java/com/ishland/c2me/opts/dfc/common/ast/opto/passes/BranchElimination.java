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

package com.ishland.c2me.opts.dfc.common.ast.opto.passes;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.AstTransformer;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantF64Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.IntervalSelectF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.IntervalSelectF64Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.RangeChoiceF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.RangeChoiceF64Node;

public class BranchElimination implements AstTransformer {

    public static final BranchElimination INSTANCE = new BranchElimination();

    private BranchElimination() {
    }

    @Override
    public AstNode transform(AstNode astNode) {
        return switch (astNode) {
            case RangeChoiceF32Node rangeChoiceF32Node -> {
                if (rangeChoiceF32Node.input instanceof ConstantF32Node c) {
                    if (c.getValue() >= rangeChoiceF32Node.minInclusive && c.getValue() < rangeChoiceF32Node.maxExclusive) {
                        yield rangeChoiceF32Node.whenInRange;
                    } else {
                        yield rangeChoiceF32Node.whenOutOfRange;
                    }
                }

                if (rangeChoiceF32Node.whenInRange.equals(rangeChoiceF32Node.whenOutOfRange)) {
                    yield rangeChoiceF32Node.whenInRange;
                }

                yield rangeChoiceF32Node;
            }
            case RangeChoiceF64Node rangeChoiceF64Node -> {
                if (rangeChoiceF64Node.input instanceof ConstantF64Node c) {
                    if (c.getValue() >= rangeChoiceF64Node.minInclusive && c.getValue() < rangeChoiceF64Node.maxExclusive) {
                        yield rangeChoiceF64Node.whenInRange;
                    } else {
                        yield rangeChoiceF64Node.whenOutOfRange;
                    }
                }

                if (rangeChoiceF64Node.whenInRange.equals(rangeChoiceF64Node.whenOutOfRange)) {
                    yield rangeChoiceF64Node.whenInRange;
                }

                yield rangeChoiceF64Node;
            }
            case IntervalSelectF32Node intervalSelectF32Node -> {
                if (intervalSelectF32Node.input instanceof ConstantF32Node c) {
                    float[] thresholds = intervalSelectF32Node.thresholds;
                    for (int i = 0, thresholdsLength = thresholds.length; i < thresholdsLength; i++) {
                        float threshold = thresholds[i];
                        if (c.getValue() < threshold) {
                            yield intervalSelectF32Node.functions[i];
                        }
                    }
                    yield intervalSelectF32Node.functions[intervalSelectF32Node.functions.length - 1];
                }

                yield intervalSelectF32Node;
            }
            case IntervalSelectF64Node intervalSelectF64Node -> {
                if (intervalSelectF64Node.input instanceof ConstantF64Node c) {
                    double[] thresholds = intervalSelectF64Node.thresholds;
                    for (int i = 0, thresholdsLength = thresholds.length; i < thresholdsLength; i++) {
                        double threshold = thresholds[i];
                        if (c.getValue() < threshold) {
                            yield intervalSelectF64Node.functions[i];
                        }
                    }
                    yield intervalSelectF64Node.functions[intervalSelectF64Node.functions.length - 1];
                }

                yield intervalSelectF64Node;
            }
            default -> astNode;
        };
    }

}
