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

package com.ishland.c2me.opts.dfc.common.ast;

import com.ishland.c2me.opts.dfc.common.Config;
import com.ishland.c2me.opts.dfc.common.ast.binary.AddNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.DivNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MaxNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MaxShortNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MinNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MinShortNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MulNode;
import com.ishland.c2me.opts.dfc.common.ast.conversion.ToF64Node;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.*;
import com.ishland.c2me.opts.dfc.common.ast.integration.tectonic.ConfigClampBindings;
import com.ishland.c2me.opts.dfc.common.ast.integration.tectonic.ConfigNoiseBindings;
import com.ishland.c2me.opts.dfc.common.ast.misc.BeardifierNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.CacheLikeNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.CoordinateNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.DelegateNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.EndIslandsNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.FindTopSurfaceNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.InterpolatedNoiseSamplerNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.Multi2SingleNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.RangeChoiceNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.YClampedGradientNode;
import com.ishland.c2me.opts.dfc.common.ast.noise.DFTWeirdScaledSamplerNode;
import com.ishland.c2me.opts.dfc.common.ast.noise.GenericShiftedNoiseNode;
import com.ishland.c2me.opts.dfc.common.ast.spline.SplineNormalNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.AbsNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.CubeNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.NegMulNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.SquareNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.SqueezeNode;
import com.ishland.c2me.opts.dfc.common.ducks.IFastCacheLike;
import net.minecraft.util.math.Spline;
import net.minecraft.util.math.noise.InterpolatedNoiseSampler;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class McToAst {

    private static final Logger LOGGER = LoggerFactory.getLogger(McToAst.class);
    public static final FrontendRegistry<AstEmitter<? extends DensityFunction>> REGISTRY = new FrontendRegistry<>();
    private static final ConcurrentHashMap<Class<?>, AtomicLong> delegateStatistics = new ConcurrentHashMap<>();

    static {
        REGISTRY.registerExactMatch(ChunkNoiseSampler.BlendAlphaDensityFunction.class, f -> new ConstantNode(1.0));
        REGISTRY.registerExactMatch(ChunkNoiseSampler.BlendOffsetDensityFunction.class, f -> new ConstantNode(0.0));
        REGISTRY.registerExactMatch(DensityFunctionTypes.BlendAlpha.class, f -> new ConstantNode(1.0));
        REGISTRY.registerExactMatch(DensityFunctionTypes.BlendOffset.class, f -> new ConstantNode(0.0));

        {
            AstEmitter<? extends DensityFunctionTypes.BinaryOperationLike> emitter = f -> {
                return switch (f.type()) {
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
            };
            REGISTRY.registerExactMatch(DensityFunctionTypes.BinaryOperation.class, (AstEmitter<DensityFunctionTypes.BinaryOperation>) emitter);
            REGISTRY.registerExactMatch(DensityFunctionTypes.LinearOperation.class, (AstEmitter<DensityFunctionTypes.LinearOperation>) emitter);
        }

        REGISTRY.registerExactMatch(DensityFunctionTypes.BlendDensity.class, f -> toAst(f.input()));
        REGISTRY.registerExactMatch(DensityFunctionTypes.Clamp.class, f -> new MaxNode(new ConstantNode(f.minValue()), new MinNode(new ConstantNode(f.maxValue()), toAst(f.input()))));
        REGISTRY.registerExactMatch(DensityFunctionTypes.Constant.class, f -> new ConstantNode(f.value()));
        REGISTRY.registerExactMatch(DensityFunctionTypes.RegistryEntryHolder.class, f -> toAst(f.function().value()));
        REGISTRY.registerExactMatch(DensityFunctionTypes.UnaryOperation.class, f -> {
            return switch (f.type()) {
                case ABS -> new AbsNode(toAst(f.input()));
                case SQUARE -> new SquareNode(toAst(f.input()));
                case CUBE -> new CubeNode(toAst(f.input()));
                case HALF_NEGATIVE -> new NegMulNode(toAst(f.input()), 0.5);
                case QUARTER_NEGATIVE -> new NegMulNode(toAst(f.input()), 0.25);
                case INVERT -> new DivNode(new ConstantNode(1.0), toAst(f.input()));
                case SQUEEZE -> new SqueezeNode(toAst(f.input()));
            };
        });
        REGISTRY.registerExactMatch(DensityFunctionTypes.RangeChoice.class, f -> new RangeChoiceNode(toAst(f.input()), f.minInclusive(), f.maxExclusive(), toAst(f.whenInRange()), toAst(f.whenOutOfRange())));

        {
            AstEmitter<? extends IFastCacheLike> emitter = f -> {
                return new CacheLikeNode(f, toAst(f.c2me$getDelegate()));
            };
            REGISTRY.registerExactMatch(DensityFunctionTypes.Wrapping.class, (AstEmitter<DensityFunctionTypes.Wrapping>) (Object) emitter);
            REGISTRY.registerExactMatch(ChunkNoiseSampler.Cache2D.class, (AstEmitter<ChunkNoiseSampler.Cache2D>) (Object) emitter);
            REGISTRY.registerExactMatch(ChunkNoiseSampler.CacheOnce.class, (AstEmitter<ChunkNoiseSampler.CacheOnce>) (Object) emitter);
            REGISTRY.registerExactMatch(ChunkNoiseSampler.DensityInterpolator.class, (AstEmitter<ChunkNoiseSampler.DensityInterpolator>) (Object) emitter);
            REGISTRY.registerExactMatch(ChunkNoiseSampler.FlatCache.class, (AstEmitter<ChunkNoiseSampler.FlatCache>) (Object) emitter);
        }

        REGISTRY.registerExactMatch(DensityFunctionTypes.ShiftedNoise.class, f -> {
            return new GenericShiftedNoiseNode(
                    new AddNode(new MulNode(CoordinateNode.AXIS_X, new ConstantNode(f.xzScale())), toAst(f.shiftX())),
                    new AddNode(new MulNode(CoordinateNode.AXIS_Y, new ConstantNode(f.yScale())), toAst(f.shiftY())),
                    new AddNode(new MulNode(CoordinateNode.AXIS_Z, new ConstantNode(f.xzScale())), toAst(f.shiftZ())),
                    f.noise()
            );
        });
        REGISTRY.registerExactMatch(DensityFunctionTypes.Noise.class, f -> {
            return new GenericShiftedNoiseNode(
                    new MulNode(CoordinateNode.AXIS_X, new ConstantNode(f.xzScale())),
                    new MulNode(CoordinateNode.AXIS_Y, new ConstantNode(f.yScale())),
                    new MulNode(CoordinateNode.AXIS_Z, new ConstantNode(f.xzScale())),
                    f.noise()
            );
        });
        REGISTRY.registerExactMatch(DensityFunctionTypes.Shift.class, f -> {
            return new MulNode(
                    new GenericShiftedNoiseNode(
                            new MulNode(CoordinateNode.AXIS_X, new ConstantNode(0.25)),
                            new MulNode(CoordinateNode.AXIS_Y, new ConstantNode(0.25)),
                            new MulNode(CoordinateNode.AXIS_Z, new ConstantNode(0.25)),
                            f.offsetNoise()
                    ),
                    new ConstantNode(4.0)
            );
        });
        REGISTRY.registerExactMatch(DensityFunctionTypes.ShiftA.class, f -> {
            return new MulNode(
                    new GenericShiftedNoiseNode(
                            new MulNode(CoordinateNode.AXIS_X, new ConstantNode(0.25)),
                            new ConstantNode(0.0),
                            new MulNode(CoordinateNode.AXIS_Z, new ConstantNode(0.25)),
                            f.offsetNoise()
                    ),
                    new ConstantNode(4.0)
            );
        });
        REGISTRY.registerExactMatch(DensityFunctionTypes.ShiftB.class, f -> {
            return new MulNode(
                    new GenericShiftedNoiseNode(
                            new MulNode(CoordinateNode.AXIS_Z, new ConstantNode(0.25)),
                            new MulNode(CoordinateNode.AXIS_X, new ConstantNode(0.25)),
                            new ConstantNode(0.0),
                            f.offsetNoise()
                    ),
                    new ConstantNode(4.0)
            );
        });
        REGISTRY.registerExactMatch(DensityFunctionTypes.YClampedGradient.class, f -> new YClampedGradientNode(f.fromY(), f.toY(), f.fromValue(), f.toValue()));
        REGISTRY.registerExactMatch(DensityFunctionTypes.WeirdScaledSampler.class, f -> new DFTWeirdScaledSamplerNode(toAst(f.input()), f.noise(), f.rarityValueMapper()));
//        REGISTRY.registerExactMatch(DensityFunctionTypes.Spline.class, f -> new SplineAstNode(f.spline()));
        REGISTRY.registerExactMatch(DensityFunctionTypes.Spline.class, f -> new Multi2SingleNode(new ToF64Node(toAst(f.spline()))));
        REGISTRY.registerExactMatch(DensityFunctionTypes.FindTopSurface.class, f -> new FindTopSurfaceNode(toAst(f.density()), toAst(f.upperBound()), new ConstantNode(f.lowerBound()), f.cellHeight()));

        // delegate nodes that have specialized OpenCL gen
        REGISTRY.registerExactMatch(DensityFunctionTypes.EndIslands.class, EndIslandsNode::new);
        REGISTRY.registerExactMatch(InterpolatedNoiseSampler.class, InterpolatedNoiseSamplerNode::new);
        REGISTRY.registerExactMatch(DensityFunctionTypes.Beardifier.class, BeardifierNode::new);

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

    public static AstNode toAst(Spline<DensityFunctionTypes.Spline.SplinePos, DensityFunctionTypes.Spline.DensityFunctionWrapper> spline) {
        return switch (spline) {
            case Spline.FixedFloatFunction<DensityFunctionTypes.Spline.SplinePos, DensityFunctionTypes.Spline.DensityFunctionWrapper> f -> new ConstantF32Node(f.value());
            case Spline.Implementation<DensityFunctionTypes.Spline.SplinePos, DensityFunctionTypes.Spline.DensityFunctionWrapper> f -> new SplineNormalNode(
                    toAst(f.locationFunction().function().value()),
                    f.locations().clone(),
                    f.values().stream().map(McToAst::toAst).toArray(AstNode[]::new),
                    f.derivatives().clone()
            );
            default -> throw new IllegalStateException("Unexpected type: " + spline.getClass());
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
