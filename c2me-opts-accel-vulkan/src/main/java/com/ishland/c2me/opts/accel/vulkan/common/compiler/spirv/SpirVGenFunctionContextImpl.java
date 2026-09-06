package com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv;

import com.ishland.c2me.opts.accel.vulkan.common.compiler.SpirVGen;
import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDef;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF32;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVEmitter;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenContext;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenFunctionContext;
import com.ishland.c2me.opts.dfc.common.gen.spirv.SpirVGenRegistry;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

/**
 * Emits one SPIR-V function.
 *
 * <p>Instructions are buffered rather than written straight into the module, because
 * {@code OpVariable} declarations must all precede the first real instruction of the entry
 * block. {@link #finish} assembles them in the required order.</p>
 *
 * <p>The single parameter is a {@code Function}-storage <em>pointer</em> to
 * {@code sample_int32_ctx_t}, matching how glslang lowers a struct parameter -- generated
 * functions have to share the stubs' signature to be substitutable for them.</p>
 */
public class SpirVGenFunctionContextImpl implements SpirVGenFunctionContext {

    /**
     * Member indices within {@code sample_int32_ctx_t}.
     */
    private static final int MEMBER_CONST_DATA = 0;
    private static final int MEMBER_RW_DATA = 1;
    private static final int MEMBER_X = 2;
    private static final int MEMBER_Y = 3;
    private static final int MEMBER_Z = 4;
    private static final int MEMBER_SAMPLE_FLAGS = 5;

    private final SpirVGen.ContextImpl globalContext;
    private final @Nullable SpirVGenFunctionContextImpl parent;
    private final FunctionVariant variant;

    private final int functionId;
    private final int returnTypeId;
    private final int functionTypeId;
    /**
     * Parameter types in order; the density function shape is a single ctx pointer.
     */
    private final int[] parameterTypes;
    private final int[] parameterIds;
    private final int ctxParamId;
    private final int entryLabelId;

    /**
     * OpVariable declarations; must lead the entry block. Shared with forks.
     */
    private final IntArrayList entryVariables;
    /**
     * Loads of by-pointer parameters, emitted after the entry block's variables.
     */
    private final IntArrayList entryLoads;
    private final IntArrayList body;

    /**
     * Value each parameter carries: the parameter itself, or a load of it when passed by pointer.
     */
    private final Int2IntOpenHashMap parameterValues;
    /**
     * Result type of every id this function produced, so call arguments can be type-checked.
     */
    private final Int2IntOpenHashMap valueTypes;

    /**
     * Node values already materialised in this function, for reuse across references.
     */
    private final Object2ObjectOpenHashMap<AstNode, String> vars = new Object2ObjectOpenHashMap<>();
    private final Object2IntOpenHashMap<String> localValues = new Object2IntOpenHashMap<>();

    /**
     * Lazily extracted ctx members, so a function loads each at most once.
     */
    private final Object2IntOpenHashMap<String> params = new Object2IntOpenHashMap<>();
    /**
     * Functions this one calls, so unreachable functions can be dropped before serialisation.
     */
    private final IntOpenHashSet callees;
    private int varIdx = 0;
    private boolean blockOpen = false;

    public SpirVGenFunctionContextImpl(SpirVGen.ContextImpl globalContext,
                                       @Nullable SpirVGenFunctionContextImpl parent,
                                       FunctionVariant variant,
                                       int functionId,
                                       int returnTypeId,
                                       int functionTypeId) {
        this(globalContext, parent, variant, functionId, returnTypeId, functionTypeId, null);
    }

    /**
     * @param parameterTypes explicit parameter types, or null for the density function shape
     *                       (one {@code Function}-storage pointer to sample_int32_ctx_t)
     */
    public SpirVGenFunctionContextImpl(SpirVGen.ContextImpl globalContext,
                                       @Nullable SpirVGenFunctionContextImpl parent,
                                       FunctionVariant variant,
                                       int functionId,
                                       int returnTypeId,
                                       int functionTypeId,
                                       int @Nullable [] parameterTypes) {
        this.globalContext = globalContext;
        this.parent = parent;
        this.variant = variant;
        this.functionId = functionId;
        this.returnTypeId = returnTypeId;
        this.functionTypeId = functionTypeId;
        this.parameterTypes = parameterTypes != null ? parameterTypes.clone() : null;
        if (parent != null) {
            // Forks emit into the same function, so they share its instruction stream and
            // parameters; only the value caches below are scoped.
            this.entryVariables = parent.entryVariables;
            this.entryLoads = parent.entryLoads;
            this.body = parent.body;
            this.ctxParamId = parent.ctxParamId;
            this.entryLabelId = parent.entryLabelId;
            this.parameterIds = parent.parameterIds;
        } else {
            this.entryVariables = new IntArrayList();
            this.entryLoads = new IntArrayList();
            this.body = new IntArrayList();
            int count = this.parameterTypes != null ? this.parameterTypes.length : 1;
            this.parameterIds = new int[count];
            for (int i = 0; i < count; i++) this.parameterIds[i] = globalContext.nextId();
            this.ctxParamId = this.parameterIds[0];
            this.entryLabelId = globalContext.nextId();
        }
        this.callees = parent != null ? parent.callees : new IntOpenHashSet();
        this.parameterValues = parent != null ? parent.parameterValues : new Int2IntOpenHashMap();
        this.valueTypes = parent != null ? parent.valueTypes : new Int2IntOpenHashMap();
        this.parameterValues.defaultReturnValue(0);
        this.valueTypes.defaultReturnValue(0);
        this.localValues.defaultReturnValue(0);
        this.params.defaultReturnValue(0);
        if (parent == null) {
            for (int i = 0; i < this.parameterIds.length; i++) {
                int declared = this.parameterTypes != null
                        ? this.parameterTypes[i]
                        : globalContext.typePointer(SpirV.STORAGE_CLASS_FUNCTION, globalContext.typeSampleCtx());
                this.valueTypes.put(this.parameterIds[i], declared);
            }
        }
    }

    /**
     * A block ends at its terminator, so tracking them here keeps {@link #finish} from adding a
     * second one after a terminator emitted through the raw {@link #opVoid} path.
     */
    private static boolean isTerminator(int opcode) {
        return opcode == SpirV.OP_BRANCH
                || opcode == SpirV.OP_BRANCH_CONDITIONAL
                || opcode == SpirV.OP_SWITCH
                || opcode == SpirV.OP_RETURN
                || opcode == SpirV.OP_RETURN_VALUE
                || opcode == SpirV.OP_UNREACHABLE;
    }

    @Override
    public SpirVGenContext getGlobalContext() {
        return this.globalContext;
    }

    @Override
    public FunctionVariant getVariant() {
        return this.variant;
    }

    public int getFunctionId() {
        return this.functionId;
    }

    public int getCtxParamId() {
        return this.ctxParamId;
    }

    public IntOpenHashSet getCallees() {
        return this.callees;
    }

    // ------------------------------------------------------------------
    // ctx members
    // ------------------------------------------------------------------

    /**
     * The {@code <id>} of parameter {@code index}.
     */
    public int getParamId(int index) {
        int id = this.parameterIds[index];
        if (this.parameterTypes == null) return id;

        int pointee = this.globalContext.functionPointerPointee(this.parameterTypes[index]);
        if (pointee == 0) return id;

        // glslang declared this parameter by pointer, so the body works with a load of it.
        // The load lives in the entry block, ahead of any branch that might use the value.
        int cached = this.parameterValues.get(id);
        if (cached != 0) return cached;

        int loaded = this.globalContext.nextId();
        this.entryLoads.add((4 << 16) | SpirV.OP_LOAD);
        this.entryLoads.add(pointee);
        this.entryLoads.add(loaded);
        this.entryLoads.add(id);
        this.parameterValues.put(id, loaded);
        this.valueTypes.put(loaded, pointee);
        return loaded;
    }

    /**
     * {@code OpAccessChain} into the ctx pointer, then {@code OpLoad}. Cached, since the value
     * cannot change within a function.
     *
     * <p>Emitted into the entry block rather than at the point of first use: the first use may
     * sit inside a conditional arm, and a value defined there does not dominate the merge block
     * or a sibling arm that goes on to reuse the cached id.</p>
     */
    private int member(String name, int memberIndex, int memberTypeId) {
        SpirVGenFunctionContextImpl root = root();
        int cached = root.params.getInt(name);
        if (cached != 0) return cached;

        int pointerType = this.globalContext.typePointer(SpirV.STORAGE_CLASS_FUNCTION, memberTypeId);
        int index = this.globalContext.constI32(memberIndex);

        int pointer = this.globalContext.nextId();
        this.entryLoads.add((5 << 16) | SpirV.OP_ACCESS_CHAIN);
        this.entryLoads.add(pointerType);
        this.entryLoads.add(pointer);
        this.entryLoads.add(this.ctxParamId);
        this.entryLoads.add(index);
        this.valueTypes.put(pointer, pointerType);

        int value = this.globalContext.nextId();
        this.entryLoads.add((4 << 16) | SpirV.OP_LOAD);
        this.entryLoads.add(memberTypeId);
        this.entryLoads.add(value);
        this.entryLoads.add(pointer);
        this.valueTypes.put(value, memberTypeId);

        root.params.put(name, value);
        return value;
    }

    @Override
    public int paramConstData() {
        return member("const_data", MEMBER_CONST_DATA, this.globalContext.typeInt(64, false));
    }

    @Override
    public int paramRwData() {
        return member("rw_data", MEMBER_RW_DATA, this.globalContext.typeInt(64, false));
    }

    @Override
    public int paramX() {
        return member("x", MEMBER_X, this.globalContext.typeInt(32, true));
    }

    @Override
    public int paramY() {
        return member("y", MEMBER_Y, this.globalContext.typeInt(32, true));
    }

    @Override
    public int paramZ() {
        return member("z", MEMBER_Z, this.globalContext.typeInt(32, true));
    }

    // ------------------------------------------------------------------
    // instruction emission
    // ------------------------------------------------------------------

    @Override
    public int paramSampleFlags() {
        return member("sample_flags", MEMBER_SAMPLE_FLAGS, this.globalContext.typeInt(32, false));
    }

    @Override
    public int op(int opcode, int resultType, int... operands) {
        if (opcode == SpirV.OP_FUNCTION_CALL && operands.length > 0) {
            this.callees.add(operands[0]);
            operands = adaptCallArguments(operands);
        }
        int result = this.globalContext.nextId();
        this.valueTypes.put(result, resultType);
        this.body.add(((operands.length + 3) << 16) | opcode);
        this.body.add(resultType);
        this.body.add(result);
        for (int operand : operands) this.body.add(operand);
        return result;
    }

    /**
     * Rewrites a call's arguments to the callee's declared parameter types. glslang passes a
     * parameter it writes to by pointer, so a value handed to one has to be materialised into a
     * {@code Function} variable first -- which is what glslang's own call sites do.
     */
    private int[] adaptCallArguments(int[] operands) {
        int[] declared = this.globalContext.parameterTypesOfFunction(operands[0]);
        if (declared == null || declared.length != operands.length - 1) return operands;

        int[] adapted = null;
        for (int i = 0; i < declared.length; i++) {
            int argument = operands[i + 1];
            int pointee = this.globalContext.functionPointerPointee(declared[i]);
            // Already the pointer the callee wants, or wanted by value: pass it through.
            if (pointee == 0 || this.valueTypes.get(argument) == declared[i]) continue;

            int variable = newLocal(pointee);
            store(variable, argument);
            if (adapted == null) adapted = operands.clone();
            adapted[i + 1] = variable;
        }
        return adapted != null ? adapted : operands;
    }

    @Override
    public void opVoid(int opcode, int... operands) {
        this.body.add(((operands.length + 1) << 16) | opcode);
        for (int operand : operands) this.body.add(operand);
        if (isTerminator(opcode)) root().blockOpen = false;
    }

    @Override
    public int extInst(int extSetId, int instruction, int resultType, int... operands) {
        int[] full = new int[operands.length + 2];
        full[0] = extSetId;
        full[1] = instruction;
        System.arraycopy(operands, 0, full, 2, operands.length);
        return op(SpirV.OP_EXT_INST, resultType, full);
    }

    // ------------------------------------------------------------------
    // locals
    // ------------------------------------------------------------------

    @Override
    public int newLocal(int type) {
        int pointerType = this.globalContext.typePointer(SpirV.STORAGE_CLASS_FUNCTION, type);
        int result = this.globalContext.nextId();
        this.entryVariables.add((4 << 16) | SpirV.OP_VARIABLE);
        this.entryVariables.add(pointerType);
        this.entryVariables.add(result);
        this.entryVariables.add(SpirV.STORAGE_CLASS_FUNCTION);
        this.valueTypes.put(result, pointerType);
        return result;
    }

    @Override
    public void store(int pointer, int value) {
        opVoid(SpirV.OP_STORE, pointer, value);
    }

    @Override
    public int load(int resultType, int pointer) {
        return op(SpirV.OP_LOAD, resultType, pointer);
    }

    // ------------------------------------------------------------------
    // delegates
    // ------------------------------------------------------------------

    @Override
    public int getDelegateVar(ValuesMethodDef target, AstNode.ReturnType returnType) {
        return switch (returnType) {
            case F64 -> getDelegateVar((ValuesMethodDefF64) target);
            case F32 -> getDelegateVar((ValuesMethodDefF32) target);
        };
    }

    @Override
    public int getDelegateVar(ValuesMethodDefF64 target) {
        if (target.isConst()) return this.globalContext.constF64(target.constValue());
        return resolve(target.generatedMethod(), this.globalContext.typeFloat(64));
    }

    @Override
    public int getDelegateVar(ValuesMethodDefF32 target) {
        if (target.isConst()) return this.globalContext.constF32(target.constValue());
        return resolve(target.generatedMethod(), this.globalContext.typeFloat(32));
    }

    /**
     * A name refers either to a value already computed in this function (or an enclosing one),
     * or to a generated function that must be called.
     */
    private int resolve(String name, int resultType) {
        for (SpirVGenFunctionContextImpl scope = this; scope != null; scope = scope.parent) {
            int local = scope.localValues.getInt(name);
            if (local != 0) return local;
        }
        int function = this.globalContext.functionIdByName(name);
        if (function == 0) {
            throw new IllegalStateException("No local value or generated function named " + name);
        }
        return op(SpirV.OP_FUNCTION_CALL, resultType, function, this.ctxParamId);
    }

    // ------------------------------------------------------------------
    // node materialisation
    // ------------------------------------------------------------------

    @Override
    public ValuesMethodDef newVar(AstNode node) {
        return switch (node.getReturnType()) {
            case F64 -> newVarF64(node);
            case F32 -> newVarF32(node);
        };
    }

    @Override
    public ValuesMethodDefF64 newVarF64(AstNode node) {
        return new ValuesMethodDefF64(newVar0(node));
    }

    @Override
    public ValuesMethodDefF32 newVarF32(AstNode node) {
        return new ValuesMethodDefF32(newVar0(node));
    }

    /**
     * Emits {@code node} inline and remembers its result, so a second reference reuses it.
     */
    private String newVar0(AstNode node) {
        String existing = getVarIfPresent(node);
        if (existing != null) return existing;

        String name = "v" + (this.varIdx++) + "@" + this.functionId;
        int result = emitNode(node);
        this.localValues.put(name, result);
        this.vars.put(node, name);
        return name;
    }

    private @Nullable String getVarIfPresent(AstNode node) {
        for (SpirVGenFunctionContextImpl scope = this; scope != null; scope = scope.parent) {
            String name = scope.vars.get(node);
            if (name != null) return name;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private int emitNode(AstNode node) {
        SpirVEmitter<AstNode> emitter =
                (SpirVEmitter<AstNode>) SpirVGenRegistry.REGISTRY.get((Class<AstNode>) node.getClass());
        if (emitter == null) {
            throw new UnsupportedOperationException("No SPIR-V emitter for " + node.getClass().getName());
        }
        return emitter.doSpirVGen(node, this);
    }

    // ------------------------------------------------------------------
    // control flow
    // ------------------------------------------------------------------

    @Override
    public int newLabel() {
        return this.globalContext.nextId();
    }

    private SpirVGenFunctionContextImpl root() {
        SpirVGenFunctionContextImpl scope = this;
        while (scope.parent != null) scope = scope.parent;
        return scope;
    }

    @Override
    public void label(int labelId) {
        this.body.add((2 << 16) | SpirV.OP_LABEL);
        this.body.add(labelId);
        root().blockOpen = true;
    }

    @Override
    public void selectionMerge(int mergeLabel) {
        opVoid(SpirV.OP_SELECTION_MERGE, mergeLabel, SpirV.SELECTION_CONTROL_NONE);
    }

    @Override
    public void loopMerge(int mergeLabel, int continueLabel) {
        opVoid(SpirV.OP_LOOP_MERGE, mergeLabel, continueLabel, SpirV.LOOP_CONTROL_NONE);
    }

    @Override
    public void branch(int targetLabel) {
        opVoid(SpirV.OP_BRANCH, targetLabel);
        root().blockOpen = false;
    }

    @Override
    public void branchConditional(int condition, int trueLabel, int falseLabel) {
        opVoid(SpirV.OP_BRANCH_CONDITIONAL, condition, trueLabel, falseLabel);
        root().blockOpen = false;
    }

    @Override
    public void switchOn(int selector, int defaultLabel, int[] literals, int[] caseLabels) {
        if (literals.length != caseLabels.length) {
            throw new IllegalArgumentException("switch literals and labels differ in length");
        }
        int[] operands = new int[2 + literals.length * 2];
        operands[0] = selector;
        operands[1] = defaultLabel;
        for (int i = 0; i < literals.length; i++) {
            operands[2 + i * 2] = literals[i];
            operands[3 + i * 2] = caseLabels[i];
        }
        opVoid(SpirV.OP_SWITCH, operands);
        root().blockOpen = false;
    }

    @Override
    public void returnValue(int value) {
        opVoid(SpirV.OP_RETURN_VALUE, value);
        root().blockOpen = false;
    }

    @Override
    public SpirVGenFunctionContext fork() {
        return new SpirVGenFunctionContextImpl(this.globalContext, this, this.variant,
                this.functionId, this.returnTypeId, this.functionTypeId, this.parameterTypes);
    }

    // ------------------------------------------------------------------
    // assembly
    // ------------------------------------------------------------------

    /**
     * Writes the function into {@code module}. Every block must be terminated, so an open
     * trailing block is closed with {@code OpUnreachable}. Only the root context may be
     * finished; forks share its buffers.
     */
    public void finish(SpirVModule module) {
        if (this.parent != null) {
            throw new IllegalStateException("finish() called on a forked context");
        }
        if (this.blockOpen) {
            this.body.add((1 << 16) | SpirV.OP_UNREACHABLE);
            this.blockOpen = false;
        }

        IntArrayList out = new IntArrayList();

        out.add((5 << 16) | SpirV.OP_FUNCTION);
        out.add(this.returnTypeId);
        out.add(this.functionId);
        out.add(SpirV.FUNCTION_CONTROL_NONE);
        out.add(this.functionTypeId);

        if (this.parameterTypes != null) {
            for (int i = 0; i < this.parameterTypes.length; i++) {
                out.add((3 << 16) | SpirV.OP_FUNCTION_PARAMETER);
                out.add(this.parameterTypes[i]);
                out.add(this.parameterIds[i]);
            }
        } else {
            int ctxPointerType = this.globalContext.typePointer(
                    SpirV.STORAGE_CLASS_FUNCTION, this.globalContext.typeSampleCtx());
            out.add((3 << 16) | SpirV.OP_FUNCTION_PARAMETER);
            out.add(ctxPointerType);
            out.add(this.ctxParamId);
        }

        out.add((2 << 16) | SpirV.OP_LABEL);
        out.add(this.entryLabelId);

        out.addAll(this.entryVariables);
        out.addAll(this.entryLoads);
        out.addAll(this.body);

        out.add((1 << 16) | SpirV.OP_FUNCTION_END);

        module.appendWords(SpirVModule.Section.FUNCTION_DEFINITIONS, out);
    }

}
