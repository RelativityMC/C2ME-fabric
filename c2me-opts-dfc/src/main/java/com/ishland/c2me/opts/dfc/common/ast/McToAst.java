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
import com.ishland.c2me.opts.dfc.common.ast.binary.MaxShortF32Node;
import com.ishland.c2me.opts.dfc.common.ast.binary.MinNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MinShortF32Node;
import com.ishland.c2me.opts.dfc.common.ast.binary.MulNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.PowNode;
import com.ishland.c2me.opts.dfc.common.ast.conversion.ToF32Node;
import com.ishland.c2me.opts.dfc.common.ast.conversion.ToF64Node;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.*;
import com.ishland.c2me.opts.dfc.common.ast.integration.tectonic.ConfigClampBindings;
import com.ishland.c2me.opts.dfc.common.ast.integration.tectonic.ConfigNoiseBindings;
import com.ishland.c2me.opts.dfc.common.ast.meta.Axis;
import com.ishland.c2me.opts.dfc.common.ast.meta.Tiling;
import com.ishland.c2me.opts.dfc.common.ast.misc.BeardifierNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.CacheLikeF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantF64Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.CoordinateF64Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.DelegateNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.EndIslandsNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.FindTopSurfaceNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.GradientF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.IntervalSelectF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.LerpNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.Multi2SingleNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.RangeChoiceF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.RepositionNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.GenericShiftedF64NoiseNode;
import com.ishland.c2me.opts.dfc.common.ast.spline.SplineNormalNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.AbsNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.CubeNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.LogNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.NegMulF32Node;
import com.ishland.c2me.opts.dfc.common.ast.unary.NegateNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.SignumNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.SqrtNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.SquareNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.SqueezeNode;
import com.ishland.c2me.opts.dfc.common.ducks.IFastCacheLike;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.DistanceMetric;
import net.minecraft.util.math.Spline;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.BinaryOperationDensityFunction;
import net.minecraft.world.gen.densityfunction.BlendDensityFunction;
import net.minecraft.world.gen.densityfunction.ClampDensityFunction;
import net.minecraft.world.gen.densityfunction.ConstantDensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import net.minecraft.world.gen.densityfunction.DistanceToPointDensityFunction;
import net.minecraft.world.gen.densityfunction.EndIslandsDensityFunction;
import net.minecraft.world.gen.densityfunction.FindTopSurfaceDensityFunction;
import net.minecraft.world.gen.densityfunction.GradientDensityFunction;
import net.minecraft.world.gen.densityfunction.IntervalSelectDensityFunction;
import net.minecraft.world.gen.densityfunction.LerpDensityFunction;
import net.minecraft.world.gen.densityfunction.NoiseDensityFunction;
import net.minecraft.world.gen.densityfunction.OffsetDensityFunction;
import net.minecraft.world.gen.densityfunction.PowerDensityFunction;
import net.minecraft.world.gen.densityfunction.RangeChoiceDensityFunction;
import net.minecraft.world.gen.densityfunction.ShiftedNoiseDensityFunction;
import net.minecraft.world.gen.densityfunction.SliceDensityFunction;
import net.minecraft.world.gen.densityfunction.SplineDensityFunction;
import net.minecraft.world.gen.densityfunction.UnaryOperationDensityFunction;
import net.minecraft.world.gen.densityfunction.WrappingDensityFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class McToAst {

    private static final Logger LOGGER = LoggerFactory.getLogger(McToAst.class);
    public static final FrontendRegistry<AstEmitter<? extends DensityFunction>> REGISTRY = new FrontendRegistry<>();
    private static final ConcurrentHashMap<Class<?>, AtomicLong> delegateStatistics = new ConcurrentHashMap<>();

    static {
        REGISTRY.registerExactMatch(ChunkNoiseSampler.BlendAlphaDensityFunction.class, f -> new ConstantF32Node(1.0F));
        REGISTRY.registerExactMatch(ChunkNoiseSampler.BlendOffsetDensityFunction.class, f -> new ConstantF32Node(0.0F));
        REGISTRY.registerExactMatch(BlendDensityFunction.class, f -> {
            return switch (f) {
                case BLEND_ALPHA -> new ConstantF32Node(1.0F);
                case BLEND_OFFSET -> new ConstantF32Node(0.0F);
                case BEARDIFIER -> new BeardifierNode(f);
            };
        });

        REGISTRY.registerExactMatch(BinaryOperationDensityFunction.class, f -> {
            return switch (f.type()) {
                case ADD -> new AddNode(toAst(f.left()), toAst(f.right()));
                case SUB -> new AddNode(toAst(f.left()), new NegateNode(toAst(f.right())));
                case MUL -> new MulNode(toAst(f.left()), toAst(f.right()));
                case DIV -> new DivNode(toAst(f.left()), toAst(f.right()));
                case MIN -> {
                    float rightMin = f.rightMinValue();
                    if (f.left().getRange().getMin() < rightMin) {
                        yield new MinShortF32Node(toAst(f.left()), toAst(f.right()), rightMin);
                    } else {
                        yield new MinNode(toAst(f.left()), toAst(f.right()));
                    }
                }
                case MAX -> {
                    float rightMax = f.rightMaxValue();
                    if (f.left().getRange().getMax() > rightMax) {
                        yield new MaxShortF32Node(toAst(f.left()), toAst(f.right()), rightMax);
                    } else {
                        yield new MaxNode(toAst(f.left()), toAst(f.right()));
                    }
                }
            };
        });

        REGISTRY.registerExactMatch(BinaryOperationDensityFunction.LinearOperation.class, f -> {
            return switch (f.specificType()) {
                case MUL -> new MulNode(new ConstantF32Node(f.leftValue()), toAst(f.right()));
                case ADD -> new AddNode(new ConstantF32Node(f.leftValue()), toAst(f.right()));
            };
        });

        REGISTRY.registerExactMatch(PowerDensityFunction.class, f -> new PowNode(toAst(f.base()), toAst(f.exponent())));

        REGISTRY.registerExactMatch(ClampDensityFunction.class, f -> new MaxNode(new ConstantF32Node(f.min()), new MinNode(new ConstantF32Node(f.max()), toAst(f.input()))));
        REGISTRY.registerExactMatch(ConstantDensityFunction.class, f -> new ConstantF32Node(f.value()));
        REGISTRY.registerExactMatch(DensityFunctionTypes.RegistryEntryHolder.class, f -> toAst(f.function().value()));
        REGISTRY.registerExactMatch(UnaryOperationDensityFunction.class, f -> {
            return switch (f.type()) {
                case ABS -> new AbsNode(toAst(f.input()));
                case SQUARE -> new SquareNode(toAst(f.input()));
                case CUBE -> new CubeNode(toAst(f.input()));
                case SQRT -> new SqrtNode(toAst(f.input()));
                case HALF_NEGATIVE -> new NegMulF32Node(toAst(f.input()), 0.5F);
                case QUARTER_NEGATIVE -> new NegMulF32Node(toAst(f.input()), 0.25F);
                case RECIPROCAL -> new DivNode(new ConstantF32Node(1.0F), toAst(f.input()));
                case NEGATE -> new NegateNode(toAst(f.input()));
                case SQUEEZE -> new SqueezeNode(toAst(f.input()));
                case LOG -> new LogNode(toAst(f.input()));
                case SIGN -> new SignumNode(toAst(f.input()));
            };
        });
        REGISTRY.registerExactMatch(RangeChoiceDensityFunction.class, f -> new RangeChoiceF32Node(toAst(f.input()), f.minInclusive(), f.maxExclusive(), toAst(f.whenInRange()), toAst(f.whenOutOfRange())));

        {
            AstEmitter<? extends IFastCacheLike> emitter = f -> {
                if ((Object) f instanceof WrappingDensityFunction wrapping && wrapping.type() == WrappingDensityFunction.Type.BLEND_DENSITY) {
                    return toAst(f.c2me$getDelegate());
                }
                return new CacheLikeF32Node(f, toAst(f.c2me$getDelegate()));
            };
            REGISTRY.registerExactMatch(WrappingDensityFunction.class, (AstEmitter<WrappingDensityFunction>) (Object) emitter);
            REGISTRY.registerExactMatch(ChunkNoiseSampler.Cache2D.class, (AstEmitter<ChunkNoiseSampler.Cache2D>) (Object) emitter);
            REGISTRY.registerExactMatch(ChunkNoiseSampler.CacheOnce.class, (AstEmitter<ChunkNoiseSampler.CacheOnce>) (Object) emitter);
            REGISTRY.registerExactMatch(ChunkNoiseSampler.DensityInterpolator.class, (AstEmitter<ChunkNoiseSampler.DensityInterpolator>) (Object) emitter);
            REGISTRY.registerExactMatch(ChunkNoiseSampler.FlatCache.class, (AstEmitter<ChunkNoiseSampler.FlatCache>) (Object) emitter);
        }

        REGISTRY.registerExactMatch(ShiftedNoiseDensityFunction.class, f -> {
            return new ToF32Node(
                    new GenericShiftedF64NoiseNode(
                            new AddNode(new MulNode(CoordinateF64Node.AXIS_X, new ConstantF64Node(f.xzScale())), new ToF64Node(toAst(f.shiftX()))),
                            new AddNode(new MulNode(CoordinateF64Node.AXIS_Y, new ConstantF64Node(f.yScale())), new ToF64Node(toAst(f.shiftY()))),
                            new AddNode(new MulNode(CoordinateF64Node.AXIS_Z, new ConstantF64Node(f.xzScale())), new ToF64Node(toAst(f.shiftZ()))),
                            f.noise()
                    )
            );
        });
        REGISTRY.registerExactMatch(NoiseDensityFunction.class, f -> {
            return new ToF32Node(
                    new GenericShiftedF64NoiseNode(
                            new MulNode(CoordinateF64Node.AXIS_X, new ConstantF64Node(f.xzScale())),
                            new MulNode(CoordinateF64Node.AXIS_Y, new ConstantF64Node(f.yScale())),
                            new MulNode(CoordinateF64Node.AXIS_Z, new ConstantF64Node(f.xzScale())),
                            f.noise()
                    )
            );
        });
        REGISTRY.registerExactMatch(OffsetDensityFunction.Shift.class, f -> {
            return new MulNode(
                    new ToF32Node(
                            new GenericShiftedF64NoiseNode(
                                    new MulNode(CoordinateF64Node.AXIS_X, new ConstantF64Node(0.25)),
                                    new MulNode(CoordinateF64Node.AXIS_Y, new ConstantF64Node(0.25)),
                                    new MulNode(CoordinateF64Node.AXIS_Z, new ConstantF64Node(0.25)),
                                    f.offsetNoise()
                            )
                    ),
                    new ConstantF32Node(4.0F)
            );
        });
        REGISTRY.registerExactMatch(OffsetDensityFunction.ShiftA.class, f -> {
            return new MulNode(
                    new ToF32Node(
                            new GenericShiftedF64NoiseNode(
                                    new MulNode(CoordinateF64Node.AXIS_X, new ConstantF64Node(0.25)),
                                    new ConstantF64Node(0.0),
                                    new MulNode(CoordinateF64Node.AXIS_Z, new ConstantF64Node(0.25)),
                                    f.offsetNoise()
                            )
                    ),
                    new ConstantF32Node(4.0F)
            );
        });
        REGISTRY.registerExactMatch(OffsetDensityFunction.ShiftB.class, f -> {
            return new MulNode(
                    new ToF32Node(
                            new GenericShiftedF64NoiseNode(
                                    new MulNode(CoordinateF64Node.AXIS_Z, new ConstantF64Node(0.25)),
                                    new MulNode(CoordinateF64Node.AXIS_X, new ConstantF64Node(0.25)),
                                    new ConstantF64Node(0.0),
                                    f.offsetNoise()
                            )
                    ),
                    new ConstantF32Node(4.0F)
            );
        });
//        REGISTRY.registerExactMatch(DensityFunctionTypes.YClampedGradient.class, f -> new YClampedGradientNode(f.fromY(), f.toY(), f.fromValue(), f.toValue()));
        REGISTRY.registerExactMatch(IntervalSelectDensityFunction.class, f -> new IntervalSelectF32Node(toAst(f.input()), f.thresholds().toFloatArray(), f.functions().stream().map(McToAst::toAst).toArray(AstNode[]::new)));
//        REGISTRY.registerExactMatch(DensityFunctionTypes.Spline.class, f -> new SplineAstNode(f.getSpline()));
        REGISTRY.registerExactMatch(SplineDensityFunction.class, f -> new Multi2SingleNode(toAst(f.getSpline())));
        REGISTRY.registerExactMatch(FindTopSurfaceDensityFunction.class, f -> new FindTopSurfaceNode(toAst(f.density()), toAst(f.upperBound()), new ConstantF32Node(f.lowerBound()), f.cellHeight()));
        REGISTRY.registerExactMatch(LerpDensityFunction.class, f -> new LerpNode(toAst(f.alpha()), toAst(f.first()), toAst(f.second())));
        REGISTRY.registerExactMatch(SliceDensityFunction.class, f -> new RepositionNode(
                toAst(f.input()),
                f.axis() == Direction.Axis.X ? new ConstantF64Node(f.coordinate()) : new CoordinateF64Node(Axis.X),
                f.axis() == Direction.Axis.Y ? new ConstantF64Node(f.coordinate()) : new CoordinateF64Node(Axis.Y),
                f.axis() == Direction.Axis.Z ? new ConstantF64Node(f.coordinate()) : new CoordinateF64Node(Axis.Z)
        ));
        REGISTRY.registerExactMatch(DistanceToPointDensityFunction.class, f -> {
            // (float) (((double) point.x) + -((double) x)) should be the same as (float) (point.x - x), where point=vec3i32, x=i32
            AstNode x = new ToF32Node(new AddNode(new ConstantF64Node(f.point().getX()), new NegateNode(new CoordinateF64Node(Axis.X))));
            AstNode y = new ToF32Node(new AddNode(new ConstantF64Node(f.point().getY()), new NegateNode(new CoordinateF64Node(Axis.Y))));
            AstNode z = new ToF32Node(new AddNode(new ConstantF64Node(f.point().getZ()), new NegateNode(new CoordinateF64Node(Axis.Z))));

            return switch (f.metric()) {
                case EUCLIDEAN, EUCLIDEAN_SQUARED -> {
                    // (x * x + y * y) + z * z
                    AstNode squared = new AddNode(
                            new AddNode(
                                    new SquareNode(x),
                                    new SquareNode(y)
                            ),
                            new SquareNode(z)
                    );
                    if (f.metric() == DistanceMetric.EUCLIDEAN) {
                        yield new SqrtNode(squared);
                    } else {
                        yield squared;
                    }
                }
                case MANHATTAN -> {
                    // (abs(x) + abs(y)) + abs(z)
                    yield new AddNode(
                            new AddNode(
                                    new AbsNode(x),
                                    new AbsNode(y)
                            ),
                            new AbsNode(z)
                    );
                }
                case CHEBYSHEV -> {
                    // max(max(abs(x), abs(y)), abs(z))
                    yield new MaxNode(
                            new MaxNode(
                                    new AbsNode(x),
                                    new AbsNode(y)
                            ),
                            new AbsNode(z)
                    );
                }
            };
        });
        REGISTRY.registerExactMatch(GradientDensityFunction.class, f -> new GradientF32Node(
                Axis.fromVanilla(f.axis()),
                Tiling.fromVanilla(f.tiling()),
                f.fromCoordinate(),
                f.toCoordinate(),
                f.fromValue(),
                f.toValue()
        ));

        // TODO DistanceToPoint, GradientDensityFunction, SliceDensityFunction

        // delegate nodes that have specialized OpenCL gen
        REGISTRY.registerExactMatch(EndIslandsDensityFunction.class, EndIslandsNode::new);
//        REGISTRY.registerExactMatch(InterpolatedNoiseSampler.class, InterpolatedNoiseSamplerNode::new);

        if (Config.enableBuiltinIntegrations) {
            AxisBindings.register(REGISTRY);
            CeilBindings.register(REGISTRY);
            CosBindings.register(REGISTRY);
            FastNoiseBindings.register(REGISTRY);
            FloorBindings.register(REGISTRY);
            MixBindings.register(REGISTRY);
            SelectBindings.register(REGISTRY);
            ShiftBindings.register(REGISTRY);
            SinBindings.register(REGISTRY);
            SqrtBindings.register(REGISTRY);
            ConfigClampBindings.register(REGISTRY);
            ConfigNoiseBindings.register(REGISTRY);
        }
    }

    public static AstNode toAst(Spline<SplineDensityFunction.DensityFunctionWrapper> spline) {
        return switch (spline) {
            case Spline.FixedFloatFunction<SplineDensityFunction.DensityFunctionWrapper> f -> new ConstantF32Node(f.value());
            case Spline.Implementation<SplineDensityFunction.DensityFunctionWrapper> f -> new SplineNormalNode(
                    toAst(f.locationFunction().function()),
                    f.locations().clone(),
                    f.values().stream().map(McToAst::toAst).toArray(AstNode[]::new),
                    f.derivatives().clone()
            );
        };
    }

    public static <T extends DensityFunction> AstNode toAst(T df) {
        AstEmitter<T> emitter = (AstEmitter<T>) REGISTRY.getOptional(df.getClass());
        if (emitter != null) {
            return emitter.toAst(df);
        } else {
            long known = delegateStatistics.computeIfAbsent(df.getClass(), unused -> new AtomicLong(0L)).getAndIncrement();
            if (known == 0) {
                LOGGER.warn("warn_once: Generating DelegateNode for type: {}", df.getClass().toString());
            }
            return new DelegateNode(df);
        }
    }

}
