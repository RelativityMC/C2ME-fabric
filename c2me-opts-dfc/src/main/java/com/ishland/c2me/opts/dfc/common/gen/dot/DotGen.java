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

package com.ishland.c2me.opts.dfc.common.gen.dot;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantNode;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.util.math.Spline;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;

import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class DotGen {

    private static final AtomicInteger ID = new AtomicInteger();

//    public static void compile(DensityFunction df) {
//        final var ctx = new Context();
//        final var ast = McToAst.toAst(df);
//        final var root = Context.base26(ctx.generate(ast));
//        final int id = ID.getAndIncrement();
//        GenDumper.dumpDot("DfcCompiled_" + id, ctx.allocatedNodes, ctx.constants, root);
//        ctx.reset();
//        final var ast2 = OptoPasses.optimize(ast);
//        final var root2 = Context.base26(ctx.generate(ast2));
//        GenDumper.dumpDot("DfcCompiled_optimized_" + id, ctx.allocatedNodes, ctx.constants, root2);
//        ctx.reset();
//    }

    public static class Context {
        private final Object2ReferenceOpenHashMap<AstNode, Builder.Impl> allocatedNodes = new Object2ReferenceOpenHashMap<>();
        private final Object2ReferenceOpenHashMap<Spline<DensityFunctionTypes.Spline.DensityFunctionWrapper>, Builder.Impl> allocatedSplines = new Object2ReferenceOpenHashMap<>();
        // don't merge constant nodes, otherwise the graph is going to be a mess
        private final ReferenceArrayList<Builder.Impl> constants = new ReferenceArrayList<>();
        private final ReferenceArrayList<Builder.Impl> extras = new ReferenceArrayList<>();
        private int counter = 0;

        public static String base26(int id) {
            StringBuilder builder = new StringBuilder(2);
            do {
                builder.insert(0, (char) ('a' + id % 26));
            } while ((id /= 26) > 0);
            return builder.toString();
        }

        void reset() {
            allocatedNodes.clear();
            constants.clear();
            counter = 0;
        }

        public sealed interface Builder {
            Builder boxShape();
            Builder diamondShape();
            Builder circleShape();
            Builder ovalShape();
            Builder triangleShape();
            Builder invTriangleShape();
            Builder trapeziumShape();
            Builder hexagonShape();
            Builder cdsShape();
            Builder folderShape();
            Builder parallelogramShape();

            Builder label(String label);
            Builder tooltip(String tooltip);
            Edge edge(int from);
            int build();

            boolean isFrozen();
            int getId();

            void write(StringBuilder sb);

            sealed interface Edge {
                Edge label(String label);
                Edge color(String color);
                Builder finish();

                String toString();
            }

            final class Impl implements Builder {

                private final ReferenceArrayList<EdgeBuilder> edges = new ReferenceArrayList<>();
                private String shape, label, tooltip;
                private boolean frozen = false;
                private final int id;

                Impl(int id) {
                    this.id = id;
                }

                @Override
                public Builder boxShape() {
                    shape = "box";
                    return this;
                }

                @Override
                public Builder diamondShape() {
                    shape = "diamond";
                    return this;
                }

                @Override
                public Builder circleShape() {
                    shape = "circle";
                    return this;
                }

                @Override
                public Builder ovalShape() {
                    shape = "oval";
                    return this;
                }

                @Override
                public Builder triangleShape() {
                    shape = "triangle";
                    return this;
                }

                @Override
                public Builder invTriangleShape() {
                    shape = "invtriangle";
                    return this;
                }

                @Override
                public Builder trapeziumShape() {
                    shape = "trapezium";
                    return this;
                }

                @Override
                public Builder hexagonShape() {
                    shape = "hexagon";
                    return this;
                }

                @Override
                public Builder cdsShape() {
                    shape = "cds";
                    return this;
                }

                @Override
                public Builder folderShape() {
                    shape = "folder";
                    return this;
                }

                @Override
                public Builder parallelogramShape() {
                    shape = "parallelogram";
                    return this;
                }

                @Override
                public Builder label(String label) {
                    this.label = label;
                    return this;
                }

                @Override
                public Builder tooltip(String tooltip) {
                    this.tooltip = tooltip;
                    return this;
                }

                @Override
                public Edge edge(int from) {
                    final var edge = new EdgeBuilder(from);
                    this.edges.add(edge);
                    return edge;
                }

                final class EdgeBuilder implements Builder.Edge {

                    private final int from;
                    private String label, color;

                    EdgeBuilder(int from) {
                        this.from = from;
                    }

                    @Override
                    public Edge label(String label) {
                        this.label = label;
                        return this;
                    }

                    @Override
                    public Edge color(String color) {
                        this.color = color;
                        return this;
                    }

                    @Override
                    public Impl finish() {
                        return Impl.this;
                    }

                    public String toString() {
                        if (label == null && color == null) {
                            return "";
                        }
                        if (label == null) {
                            return " [color=" + color + "]";
                        }
                        if (color == null) {
                            return " [label=\"" + label + "\"]";
                        }
                        return " [label=\"" + label + "\", color=" + color + "]";
                    }
                }

                @Override
                public int build() {
                    frozen = true;
                    return id;
                }

                @Override
                public boolean isFrozen() {
                    return this.frozen;
                }

                @Override
                public int getId() {
                    return this.id;
                }

                @Override
                public void write(StringBuilder sb) {
                    String name = base26(id);
                    sb.append("\t").append(name).append(" [shape=").append(shape).append(", ");
                    if (label != null && label.startsWith("<") && label.endsWith(">")) {
                        sb.append("label=").append(label);
                    } else {
                        sb.append("label=\"").append("id=").append(name).append("\\n").append(label).append("\"");
                    }
                    sb.append(", tooltip=\"");
                    if (tooltip != null) {
                        sb.append(tooltip).append("\\n");
                    }
                    for (EdgeBuilder edge : edges) {
                        final var node = base26(edge.from);
                        if(edge.label != null) {
                            sb.append(edge.label).append(".id=").append(node).append("\\n");
                        } else {
                            sb.append("children[].id=").append(node).append("\\n");
                        }
                    }
                    sb.append('\"');
                    sb.append("]");
                    for (EdgeBuilder edge : edges) {
                        sb.append('\n');
                        sb.append("\t");
                        final var node = base26(edge.from);
                        sb.append(node).append(" -> ").append(name).append(edge);
                    }
                }
            }
        }

        public int generate(AstNode node) {
            final Builder.Impl builder;
            if (node instanceof ConstantNode) {
                builder = new Builder.Impl(counter++);
                constants.add(builder);
            } else {
                builder = allocatedNodes.computeIfAbsent(node, _ -> new Builder.Impl(counter++));
                if (builder.frozen) {
                    return builder.id;
                }
            }
            return DotGenRegistry.doDotGen(node, this, builder);
        }

        /**
         * Callers should check {@code builder.frozen}
         */
        public Builder getSplineBuilder(Spline<DensityFunctionTypes.Spline.DensityFunctionWrapper> spline) {
            final Builder.Impl builder;
            builder = allocatedSplines.computeIfAbsent(spline, _ -> new Builder.Impl(counter++));
            return builder;
        }

        public Builder createExtraBuilder() {
            Builder.Impl builder = new Builder.Impl(counter++);
            this.extras.add(builder);
            return builder;
        }

        public String write(String name, int rootId) {
            Iterator<Builder.Impl> iterator = Stream.concat(
                    Stream.concat(
                            this.constants.stream(),
                            this.allocatedNodes.values().stream()
                    ),
                    Stream.concat(
                            this.allocatedSplines.values().stream(),
                            this.extras.stream()
                    )
            ).sorted(Comparator.comparing(Builder.Impl::getId)).iterator();
            StringBuilder sb = new StringBuilder();
            sb.append("strict digraph ").append(name).append(" {");
            sb.append('\n');
            sb.append('\t').append("root=").append(base26(rootId));
            sb.append("\n\t").append("ranksep=1");
            while (iterator.hasNext()) {
                sb.append('\n');
                iterator.next().write(sb);
            }
            sb.append('\n');
            sb.append("}");
            return sb.toString();
        }

    }
}
