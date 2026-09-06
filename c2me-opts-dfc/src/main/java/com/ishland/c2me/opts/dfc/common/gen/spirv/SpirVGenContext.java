package com.ishland.c2me.opts.dfc.common.gen.spirv;

import com.ishland.c2me.opts.dfc.common.ast.AstNode;
import com.ishland.c2me.opts.dfc.common.ast.misc.CacheLikeNode;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDef;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF32;
import com.ishland.c2me.opts.dfc.common.gen.meta.ValuesMethodDefF64;

/**
 * Module-level SPIR-V generation context. Mirrors
 * {@link com.ishland.c2me.opts.dfc.common.gen.opencl.OpenCLCGenContext}.
 *
 * <p><b>Note on int returns.</b> Two unrelated kinds of {@code int} appear here:
 * SPIR-V {@code <id>}s (from {@code nextId}, {@code type*}, {@code const*}) and
 * byte offsets into the const/dynamic data buffers (from {@code alloc*} /
 * {@code getGlobalDynamicDataOffset}). They are not interchangeable.</p>
 */
public interface SpirVGenContext {

    // ---- <id> allocation -------------------------------------------------

    int nextId();

    /** A fresh, unique name for a generated function. */
    String nextMethodName();

    /** Attaches an OpName for debugging; no-op when debug info is disabled. */
    void debugName(int target, String name);

    // ---- type table (deduplicated) ---------------------------------------

    int typeVoid();
    int typeBool();
    int typeInt(int width, boolean signed);
    int typeFloat(int width);
    int typeVector(int componentType, int count);
    int typeArray(int elementType, int lengthConstantId);
    int typeRuntimeArray(int elementType);
    int typeStruct(int... memberTypes);
    int typePointer(int storageClass, int pointeeType);
    int typeFunction(int returnType, int... parameterTypes);

    /** Convenience: the {@code sample_int32_ctx_t} equivalent passed to every generated function. */
    int typeSampleCtx();

    /** The prelude's {@code cache_result_t}: {@code { bool cached; double res; }}. */
    int typeSampleCacheResult();

    // ---- constants (deduplicated) ----------------------------------------

    int constBool(boolean value);
    int constI32(int value);
    int constU32(int value);
    int constU64(long value);
    int constF32(float value);
    int constF64(double value);

    // ---- module preamble --------------------------------------------------

    void capability(int capability);
    void extension(String name);
    int extInstImport(String name);
    void decorate(int target, int decoration, int... operands);
    void memberDecorate(int structType, int member, int decoration, int... operands);

    /**
     * Declares a {@code PhysicalStorageBuffer} pointer type for {@code pointeeType},
     * emitting the {@code ArrayStride}/{@code Offset} decorations required by
     * scalar block layout. Deduplicated.
     */
    int bufferReference(int pointeeType, int align);

    // ---- functions --------------------------------------------------------

    ValuesMethodDef newDispatcher(AstNode node, String id, AstNode.ReturnType returnType);
    ValuesMethodDefF64 newDispatcherF64(AstNode node);
    ValuesMethodDefF64 newDispatcherF64(AstNode node, String id);
    ValuesMethodDefF32 newDispatcherF32(AstNode node);
    ValuesMethodDefF32 newDispatcherF32(AstNode node, String id);

    ValuesMethodDef newMethod(AstNode node, SpirVGenFunctionContext.FunctionVariant variant, AstNode.ReturnType returnType);
    ValuesMethodDefF64 newMethodF64(AstNode node, SpirVGenFunctionContext.FunctionVariant variant);
    ValuesMethodDefF32 newMethodF32(AstNode node, SpirVGenFunctionContext.FunctionVariant variant);

    /** @return the {@code <id>} of the {@code OpFunction} backing {@code target}. */
    int functionId(ValuesMethodDef target);

    /**
     * {@code <id>} of a helper the prelude defines, for emitting a direct {@code OpFunctionCall}
     * with arbitrary arguments (unlike generated density function code, which always takes the
     * sample context pointer).
     *
     * <p>The prelude must keep the helper reachable from an entry point, or glslang eliminates
     * it before it can be resolved here.</p>
     *
     * @throws IllegalStateException if the prelude does not define it
     */
    int preludeFunctionId(String name);

    // ---- buffer-resident data (byte offsets, not <id>s) -------------------

    int allocGlobalDynamicData(Object data);
    int allocGlobalConstData(byte[] data, int alignment);
    int allocGlobalConstDataObject(Object obj);
    int getGlobalDynamicDataOffset(Object data);

    int registerFlatCache(CacheLikeNode node);
    int registerCache2d(CacheLikeNode node);
    int registerInterpolator(CacheLikeNode node);

}
