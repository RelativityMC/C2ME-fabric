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

package com.ishland.c2me.opts.dfc.common.ast;

import com.ishland.c2me.opts.dfc.common.Config;
import com.ishland.c2me.opts.dfc.common.ast.binary.AddNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.DivNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MaxNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MaxShortNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MinNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MinShortNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MulNode;
import com.ishland.c2me.opts.dfc.common.ast.integration.tectonic.ConfigClampBindings;
import com.ishland.c2me.opts.dfc.common.ast.integration.tectonic.ConfigNoiseBindings;
import com.ishland.c2me.opts.dfc.common.ast.misc.BeardifierNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.CacheLikeNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.CoordinateNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.DelegateNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.EndIslandsNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.FindTopSurfaceNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.InterpolatedNoiseSamplerNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.IntervalSelectNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.RangeChoiceNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.YClampedGradientNode;
import com.ishland.c2me.opts.dfc.common.ast.noise.GenericShiftedNoiseNode;
import com.ishland.c2me.opts.dfc.common.ast.opto.OptoPasses;
import com.ishland.c2me.opts.dfc.common.ast.spline.SplineAstNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.AbsNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.CubeNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.NegMulNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.SquareNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.SqueezeNode;
import com.ishland.c2me.opts.dfc.common.ducks.IFastCacheLike;
import com.ishland.c2me.opts.dfc.common.gen.jvm.BytecodeGen;
import com.ishland.c2me.opts.dfc.common.gen.jvm.CompiledDensityFunction;
import net.minecraft.util.math.noise.InterpolatedNoiseSampler;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

public class McToAst {

    private static final Logger LOGGER = LoggerFactory.getLogger(McToAst.class);
    private static final ConcurrentHashMap<Class<?>, AtomicLong> delegateStatistics = new ConcurrentHashMap<>();

    public static AstNode toAst(DensityFunction df) {
        Objects.requireNonNull(df);
        return switch (df) {
            case ChunkNoiseSampler.BlendAlphaDensityFunction f -> new ConstantNode(1.0);
            case ChunkNoiseSampler.BlendOffsetDensityFunction f -> new ConstantNode(0.0);
            case DensityFunctionTypes.BlendAlpha f -> new ConstantNode(1.0);
            case DensityFunctionTypes.BlendOffset f -> new ConstantNode(0.0);
            case DensityFunctionTypes.BinaryOperationLike f -> switch (f.type()) {
                case ADD -> new AddNode(toAst(f.argument1()), toAst(f.argument2()));
                case MUL -> new MulNode(toAst(f.argument1()), toAst(f.argument2()));
                case MIN -> {
                    double rightMin = f.argument2().minValue();
                    if (f.argument1().minValue() < rightMin) {
                        yield new MinShortNode(toAst(f.argument1()), toAst(f.argument2()), rightMin);
                    } else {
                        yield new MinNode(toAst(f.argument1()), toAst(f.argument2()));
                    }
                }
                case MAX -> {
                    double rightMax = f.argument2().maxValue();
                    if (f.argument1().maxValue() > rightMax) {
                        yield new MaxShortNode(toAst(f.argument1()), toAst(f.argument2()), rightMax);
                    } else {
                        yield new MaxNode(toAst(f.argument1()), toAst(f.argument2()));
                    }
                }
            };
            case DensityFunctionTypes.Clamp f -> new MaxNode(new ConstantNode(f.minValue()), new MinNode(new ConstantNode(f.maxValue()), toAst(f.input())));
            case DensityFunctionTypes.Constant f -> new ConstantNode(f.value());
            case DensityFunctionTypes.RegistryEntryHolder f -> toAst(f.function().value());
            case DensityFunctionTypes.UnaryOperation f -> switch (f.type()) {
                case ABS -> new AbsNode(toAst(f.input()));
                case SQUARE -> new SquareNode(toAst(f.input()));
                case CUBE -> new CubeNode(toAst(f.input()));
                case HALF_NEGATIVE -> new NegMulNode(toAst(f.input()), 0.5);
                case QUARTER_NEGATIVE -> new NegMulNode(toAst(f.input()), 0.25);
                case INVERT -> new DivNode(new ConstantNode(1.0), toAst(f.input()));
                case SQUEEZE -> new SqueezeNode(toAst(f.input()));
            };
            case DensityFunctionTypes.RangeChoice f -> new RangeChoiceNode(toAst(f.input()), f.minInclusive(), f.maxExclusive(), toAst(f.whenInRange()), toAst(f.whenOutOfRange()));
            case IFastCacheLike f -> {
                if ((Object) f instanceof DensityFunctionTypes.Wrapping wrapping && wrapping.type() == DensityFunctionTypes.Wrapping.Type.BLEND_DENSITY) {
                    yield toAst(f.c2me$getDelegate());
                }
                yield new CacheLikeNode(f, toAst(f.c2me$getDelegate()));
            }
            case DensityFunctionTypes.ShiftedNoise f -> new GenericShiftedNoiseNode(
                    new AddNode(new MulNode(CoordinateNode.AXIS_X, new ConstantNode(f.xzScale())), toAst(f.shiftX())),
                    new AddNode(new MulNode(CoordinateNode.AXIS_Y, new ConstantNode(f.yScale())), toAst(f.shiftY())),
                    new AddNode(new MulNode(CoordinateNode.AXIS_Z, new ConstantNode(f.xzScale())), toAst(f.shiftZ())),
                    f.noise()
            );
            case DensityFunctionTypes.Noise f -> new GenericShiftedNoiseNode(
                    new MulNode(CoordinateNode.AXIS_X, new ConstantNode(f.xzScale())),
                    new MulNode(CoordinateNode.AXIS_Y, new ConstantNode(f.yScale())),
                    new MulNode(CoordinateNode.AXIS_Z, new ConstantNode(f.xzScale())),
                    f.noise()
            );
            case DensityFunctionTypes.Shift f -> new MulNode(
                    new GenericShiftedNoiseNode(
                            new MulNode(CoordinateNode.AXIS_X, new ConstantNode(0.25)),
                            new MulNode(CoordinateNode.AXIS_Y, new ConstantNode(0.25)),
                            new MulNode(CoordinateNode.AXIS_Z, new ConstantNode(0.25)),
                            f.offsetNoise()
                    ),
                    new ConstantNode(4.0)
            );
            case DensityFunctionTypes.ShiftA f -> new MulNode(
                    new GenericShiftedNoiseNode(
                            new MulNode(CoordinateNode.AXIS_X, new ConstantNode(0.25)),
                            new ConstantNode(0.0),
                            new MulNode(CoordinateNode.AXIS_Z, new ConstantNode(0.25)),
                            f.offsetNoise()
                    ),
                    new ConstantNode(4.0)
            );
            case DensityFunctionTypes.ShiftB f -> new MulNode(
                    new GenericShiftedNoiseNode(
                            new MulNode(CoordinateNode.AXIS_Z, new ConstantNode(0.25)),
                            new MulNode(CoordinateNode.AXIS_X, new ConstantNode(0.25)),
                            new ConstantNode(0.0),
                            f.offsetNoise()
                    ),
                    new ConstantNode(4.0)
            );
            case DensityFunctionTypes.YClampedGradient f -> new YClampedGradientNode(f.fromY(), f.toY(), f.fromValue(), f.toValue());
            case DensityFunctionTypes.IntervalSelect f -> new IntervalSelectNode(toAst(f.input()), f.thresholds().toDoubleArray(), f.functions().stream().map(McToAst::toAst).toArray(AstNode[]::new));
            case DensityFunctionTypes.Spline f -> new SplineAstNode(f.getSpline());
            case DensityFunctionTypes.FindTopSurface f -> new FindTopSurfaceNode(toAst(f.density()), toAst(f.upperBound()), new ConstantNode(f.lowerBound()), f.cellHeight());

            // delegate nodes that have specialized OpenCL gen
            case DensityFunctionTypes.EndIslands f -> new EndIslandsNode(f);
            case InterpolatedNoiseSampler f -> new InterpolatedNoiseSamplerNode(f);
            case DensityFunctionTypes.Beardifier f -> new BeardifierNode(f);

            default -> {
                if (Config.enableBuiltinIntegrations) {
                    {
                        AstNode node = ConfigClampBindings.tryParse(df);
                        if (node != null) yield node;
                    }

                    {
                        AstNode node = ConfigNoiseBindings.tryParse(df);
                        if (node != null) yield node;
                    }
                }

                long known = delegateStatistics.computeIfAbsent(df.getClass(), unused -> new AtomicLong(0L)).getAndIncrement();
                if (known == 0) {
                    LOGGER.warn("warn_once: Generating DelegateNode for type: {}", df.getClass().toString());
                }
                yield new DelegateNode(df);
            }
        };
    }

}
