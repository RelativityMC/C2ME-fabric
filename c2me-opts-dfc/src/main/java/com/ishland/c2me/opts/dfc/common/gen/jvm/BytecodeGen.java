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

package com.ishland.c2me.opts.dfc.common.gen.jvm;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import com.ishland.c2me.opts.dfc.common.ast.McToAst;
import com.ishland.c2me.opts.dfc.common.ast.misc.CacheLikeNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.RootNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.YClampedGradientNode;
import com.ishland.c2me.opts.dfc.common.ast.opto.OptoPasses;
import com.ishland.c2me.opts.dfc.common.gen.GenDumper;
import com.ishland.c2me.opts.dfc.common.gen.jvm.internalapi.ArgumentVisitor;
import com.ishland.c2me.opts.dfc.common.gen.jvm.internalapi.IMultiMethod;
import com.ishland.c2me.opts.dfc.common.gen.jvm.internalapi.ISingleMethod;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefD;
import com.ishland.c2me.opts.dfc.common.gen.jvm.util.DfcObjectCache;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceMaps;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;
import net.minecraft.util.math.Spline;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.InstructionAdapter;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

public class BytecodeGen {

    private static final AtomicLong ordinal = new AtomicLong();

    public static final Hash.Strategy<AstNode> RELAXED_STRATEGY = new Hash.Strategy<>() {
        @Override
        public int hashCode(AstNode o) {
            return o.relaxedHashCode();
        }

        @Override
        public boolean equals(AstNode a, AstNode b) {
            return a.relaxedEquals(b);
        }
    };
    private static final Object2ReferenceMap<AstNode, Class<?>> compilationCache = Object2ReferenceMaps.synchronize(new Object2ReferenceOpenCustomHashMap<>(RELAXED_STRATEGY));

//    public static DensityFunction compile(String name, DensityFunction densityFunction, Reference2ReferenceMap<DensityFunction, OptoPasses.AstPair> optoCache, Reference2ReferenceMap<DensityFunction, DensityFunction> tempCache) {
//        DensityFunction cached = tempCache.get(densityFunction);
//        if (cached != null) {
//            return cached;
//        }
//        OptoPasses.AstPair pair = optoCache.computeIfAbsent(densityFunction, (DensityFunction df) -> OptoPasses.optimize(McToAst.toAst(df)));
//        if (pair.optimized() instanceof ConstantNode constantNode) {
//            return DensityFunctionTypes.constant(constantNode.getValue());
//        } else if (pair.optimized() instanceof YClampedGradientNode) {
//            return densityFunction;
//        }
//        CompiledDensityFunction compiled = new CompiledDensityFunction(compile0(name, pair), densityFunction);
//        tempCache.put(densityFunction, compiled);
//        return compiled;
//    }

    public static Context initContext() {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        String name = String.format("DfcCompiled_%d", ordinal.getAndIncrement());
        writer.visit(Opcodes.V21, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, name, null, Type.getInternalName(Object.class), new String[]{Type.getInternalName(CompiledEntry.class)});
        return new Context(writer, name);
    }

    public static CompiledEntry finalizeCompilation(Context genContext) {
        Object[] args = genContext.args.entrySet().stream()
                .sorted(Comparator.comparingInt(o -> o.getValue().ordinal()))
                .map(Map.Entry::getKey)
                .toArray(Object[]::new);

        genConstructor(genContext);
        genGetArgs(genContext);
        genNewInstance(genContext);
        genGetRootsUnsafe(genContext);

        byte[] bytes = genContext.classWriter.toByteArray();
        Path dumpedClass = GenDumper.dumpClass(genContext.className, bytes);
        GenDumper.dumpDot(genContext.className, dumpedClass, genContext.toDump);
        Class<?> defined = defineClass(genContext.className, bytes);
//        compilationCache.put(node, defined);

        CompiledEntry compiledEntry;
        try {
            compiledEntry = (CompiledEntry) defined.getConstructor(Object[].class, ArgumentVisitor.class).newInstance(new Object[]{args, ArgumentVisitor.IDENTITY});
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        for (CompiledDensityFunction delayedInit : genContext.delayedInits) {
            delayedInit.initFrom(compiledEntry);
        }
        genContext.delayedInits.clear();

        return compiledEntry;
    }

    private static void genConstructor(Context context) {
        InstructionAdapter m = new InstructionAdapter(
                new AnalyzerAdapter(
                        context.className,
                        Opcodes.ACC_PUBLIC,
                        "<init>",
                        Context.CONSTRUCTOR_DESC,
                        context.classWriter.visitMethod(
                                Opcodes.ACC_PUBLIC,
                                "<init>",
                                Context.CONSTRUCTOR_DESC,
                                null,
                                null
                        )
                )
        );

        Label start = new Label();
        Label end = new Label();
        m.visitLabel(start);

        m.load(0, InstructionAdapter.OBJECT_TYPE);
        m.invokespecial(Type.getInternalName(Object.class), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE), false);

        for (Map.Entry<Object, Context.FieldRecord> entry : context.args.entrySet().stream().sorted(Comparator.comparingInt(o -> o.getValue().ordinal())).toList()) {
            String name = entry.getValue().name();
            Class<?> type = entry.getValue().type();
            int ordinal = entry.getValue().ordinal();
            String postProcessMethod = entry.getValue().postProcessMethod;

            m.load(0, InstructionAdapter.OBJECT_TYPE);

            {
                m.load(2, InstructionAdapter.OBJECT_TYPE);

                {
                    if (postProcessMethod != null) {
                        m.load(0, InstructionAdapter.OBJECT_TYPE);
                    }

                    {
                        m.load(1, InstructionAdapter.OBJECT_TYPE);
                        m.iconst(ordinal);
                        m.aload(InstructionAdapter.OBJECT_TYPE);
                        m.checkcast(Type.getType(type));
                    }

                    if (postProcessMethod != null) {
                        m.invokevirtual(context.className, postProcessMethod, Context.POSTPROCESSING_DESC, false);
                        m.checkcast(Type.getType(type));
                    }
                }

                m.invokeinterface(Type.getInternalName(ArgumentVisitor.class), "apply", Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(Object.class)));
                m.checkcast(Type.getType(type));
            }

            m.putfield(context.className, name, Type.getDescriptor(type));
        }

        {
            context.classWriter.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, "roots", Type.getDescriptor(SubCompiledDensityFunction[].class), null, null);

            m.load(0, InstructionAdapter.OBJECT_TYPE);

            {
                m.iconst(context.roots.size());
                m.newarray(Type.getType(SubCompiledDensityFunction.class));

                ReferenceArrayList<Context.MethodPair> roots = context.roots;
                for (int i = 0, rootsSize = roots.size(); i < rootsSize; i++) {
                    Context.MethodPair root = roots.get(i);
                    m.dup();
                    m.iconst(i);

                    {
                        m.anew(Type.getType(SubCompiledDensityFunction.class));
                        m.dup();

                        m.load(0, InstructionAdapter.OBJECT_TYPE);
                        m.invokedynamic(
                                "evalSingle",
                                Type.getMethodDescriptor(Type.getType(ISingleMethod.class), Type.getType(context.classDesc)),
                                new Handle(
                                        Opcodes.H_INVOKESTATIC,
                                        "java/lang/invoke/LambdaMetafactory",
                                        "metafactory",
                                        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                                        false
                                ),
                                new Object[]{
                                        Type.getMethodType(Context.SINGLE_DESC),
                                        new Handle(
                                                Opcodes.H_INVOKEVIRTUAL,
                                                context.className,
                                                root.single(),
                                                Context.SINGLE_DESC,
                                                false
                                        ),
                                        Type.getMethodType(Context.SINGLE_DESC)
                                }
                        );

                        m.load(0, InstructionAdapter.OBJECT_TYPE);
                        m.invokedynamic(
                                "evalMulti",
                                Type.getMethodDescriptor(Type.getType(IMultiMethod.class), Type.getType(context.classDesc)),
                                new Handle(
                                        Opcodes.H_INVOKESTATIC,
                                        "java/lang/invoke/LambdaMetafactory",
                                        "metafactory",
                                        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                                        false
                                ),
                                new Object[]{
                                        Type.getMethodType(Context.MULTI_DESC),
                                        new Handle(
                                                Opcodes.H_INVOKEVIRTUAL,
                                                context.className,
                                                root.multi(),
                                                Context.MULTI_DESC,
                                                false
                                        ),
                                        Type.getMethodType(Context.MULTI_DESC)
                                }
                        );

                        m.aconst(null);
                        m.checkcast(Type.getType(DensityFunction.class));

                        m.invokespecial(
                                Type.getInternalName(SubCompiledDensityFunction.class),
                                "<init>",
                                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(ISingleMethod.class), Type.getType(IMultiMethod.class), Type.getType(DensityFunction.class)),
                                false
                        );

                        m.checkcast(Type.getType(SubCompiledDensityFunction.class));
                    }

                    m.astore(Type.getType(SubCompiledDensityFunction.class));
                }
            }

            m.putfield(context.className, "roots", Type.getDescriptor(SubCompiledDensityFunction[].class));
        }

        m.areturn(Type.VOID_TYPE);
        m.visitLabel(end);
        m.visitLocalVariable("this", context.classDesc, null, start, end, 0);
        m.visitLocalVariable("args", Type.getDescriptor(Object[].class), null, start, end, 1);
        m.visitLocalVariable("visitor", Type.getDescriptor(ArgumentVisitor.class), null, start, end, 2);
        m.visitMaxs(0, 0);
    }

    private static void genGetArgs(Context context) {
        InstructionAdapter m = new InstructionAdapter(
                new AnalyzerAdapter(
                        context.className,
                        Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                        "getArgs",
                        Type.getMethodDescriptor(Type.getType(Object[].class)),
                        context.classWriter.visitMethod(
                                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                                "getArgs",
                                Type.getMethodDescriptor(Type.getType(Object[].class)),
                                null,
                                null
                        )
                )
        );

        Label start = new Label();
        Label end = new Label();
        m.visitLabel(start);

        m.iconst(context.args.size());
        m.newarray(InstructionAdapter.OBJECT_TYPE);

        int index = 0;
        for (Map.Entry<Object, Context.FieldRecord> entry : context.args.entrySet().stream().sorted(Comparator.comparingInt(o -> o.getValue().ordinal())).toList()) {
            String name = entry.getValue().name();
            Class<?> type = entry.getValue().type();

            m.dup();
            m.iconst(index ++);
            m.load(0, InstructionAdapter.OBJECT_TYPE);
            m.getfield(context.className, name, Type.getDescriptor(type));
            m.astore(InstructionAdapter.OBJECT_TYPE);
        }

        m.areturn(InstructionAdapter.OBJECT_TYPE);
        m.visitLabel(end);
        m.visitLocalVariable("this", context.classDesc, null, start, end, 0);
        m.visitLocalVariable("args", Type.getDescriptor(Object[].class), null, start, end, 1);
        m.visitMaxs(0, 0);
    }

    private static void genNewInstance(Context context) {
        InstructionAdapter m = new InstructionAdapter(
                new AnalyzerAdapter(
                        context.className,
                        Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                        "newInstance",
                        Type.getMethodDescriptor(Type.getType(CompiledEntry.class), Type.getType(Object[].class), Type.getType(ArgumentVisitor.class)),
                        context.classWriter.visitMethod(
                                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                                "newInstance",
                                Type.getMethodDescriptor(Type.getType(CompiledEntry.class), Type.getType(Object[].class), Type.getType(ArgumentVisitor.class)),
                                null,
                                null
                        )
                )
        );
        Label start = new Label();
        Label end = new Label();
        m.visitLabel(start);

        m.anew(Type.getType(context.classDesc));
        m.dup();
        m.load(1, InstructionAdapter.OBJECT_TYPE);
        m.load(2, InstructionAdapter.OBJECT_TYPE);
        m.invokespecial(context.className, "<init>", Context.CONSTRUCTOR_DESC, false);
        m.areturn(InstructionAdapter.OBJECT_TYPE);

        m.visitLabel(end);
        m.visitLocalVariable("this", context.classDesc, null, start, end, 0);
        m.visitLocalVariable("args", Type.getDescriptor(Object[].class), null, start, end, 1);
        m.visitLocalVariable("visitor", Type.getDescriptor(ArgumentVisitor.class), null, start, end, 2);
        m.visitMaxs(0, 0);
    }

    private static void genGetRootsUnsafe(Context context) {
        InstructionAdapter m = new InstructionAdapter(
                new AnalyzerAdapter(
                        context.className,
                        Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                        "getRootsUnsafe",
                        Type.getMethodDescriptor(Type.getType(SubCompiledDensityFunction[].class)),
                        context.classWriter.visitMethod(
                                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                                "getRootsUnsafe",
                                Type.getMethodDescriptor(Type.getType(SubCompiledDensityFunction[].class)),
                                null,
                                null
                        )
                )
        );
        Label start = new Label();
        Label end = new Label();
        m.visitLabel(start);

        m.load(0, InstructionAdapter.OBJECT_TYPE);
        m.getfield(context.className, "roots", Type.getDescriptor(SubCompiledDensityFunction[].class));
        m.areturn(InstructionAdapter.OBJECT_TYPE);

        m.visitLabel(end);
        m.visitLocalVariable("this", context.classDesc, null, start, end, 0);
        m.visitMaxs(0, 0);
    }

//    private static void genFields(Context context) {
//        for (Map.Entry<Object, Context.FieldRecord> entry : context.args.entrySet().stream().sorted(Comparator.comparingInt(o -> o.getValue().ordinal())).toList()) {
//            String name = entry.getValue().name();
//            Class<?> type = entry.getValue().type();
//
//            context.classWriter.visitField(
//                    Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL,
//                    name,
//                    Type.getDescriptor(type),
//                    null,
//                    null
//            );
//        }
//    }

    private static Class<?> defineClass(String className, byte[] bytes) {
        ClassLoader classLoader = new ClassLoader(BytecodeGen.class.getClassLoader()) {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                if (name.equals(className)) {
                    return super.defineClass(name, bytes, 0, bytes.length);
                }

                return super.loadClass(name);
            }
        };

        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static class Context {
        public static final String SINGLE_DESC = Type.getMethodDescriptor(Type.getType(double.class), Type.getType(int.class), Type.getType(int.class), Type.getType(int.class), Type.getType(EvalType.class), Type.getType(DfcObjectCache.class));
        public static final String MULTI_DESC = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(double[].class), Type.getType(int[].class), Type.getType(int[].class), Type.getType(int[].class), Type.getType(EvalType.class), Type.getType(DfcObjectCache.class));
        public static final String POSTPROCESSING_DESC = Type.getMethodDescriptor(Type.getType(Object.class), Type.getType(Object.class));
        public static final String CONSTRUCTOR_DESC = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(Object[].class), Type.getType(ArgumentVisitor.class));
        public final ClassWriter classWriter;
        public final String className;
        public final String classDesc;
        private int methodIdx = 0;
        private final Object2ReferenceOpenHashMap<AstNode, String> singleMethods = new Object2ReferenceOpenHashMap<>();
        private final Object2ReferenceOpenHashMap<AstNode, String> multiMethods = new Object2ReferenceOpenHashMap<>();
        private final Object2ReferenceOpenHashMap<Spline<DensityFunctionTypes.Spline.SplinePos, DensityFunctionTypes.Spline.DensityFunctionWrapper>, String> splineMethods = new Object2ReferenceOpenHashMap<>();
        private final Object2ReferenceOpenHashMap<Spline<DensityFunctionTypes.Spline.SplinePos, DensityFunctionTypes.Spline.DensityFunctionWrapper>, String> splineMethodsCache1 = new Object2ReferenceOpenHashMap<>();
        private final Reference2ObjectOpenHashMap<Object, FieldRecord> args = new Reference2ObjectOpenHashMap<>();
        private final ReferenceArrayList<MethodPair> roots = new ReferenceArrayList<>();
        private final Reference2ReferenceLinkedOpenHashMap<String, OptoPasses.AstPair> toDump = new Reference2ReferenceLinkedOpenHashMap<>();
        private final ReferenceArrayList<CompiledDensityFunction> delayedInits = new ReferenceArrayList<>();
        private final Reference2ReferenceMap<DensityFunction, OptoPasses.AstPair> optoCache = new Reference2ReferenceLinkedOpenHashMap<>();

        public Context(ClassWriter classWriter, String className) {
            this.classWriter = Objects.requireNonNull(classWriter);
            this.className = Objects.requireNonNull(className);
            this.classDesc = String.format("L%s;", this.className);
        }

        public String nextMethodName() {
            return String.format("method_%d", methodIdx++);
        }

        public String nextMethodName(String suffix) {
            return String.format("method_%d_%s", methodIdx++, suffix);
        }

        public String nextMethodName(AstNode node) {
            StringBuilder b = new StringBuilder();
            b.append(node.getClass().getSimpleName());
            if (node instanceof CacheLikeNode cacheLikeNode && (Object) cacheLikeNode.getCacheLike() instanceof DensityFunctionTypes.Wrapping wrapping) {
                b.append('_').append(wrapping.type().asString());
            }
            return nextMethodName(b.toString());
        }

        public ValuesMethodDefD newSingleMethod(AstNode node) {
            if (node instanceof ConstantNode constantNode) {
                return new ValuesMethodDefD(constantNode.getValue());
            } else {
                String generated = this.newSingleMethodUnoptimized(node);
                return new ValuesMethodDefD(generated);
            }
        }

        public String newSingleMethodUnoptimized(AstNode node) {
            return this.singleMethods.computeIfAbsent(node, (AstNode node1) -> this.newSingleMethod((adapter, localVarConsumer) -> BytecodeGenRegistry.doBytecodeGenSingle(node1, this, adapter, localVarConsumer), nextMethodName(node)));
        }

        private String newSingleMethod(BiConsumer<InstructionAdapter, LocalVarConsumer> generator) {
            return newSingleMethod(generator, nextMethodName());
        }

        private String newSingleMethod(BiConsumer<InstructionAdapter, LocalVarConsumer> generator, String name) {
            newSingleMethod0(generator, name, false);
            return name;
        }

        private void newSingleMethod0(BiConsumer<InstructionAdapter, LocalVarConsumer> generator, String name, boolean isPublic) {
            InstructionAdapter adapter = new InstructionAdapter(
                    new AnalyzerAdapter(
                            this.className,
                            (isPublic ? Opcodes.ACC_PUBLIC : Opcodes.ACC_PRIVATE) | Opcodes.ACC_FINAL,
                            name,
                            SINGLE_DESC,
                            classWriter.visitMethod(
                                    (isPublic ? Opcodes.ACC_PUBLIC : Opcodes.ACC_PRIVATE) | Opcodes.ACC_FINAL,
                                    name,
                                    SINGLE_DESC,
                                    null,
                                    null
                            )
                    )
            );
            List<IntObjectPair<Pair<String, String>>> extraLocals = new ArrayList<>();
            Label start = new Label();
            Label end = new Label();
            adapter.visitLabel(start);
            generator.accept(adapter, (localName, localDesc) -> {
                int ordinal = extraLocals.size() + 6;
                extraLocals.add(IntObjectPair.of(ordinal, Pair.of(localName, localDesc)));
                return ordinal;
            });
            adapter.visitLabel(end);
            adapter.visitLocalVariable("this", this.classDesc, null, start, end, 0);
            adapter.visitLocalVariable("x", Type.INT_TYPE.getDescriptor(), null, start, end, 1);
            adapter.visitLocalVariable("y", Type.INT_TYPE.getDescriptor(), null, start, end, 2);
            adapter.visitLocalVariable("z", Type.INT_TYPE.getDescriptor(), null, start, end, 3);
            adapter.visitLocalVariable("evalType", Type.getType(EvalType.class).getDescriptor(), null, start, end, 4);
            adapter.visitLocalVariable("dfcObjectCache", Type.getType(DfcObjectCache.class).getDescriptor(), null, start, end, 5);
            for (IntObjectPair<Pair<String, String>> local : extraLocals) {
                adapter.visitLocalVariable(local.right().left(), local.right().right(), null, start, end, local.leftInt());
            }
            adapter.visitMaxs(0, 0);
        }

        public ValuesMethodDefD newMultiMethod(AstNode node) {
            if (node instanceof ConstantNode constantNode) {
                return new ValuesMethodDefD(constantNode.getValue());
            } else {
                String generated = newMultiMethodUnoptimized(node);
                return new ValuesMethodDefD(generated);
            }
        }

        public String newMultiMethodUnoptimized(AstNode node) {
            return this.multiMethods.computeIfAbsent(node, (AstNode node1) -> this.newMultiMethod((adapter, localVarConsumer) -> BytecodeGenRegistry.doBytecodeGenMulti(node1, this, adapter, localVarConsumer), nextMethodName(node)));
        }

        private String newMultiMethod(BiConsumer<InstructionAdapter, LocalVarConsumer> generator) {
            return newMultiMethod(generator, nextMethodName());
        }

        private String newMultiMethod(BiConsumer<InstructionAdapter, LocalVarConsumer> generator, String name) {
            newMultiMethod0(generator, name, false);
            return name;
        }

        private void newMultiMethod0(BiConsumer<InstructionAdapter, LocalVarConsumer> generator, String name, boolean isPublic) {
            InstructionAdapter adapter = new InstructionAdapter(
                    new AnalyzerAdapter(
                            this.className,
                            (isPublic ? Opcodes.ACC_PUBLIC : Opcodes.ACC_PRIVATE) | Opcodes.ACC_FINAL,
                            name,
                            MULTI_DESC,
                            classWriter.visitMethod(
                                    (isPublic ? Opcodes.ACC_PUBLIC : Opcodes.ACC_PRIVATE) | Opcodes.ACC_FINAL,
                                    name,
                                    MULTI_DESC,
                                    null,
                                    null
                            )
                    )
            );
            List<IntObjectPair<Pair<String, String>>> extraLocals = new ArrayList<>();
            Label start = new Label();
            Label end = new Label();
            adapter.visitLabel(start);
            generator.accept(adapter, (localName, localDesc) -> {
                int ordinal = extraLocals.size() + 8;
                extraLocals.add(IntObjectPair.of(ordinal, Pair.of(localName, localDesc)));
                return ordinal;
            });
            adapter.visitLabel(end);
            adapter.visitLocalVariable("this", this.classDesc, null, start, end, 0);
            adapter.visitLocalVariable("res", Type.getType(double[].class).getDescriptor(), null, start, end, 1);
            adapter.visitLocalVariable("x", Type.getType(double[].class).getDescriptor(), null, start, end, 2);
            adapter.visitLocalVariable("y", Type.getType(double[].class).getDescriptor(), null, start, end, 3);
            adapter.visitLocalVariable("z", Type.getType(double[].class).getDescriptor(), null, start, end, 4);
            adapter.visitLocalVariable("evalType", Type.getType(EvalType.class).getDescriptor(), null, start, end, 5);
            adapter.visitLocalVariable("dfcObjectCache", Type.getType(DfcObjectCache.class).getDescriptor(), null, start, end, 6);
            for (IntObjectPair<Pair<String, String>> local : extraLocals) {
                adapter.visitLocalVariable(local.right().left(), local.right().right(), null, start, end, local.leftInt());
            }
            adapter.visitMaxs(0, 0);
        }

        public String getCachedSplineMethod(Spline<DensityFunctionTypes.Spline.SplinePos, DensityFunctionTypes.Spline.DensityFunctionWrapper> spline, boolean cache1) {
            return (cache1 ? this.splineMethodsCache1 : this.splineMethods).get(spline);
        }

        public void cacheSplineMethod(Spline<DensityFunctionTypes.Spline.SplinePos, DensityFunctionTypes.Spline.DensityFunctionWrapper> spline, String method, boolean cache1) {
            (cache1 ? this.splineMethodsCache1 : this.splineMethods).put(spline, method);
        }

        public void callDelegateSingle(InstructionAdapter m, ValuesMethodDefD target) {
            if (target.isConst()) {
                m.dconst(target.constValue());
            } else {
                m.load(0, InstructionAdapter.OBJECT_TYPE);
                m.load(1, Type.INT_TYPE);
                m.load(2, Type.INT_TYPE);
                m.load(3, Type.INT_TYPE);
                m.load(4, InstructionAdapter.OBJECT_TYPE);
                m.load(5, InstructionAdapter.OBJECT_TYPE);
                m.invokevirtual(this.className, target.generatedMethod(), SINGLE_DESC, false);
            }
        }

        public void callDelegateSingleFromMulti(InstructionAdapter m, ValuesMethodDefD target, int indexLocal) {
            if (target.isConst()) {
                m.dconst(target.constValue());
            } else {
                m.load(0, InstructionAdapter.OBJECT_TYPE);
                m.load(2, InstructionAdapter.OBJECT_TYPE);
                m.load(indexLocal, Type.INT_TYPE);
                m.aload(Type.INT_TYPE);
                m.load(3, InstructionAdapter.OBJECT_TYPE);
                m.load(indexLocal, Type.INT_TYPE);
                m.aload(Type.INT_TYPE);
                m.load(4, InstructionAdapter.OBJECT_TYPE);
                m.load(indexLocal, Type.INT_TYPE);
                m.aload(Type.INT_TYPE);
                m.load(5, InstructionAdapter.OBJECT_TYPE);
                m.load(6, InstructionAdapter.OBJECT_TYPE);

                m.invokevirtual(
                        this.className,
                        target.generatedMethod(),
                        BytecodeGen.Context.SINGLE_DESC,
                        false
                );
            }
        }

        public void callDelegateMulti(InstructionAdapter m, ValuesMethodDefD target) {
            callDelegateMulti(m, target, 1);
        }

        public void callDelegateMulti(InstructionAdapter m, ValuesMethodDefD target, int arrayLocalIndex) {
            if (target.isConst()) {
                m.load(arrayLocalIndex, InstructionAdapter.OBJECT_TYPE);
                m.dconst(target.constValue());
                m.invokestatic(Type.getInternalName(Arrays.class), "fill", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(double[].class), Type.DOUBLE_TYPE), false);
            } else {
                m.load(0, InstructionAdapter.OBJECT_TYPE);
                m.load(arrayLocalIndex, InstructionAdapter.OBJECT_TYPE);
                m.load(2, InstructionAdapter.OBJECT_TYPE);
                m.load(3, InstructionAdapter.OBJECT_TYPE);
                m.load(4, InstructionAdapter.OBJECT_TYPE);
                m.load(5, InstructionAdapter.OBJECT_TYPE);
                m.load(6, InstructionAdapter.OBJECT_TYPE);
                m.invokevirtual(this.className, target.generatedMethod(), MULTI_DESC, false);
            }
        }

        public <T> String newField(Class<T> type, T data) {
            return this.newField(type, data, null);
        }

        public <T> String newField(Class<T> type, T data, Consumer<InstructionAdapter> generator) {
            FieldRecord existing = this.args.get(data);
            if (existing != null) {
                return existing.name();
            }
            int size = this.args.size();
            String name = String.format("field_%d", size);
            classWriter.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, name, Type.getDescriptor(type), null, null);

            String postprocessMethod;
            if (generator != null) {
                postprocessMethod = String.format("postprocess_field_%d", size);
                InstructionAdapter adapter = new InstructionAdapter(
                        new AnalyzerAdapter(
                                this.className,
                                Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL,
                                postprocessMethod,
                                POSTPROCESSING_DESC,
                                classWriter.visitMethod(
                                        Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL,
                                        postprocessMethod,
                                        POSTPROCESSING_DESC,
                                        null,
                                        null
                                )
                        )
                );
                Label start = new Label();
                Label end = new Label();
                adapter.visitLabel(start);
                generator.accept(adapter);
                adapter.visitLabel(end);
                adapter.visitMaxs(0, 0);
                adapter.visitLocalVariable("this", this.classDesc, null, start, end, 0);
                adapter.visitLocalVariable("operand", Type.getDescriptor(Object.class), null, start, end, 0);
            } else {
                postprocessMethod = null;
            }

            this.args.put(data, new FieldRecord(name, size, type, postprocessMethod));
            return name;
        }

        public void doCountedLoop(InstructionAdapter m, LocalVarConsumer localVarConsumer, IntConsumer bodyGenerator) {
            int loopIdx = localVarConsumer.createLocalVariable("loopIdx", Type.INT_TYPE.getDescriptor());
            m.iconst(0);
            m.store(loopIdx, Type.INT_TYPE);

            Label start = new Label();
            Label end = new Label();

            m.visitLabel(start);
            m.load(loopIdx, Type.INT_TYPE);
            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.arraylength();
            m.ificmpge(end);

            bodyGenerator.accept(loopIdx);

            m.iinc(loopIdx, 1);
            m.goTo(start);
            m.visitLabel(end);
        }

        public void delegateAllToSingle(InstructionAdapter m, BytecodeGen.Context.LocalVarConsumer localVarConsumer, AstNode current) {
            ValuesMethodDefD singleMethod = this.newSingleMethod(current);
            this.doCountedLoop(m, localVarConsumer, idx -> {
                m.load(1, InstructionAdapter.OBJECT_TYPE);
                m.load(idx, Type.INT_TYPE);

                this.callDelegateSingleFromMulti(m, singleMethod, idx);

                m.astore(Type.DOUBLE_TYPE);
            });
        }

        private OptoPasses.AstPair optimizeCached(DensityFunction densityFunction) {
            return optoCache.computeIfAbsent(densityFunction, (DensityFunction df) -> OptoPasses.optimize(McToAst.toAst(df)));
        }

        public int registerRoot(String suffix, AstNode node) {
            int index = this.roots.size();
            String single = String.format("evalSingle_%d_%s", index, suffix);
            String multi = String.format("evalMulti_%d_%s", index, suffix);
            RootNode rootNode = new RootNode(node);
            this.newSingleMethod0((adapter, localVarConsumer) -> BytecodeGenRegistry.doBytecodeGenSingle(rootNode, this, adapter, localVarConsumer), single, true);
            this.newMultiMethod0((adapter, localVarConsumer) -> BytecodeGenRegistry.doBytecodeGenMulti(rootNode, this, adapter, localVarConsumer), multi, true);
            this.roots.add(new MethodPair(single, multi));
            return index;
        }

        public DensityFunction compileDelayed(String suffix, DensityFunction df) {
            OptoPasses.AstPair pair = optimizeCached(df);
            if (pair.optimized() instanceof ConstantNode constantNode) {
                return DensityFunctionTypes.constant(constantNode.getValue());
            } else if (pair.optimized() instanceof YClampedGradientNode) {
                return df;
            }
            int index = this.registerRoot(suffix, pair.optimized());
            CompiledDensityFunction compiled = new CompiledDensityFunction(index, df);
            this.delayedInits.add(compiled);
            this.toDump.put(suffix, pair);
            return compiled;
        }

        public static interface LocalVarConsumer {
            int createLocalVariable(String name, String descriptor);
        }

        private static record FieldRecord(String name, int ordinal, Class<?> type, String postProcessMethod) {
        }

        private static record MethodPair(String single, String multi) {
        }
    }

    @FunctionalInterface
    public interface EvalSingleInterface {
        double evalSingle(int x, int y, int z, EvalType type);
    }

    @FunctionalInterface
    public interface EvalMultiInterface {
        void evalMulti(double[] res, int[] x, int[] y, int[] z, EvalType type);
    }

}
