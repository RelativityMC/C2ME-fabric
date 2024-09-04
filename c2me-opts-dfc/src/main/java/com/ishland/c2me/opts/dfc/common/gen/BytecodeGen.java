package com.ishland.c2me.opts.dfc.common.gen;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.EvalType;
import com.ishland.c2me.opts.dfc.common.ast.McToAst;
import com.ishland.c2me.opts.dfc.common.ast.misc.ConstantNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.RootNode;
import com.ishland.c2me.opts.dfc.common.vif.AstVanillaInterface;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import net.minecraft.world.gen.densityfunction.DensityFunctionTypes;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AnalyzerAdapter;
import org.objectweb.asm.commons.InstructionAdapter;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.IntConsumer;
import java.util.stream.Collectors;

public class BytecodeGen {

    private static final File exportDir = new File("./dfcOutput");

    private static final AtomicLong ordinal = new AtomicLong();

    static {
        try {
            org.spongepowered.asm.util.Files.deleteRecursively(exportDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static DensityFunction compile(DensityFunction densityFunction) {
        if (densityFunction instanceof AstVanillaInterface vif) {
            AstNode ast = vif.getAstNode();
            return new CompiledDensityFunction(compile0(ast), vif.getBlendingFallback());
        }
        AstNode ast = McToAst.toAst(densityFunction);
        if (ast instanceof ConstantNode constantNode) {
            return DensityFunctionTypes.constant(constantNode.getValue());
        }
        return new CompiledDensityFunction(compile0(ast), densityFunction);
    }

    public static CompiledEntry compile0(AstNode node) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        String name = String.format("DfcCompiled_%d", ordinal.getAndIncrement());
        writer.visit(Opcodes.V21, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, name, null, Type.getInternalName(Object.class), new String[]{Type.getInternalName(CompiledEntry.class)});

        RootNode rootNode = new RootNode(node);

        Context genContext = new Context(writer, name);
        genContext.newSingleMethod0((adapter, localVarConsumer) -> rootNode.doBytecodeGenSingle(genContext, adapter, localVarConsumer), "evalSingle", true);
        genContext.newMultiMethod0((adapter, localVarConsumer) -> rootNode.doBytecodeGenMulti(genContext, adapter, localVarConsumer), "evalMulti", true);

        genConstructor(genContext);
        genGetArgs(genContext);
        genNewInstance(genContext);
//        genFields(genContext);

        List<Object> args = genContext.args.entrySet().stream()
                .sorted(Comparator.comparingInt(o -> o.getValue().ordinal()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(ArrayList::new));

        ListIterator<Object> iterator = args.listIterator();
        while (iterator.hasNext()) {
            Object next = iterator.next();
            if (next instanceof DensityFunctionTypes.Wrapping wrapping) {
                iterator.set(new DensityFunctionTypes.Wrapping(wrapping.type(), compile(wrapping.wrapped())));
            }
        }

        byte[] bytes = writer.toByteArray();
        dumpClass(genContext.className, bytes);
        Class<?> defined = defineClass(genContext.className, bytes);
        try {
            return (CompiledEntry) defined.getConstructor(List.class).newInstance(args);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private static void genConstructor(Context context) {
        InstructionAdapter m = new InstructionAdapter(
                new AnalyzerAdapter(
                        context.className,
                        Opcodes.ACC_PUBLIC,
                        "<init>",
                        Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(List.class)),
                        context.classWriter.visitMethod(
                                Opcodes.ACC_PUBLIC,
                                "<init>",
                                Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(List.class)),
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

            m.load(0, InstructionAdapter.OBJECT_TYPE);
            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.iconst(ordinal);
            m.invokeinterface(Type.getInternalName(List.class), "get", Type.getMethodDescriptor(InstructionAdapter.OBJECT_TYPE, Type.INT_TYPE));
            m.checkcast(Type.getType(type));
            m.putfield(context.className, name, Type.getDescriptor(type));
        }

        m.areturn(Type.VOID_TYPE);
        m.visitLabel(end);
        m.visitLocalVariable("this", context.classDesc, null, start, end, 0);
        m.visitLocalVariable("list", Type.getDescriptor(List.class), null, start, end, 1);
        m.visitMaxs(0, 0);
    }

    private static void genGetArgs(Context context) {
        InstructionAdapter m = new InstructionAdapter(
                new AnalyzerAdapter(
                        context.className,
                        Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                        "getArgs",
                        Type.getMethodDescriptor(Type.getType(List.class)),
                        context.classWriter.visitMethod(
                                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                                "getArgs",
                                Type.getMethodDescriptor(Type.getType(List.class)),
                                null,
                                null
                        )
                )
        );

        Label start = new Label();
        Label end = new Label();
        m.visitLabel(start);

        m.anew(Type.getType(ArrayList.class));
        m.dup();
        m.iconst(context.args.size());
        m.invokespecial(Type.getInternalName(ArrayList.class), "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, Type.INT_TYPE), false);
        m.store(1, InstructionAdapter.OBJECT_TYPE);

        for (Map.Entry<Object, Context.FieldRecord> entry : context.args.entrySet().stream().sorted(Comparator.comparingInt(o -> o.getValue().ordinal())).toList()) {
            String name = entry.getValue().name();
            Class<?> type = entry.getValue().type();

            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(0, InstructionAdapter.OBJECT_TYPE);
            m.getfield(context.className, name, Type.getDescriptor(type));
            m.invokeinterface(Type.getInternalName(List.class), "add", Type.getMethodDescriptor(Type.BOOLEAN_TYPE, InstructionAdapter.OBJECT_TYPE));
            m.pop();
        }

        m.load(1, InstructionAdapter.OBJECT_TYPE);
        m.areturn(InstructionAdapter.OBJECT_TYPE);
        m.visitLabel(end);
        m.visitLocalVariable("this", context.classDesc, null, start, end, 0);
        m.visitLocalVariable("list", Type.getDescriptor(List.class), null, start, end, 1);
        m.visitMaxs(0, 0);
    }

    private static void genNewInstance(Context context) {
        InstructionAdapter m = new InstructionAdapter(
                new AnalyzerAdapter(
                        context.className,
                        Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                        "newInstance",
                        Type.getMethodDescriptor(Type.getType(CompiledEntry.class), Type.getType(List.class)),
                        context.classWriter.visitMethod(
                                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                                "newInstance",
                                Type.getMethodDescriptor(Type.getType(CompiledEntry.class), Type.getType(List.class)),
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
        m.invokespecial(context.className, "<init>", Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(List.class)), false);
        m.areturn(InstructionAdapter.OBJECT_TYPE);

        m.visitLabel(end);
        m.visitLocalVariable("this", context.classDesc, null, start, end, 0);
        m.visitLocalVariable("list", Type.getDescriptor(List.class), null, start, end, 1);
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

    private static void dumpClass(String className, byte[] bytes) {
        File outputFile = new File(exportDir, className + ".class");
        outputFile.getParentFile().mkdirs();
        try {
            com.google.common.io.Files.write(bytes, outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
        public static final String SINGLE_DESC = Type.getMethodDescriptor(Type.getType(double.class), Type.getType(int.class), Type.getType(int.class), Type.getType(int.class), Type.getType(EvalType.class));
        public static final String MULTI_DESC = Type.getMethodDescriptor(Type.VOID_TYPE, Type.getType(double[].class), Type.getType(int[].class), Type.getType(int[].class), Type.getType(int[].class), Type.getType(EvalType.class));
        private final ClassWriter classWriter;
        public final String className;
        public final String classDesc;
        private int methodIdx = 0;
        private final Reference2ObjectOpenHashMap<AstNode, String> singleMethods = new Reference2ObjectOpenHashMap<>();
        private final Reference2ObjectOpenHashMap<AstNode, String> multiMethods = new Reference2ObjectOpenHashMap<>();
        private final Reference2ObjectOpenHashMap<Object, FieldRecord> args = new Reference2ObjectOpenHashMap<>();

        public Context(ClassWriter classWriter, String className) {
            this.classWriter = Objects.requireNonNull(classWriter);
            this.className = Objects.requireNonNull(className);
            this.classDesc = String.format("L%s;", this.className);
        }

        private String nextMethodName() {
            return String.format("method_%d", methodIdx++);
        }

        private String nextMethodName(String suffix) {
            return String.format("method_%d_%s", methodIdx++, suffix);
        }

        public String newSingleMethod(AstNode node) {
            return this.singleMethods.computeIfAbsent(node, (AstNode node1) -> this.newSingleMethod((adapter, localVarConsumer) -> node1.doBytecodeGenSingle(this, adapter, localVarConsumer), nextMethodName(node.getClass().getSimpleName())));
        }

        public String newSingleMethod(BiConsumer<InstructionAdapter, LocalVarConsumer> generator) {
            return newSingleMethod(generator, nextMethodName());
        }

        public String newSingleMethod(BiConsumer<InstructionAdapter, LocalVarConsumer> generator, String name) {
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
                int ordinal = extraLocals.size() + 5;
                extraLocals.add(IntObjectPair.of(ordinal, Pair.of(localName, localDesc)));
                return ordinal;
            });
            adapter.visitLabel(end);
            adapter.visitLocalVariable("this", this.classDesc, null, start, end, 0);
            adapter.visitLocalVariable("x", Type.INT_TYPE.getDescriptor(), null, start, end, 1);
            adapter.visitLocalVariable("y", Type.INT_TYPE.getDescriptor(), null, start, end, 2);
            adapter.visitLocalVariable("z", Type.INT_TYPE.getDescriptor(), null, start, end, 3);
            adapter.visitLocalVariable("evalType", Type.getType(EvalType.class).getDescriptor(), null, start, end, 4);
            for (IntObjectPair<Pair<String, String>> local : extraLocals) {
                adapter.visitLocalVariable(local.right().left(), local.right().right(), null, start, end, local.leftInt());
            }
            adapter.visitMaxs(0, 0);
        }

        public String newMultiMethod(AstNode node) {
            return this.multiMethods.computeIfAbsent(node, (AstNode node1) -> this.newMultiMethod((adapter, localVarConsumer) -> node1.doBytecodeGenMulti(this, adapter, localVarConsumer), nextMethodName(node.getClass().getSimpleName())));
        }

        public String newMultiMethod(BiConsumer<InstructionAdapter, LocalVarConsumer> generator) {
            return newMultiMethod(generator, nextMethodName());
        }

        public String newMultiMethod(BiConsumer<InstructionAdapter, LocalVarConsumer> generator, String name) {
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
                int ordinal = extraLocals.size() + 6;
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
            for (IntObjectPair<Pair<String, String>> local : extraLocals) {
                adapter.visitLocalVariable(local.right().left(), local.right().right(), null, start, end, local.leftInt());
            }
            adapter.visitMaxs(0, 0);
        }

        public void callDelegateSingle(InstructionAdapter m, String target) {
            m.load(0, InstructionAdapter.OBJECT_TYPE);
            m.load(1, Type.INT_TYPE);
            m.load(2, Type.INT_TYPE);
            m.load(3, Type.INT_TYPE);
            m.load(4, InstructionAdapter.OBJECT_TYPE);
            m.invokevirtual(this.className, target, SINGLE_DESC, false);
        }

        public void callDelegateMulti(InstructionAdapter m, String target) {
            m.load(0, InstructionAdapter.OBJECT_TYPE);
            m.load(1, InstructionAdapter.OBJECT_TYPE);
            m.load(2, InstructionAdapter.OBJECT_TYPE);
            m.load(3, InstructionAdapter.OBJECT_TYPE);
            m.load(4, InstructionAdapter.OBJECT_TYPE);
            m.load(5, InstructionAdapter.OBJECT_TYPE);
            m.invokevirtual(this.className, target, MULTI_DESC, false);
        }

        public <T> String newField(Class<T> type, T data) {
            FieldRecord existing = this.args.get(data);
            if (existing != null) {
                return existing.name();
            }
            int size = this.args.size();
            String name = String.format("field_%d", size);
            classWriter.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_FINAL, name, Type.getDescriptor(type), null, null);
            this.args.put(data, new FieldRecord(name, size, type));
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

        public static interface LocalVarConsumer {
            int createLocalVariable(String name, String descriptor);
        }

        private static record FieldRecord(String name, int ordinal, Class<?> type) {
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
