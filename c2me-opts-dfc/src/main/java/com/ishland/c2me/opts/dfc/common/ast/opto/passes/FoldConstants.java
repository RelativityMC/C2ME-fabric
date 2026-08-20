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
import com.ishland.c2me.opts.dfc.common.ast.binary.AbstractBinaryNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.AddNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.DivNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MaxNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MaxShortF32Node;
import com.ishland.c2me.opts.dfc.common.ast.binary.MaxShortF64Node;
import com.ishland.c2me.opts.dfc.common.ast.binary.MinNode;
import com.ishland.c2me.opts.dfc.common.ast.binary.MinShortF32Node;
import com.ishland.c2me.opts.dfc.common.ast.binary.MinShortF64Node;
import com.ishland.c2me.opts.dfc.common.ast.binary.MulNode;
import com.ishland.c2me.opts.dfc.common.ast.conversion.ToF32Node;
import com.ishland.c2me.opts.dfc.common.ast.conversion.ToF64Node;
import com.ishland.c2me.opts.dfc.common.ast.integration.lithostitched.misc.MixNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.CacheLikeF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantF32Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantF64Node;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantNodeLike;
import com.ishland.c2me.opts.dfc.common.ast.misc.LerpNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.Multi2SingleNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.RepositionNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.RoundingDFNode;
import com.ishland.c2me.opts.dfc.common.ast.unary.*;
import com.ishland.c2me.opts.dfc.common.gen.jvm.util.ZeroUtils;
import net.minecraft.util.math.MathHelper;

public class FoldConstants implements AstTransformer {

    public static final FoldConstants INSTANCE = new FoldConstants();

    private FoldConstants() {
    }

    @Override
    public AstNode transform(AstNode astNode) {
        if (astNode instanceof AbstractBinaryNode abstractBinaryNode) {
            if (abstractBinaryNode.left instanceof ConstantF32Node left && abstractBinaryNode.right instanceof ConstantF32Node right) {
                return new ConstantF32Node(abstractBinaryNode.computeF32(left.getValue(), right.getValue()));
            }
            if (abstractBinaryNode.left instanceof ConstantF64Node left && abstractBinaryNode.right instanceof ConstantF64Node right) {
                return new ConstantF64Node(abstractBinaryNode.computeF64(left.getValue(), right.getValue()));
            }
        }

        if (astNode instanceof AbstractUnaryNode abstractUnaryNode) {
            if (abstractUnaryNode.operand instanceof ConstantF32Node operand) {
                return new ConstantF32Node(abstractUnaryNode.computeF32(operand.getValue()));
            }
            if (abstractUnaryNode.operand instanceof ConstantF64Node operand) {
                return new ConstantF64Node(abstractUnaryNode.computeF64(operand.getValue()));
            }
        }

        return switch (astNode) {
            case AddNode addNode -> {
                // TreeNormalization: const left
                if (addNode.left instanceof ConstantF64Node c && c.getValue() == 0.0 && !ZeroUtils.isPositiveZero(c.getValue())) {
                    yield addNode.right;
                }
                if (addNode.left instanceof ConstantF32Node c && c.getValue() == 0.0F && !ZeroUtils.isPositiveZero(c.getValue())) {
                    yield addNode.right;
                }

                yield addNode;
            }
            case DivNode divNode -> {
                if (divNode.left instanceof ConstantF64Node c && c.getValue() == 0.0) { // special case defined in vanilla
                    yield new ConstantF64Node(0.0);
                }
                if (divNode.left instanceof ConstantF32Node c && c.getValue() == 0.0F) { // special case defined in vanilla
                    yield new ConstantF32Node(0.0F);
                }

                yield divNode;
            }
            case MulNode mulNode -> {
                if (mulNode.left instanceof ConstantF64Node c && c.getValue() == 0.0) { // special case defined in vanilla
                    yield new ConstantF64Node(0.0);
                }
                if (mulNode.left instanceof ConstantF32Node c && c.getValue() == 0.0F) { // special case defined in vanilla
                    yield new ConstantF32Node(0.0F);
                }

                // TreeNormalization: const left
                if (mulNode.left instanceof ConstantF64Node c && c.getValue() == 1.0) {
                    yield mulNode.right;
                }
                if (mulNode.left instanceof ConstantF32Node c && c.getValue() == 1.0) {
                    yield mulNode.right;
                }

                yield mulNode;
            }
            case MaxShortF64Node maxShortF64Node -> {
                if (maxShortF64Node.left instanceof ConstantF64Node c1 && c1.getValue() >= maxShortF64Node.rightMax) {
                    yield c1;
                }

                if (maxShortF64Node.left instanceof ConstantF64Node || maxShortF64Node.right instanceof ConstantF64Node) {
                    yield new MaxNode(maxShortF64Node.left, maxShortF64Node.right);
                }

                yield maxShortF64Node;
            }
            case MaxShortF32Node maxShortF32Node -> {
                if (maxShortF32Node.left instanceof ConstantF32Node c1 && c1.getValue() >= maxShortF32Node.rightMax) {
                    yield c1;
                }

                if (maxShortF32Node.left instanceof ConstantF32Node || maxShortF32Node.right instanceof ConstantF32Node) {
                    yield new MaxNode(maxShortF32Node.left, maxShortF32Node.right);
                }

                yield maxShortF32Node;
            }
            case MinShortF64Node minShortF64Node -> {
                if (minShortF64Node.left instanceof ConstantF64Node c1 && c1.getValue() <= minShortF64Node.rightMin) {
                    yield c1;
                }

                if (minShortF64Node.left instanceof ConstantF64Node || minShortF64Node.right instanceof ConstantF64Node) {
                    yield new MinNode(minShortF64Node.left, minShortF64Node.right);
                }

                yield minShortF64Node;
            }
            case MinShortF32Node minShortF32Node -> {
                if (minShortF32Node.left instanceof ConstantF32Node c1 && c1.getValue() <= minShortF32Node.rightMin) {
                    yield c1;
                }

                if (minShortF32Node.left instanceof ConstantF32Node || minShortF32Node.right instanceof ConstantF32Node) {
                    yield new MinNode(minShortF32Node.left, minShortF32Node.right);
                }

                yield minShortF32Node;
            }
            case MixNode mixNode -> {
                if (mixNode.input instanceof ConstantF64Node c1 && mixNode.argument1 instanceof ConstantF64Node c2 && mixNode.argument2 instanceof ConstantF64Node c3) {
                    yield new ConstantF64Node(c2.getValue() * (1.0 - c1.getValue()) + c3.getValue() * c1.getValue());
                }
                if (mixNode.input instanceof ConstantF32Node c1 && mixNode.argument1 instanceof ConstantF32Node c2 && mixNode.argument2 instanceof ConstantF32Node c3) {
                    yield new ConstantF32Node(c2.getValue() * (1.0F - c1.getValue()) + c3.getValue() * c1.getValue());
                }

                if (mixNode.input instanceof ConstantF64Node c1 && mixNode.argument1 instanceof ConstantF64Node c2) {
                    yield new AddNode(new ConstantF64Node(c2.getValue() * (1.0 - c1.getValue())), new MulNode(c1, mixNode.argument2)); // TreeNormalization: const left for consistency
                }
                if (mixNode.input instanceof ConstantF32Node c1 && mixNode.argument1 instanceof ConstantF32Node c2) {
                    yield new AddNode(new ConstantF32Node(c2.getValue() * (1.0F - c1.getValue())), new MulNode(c1, mixNode.argument2)); // TreeNormalization: const left for consistency
                }

                if (mixNode.input instanceof ConstantF64Node c1 && mixNode.argument2 instanceof ConstantF64Node c2) {
                    yield new AddNode(new ConstantF64Node(c2.getValue() * c1.getValue()), new MulNode(new ConstantF64Node(1.0 - c1.getValue()), mixNode.argument1)); // TreeNormalization: const left for consistency
                }
                if (mixNode.input instanceof ConstantF32Node c1 && mixNode.argument2 instanceof ConstantF32Node c2) {
                    yield new AddNode(new ConstantF32Node(c2.getValue() * c1.getValue()), new MulNode(new ConstantF32Node(1.0F - c1.getValue()), mixNode.argument1)); // TreeNormalization: const left for consistency
                }

                if (mixNode.input instanceof ConstantF64Node c1 && c1.getValue() <= 0.0) {
                    yield mixNode.argument1;
                }
                if (mixNode.input instanceof ConstantF32Node c1 && c1.getValue() <= 0.0) {
                    yield mixNode.argument1;
                }

                if (mixNode.input instanceof ConstantF64Node c1 && c1.getValue() >= 1.0) {
                    yield mixNode.argument2;
                }
                if (mixNode.input instanceof ConstantF32Node c1 && c1.getValue() >= 1.0) {
                    yield mixNode.argument2;
                }

                yield mixNode;
            }
            case CacheLikeF32Node cacheLikeF32Node -> {
                if (cacheLikeF32Node.getDelegate() instanceof ConstantF32Node c) { // all registered caches act the same with constant operand, for now
                    yield c;
                }

                yield cacheLikeF32Node;
            }
            case LerpNode lerpNode -> {
                if (lerpNode.delta instanceof ConstantF64Node c) {
                    if (lerpNode.start instanceof ConstantF64Node c1 && lerpNode.end instanceof ConstantF64Node c2) {
                        yield new ConstantF64Node(MathHelper.lerp(c.getValue(), c1.getValue(), c2.getValue()));
                    }
                    if (c.getValue() == 0.0) {
                        yield lerpNode.start;
                    }
                    if (c.getValue() == 1.0) {
                        yield lerpNode.end;
                    }
                }
                if (lerpNode.delta instanceof ConstantF32Node c) {
                    if (lerpNode.start instanceof ConstantF32Node c1 && lerpNode.end instanceof ConstantF32Node c2) {
                        yield new ConstantF32Node(MathHelper.lerp(c.getValue(), c1.getValue(), c2.getValue()));
                    }
                    if (c.getValue() == 0.0F) {
                        yield lerpNode.start;
                    }
                    if (c.getValue() == 1.0F) {
                        yield lerpNode.end;
                    }
                }

                yield lerpNode;
            }
            case RoundingDFNode roundingDFNode -> {
                if (roundingDFNode.multiple instanceof ConstantF64Node multiple) {
                    if (multiple.getValue() == 0.0) {
                        yield roundingDFNode.input;
                    }
                    if (roundingDFNode.input instanceof ConstantF64Node input) {
                        AbstractUnaryNode dummyNode = switch (roundingDFNode.operation) {
                            case FLOOR -> new FloorNode(new ConstantF64Node(0.0));
                            case ROUND_HALF_UP -> new RoundHalfUpNode(new ConstantF64Node(0.0));
                            case CEIL -> new CeilNode(new ConstantF64Node(0.0));
                            case ROUND_TOWARDS_ZERO -> new RoundTowardsZeroNode(new ConstantF64Node(0.0));
                        };
                        yield new ConstantF64Node(
                                dummyNode.computeF64(input.getValue() / multiple.getValue()) * multiple.getValue()
                        );
                    }
                }
                if (roundingDFNode.multiple instanceof ConstantF32Node multiple) {
                    if (multiple.getValue() == 0.0F) {
                        yield roundingDFNode.input;
                    }
                    if (roundingDFNode.input instanceof ConstantF32Node input) {
                        AbstractUnaryNode dummyNode = switch (roundingDFNode.operation) {
                            case FLOOR -> new FloorNode(new ConstantF32Node(0.0F));
                            case ROUND_HALF_UP -> new RoundHalfUpNode(new ConstantF32Node(0.0F));
                            case CEIL -> new CeilNode(new ConstantF32Node(0.0F));
                            case ROUND_TOWARDS_ZERO -> new RoundTowardsZeroNode(new ConstantF32Node(0.0F));
                        };
                        yield new ConstantF32Node(
                                dummyNode.computeF32(input.getValue() / multiple.getValue()) * multiple.getValue()
                        );
                    }
                }

                yield roundingDFNode;
            }
            case ToF32Node toF32Node -> {
                if (toF32Node.next.getReturnType() == AstNode.ReturnType.F32) {
                    yield toF32Node.next;
                }
                if (toF32Node.next instanceof ConstantF64Node c) {
                    yield new ConstantF32Node((float) c.getValue());
                }

                yield toF32Node;
            }
            case ToF64Node toF64Node -> {
                if (toF64Node.next.getReturnType() == AstNode.ReturnType.F64) {
                    yield toF64Node.next;
                }
                if (toF64Node.next instanceof ConstantF32Node c) {
                    yield new ConstantF64Node(c.getValue());
                }

                yield toF64Node;
            }
            case RepositionNode repositionNode -> {
                if (repositionNode.input instanceof ConstantNodeLike) {
                    yield repositionNode.input;
                }

                yield repositionNode;
            }
            case Multi2SingleNode multi2SingleNode -> {
                if (multi2SingleNode.next instanceof ConstantNodeLike) {
                    yield multi2SingleNode.next;
                }

                yield multi2SingleNode;
            }
            case null -> throw new NullPointerException();
            default -> astNode;
        };
    }

}
