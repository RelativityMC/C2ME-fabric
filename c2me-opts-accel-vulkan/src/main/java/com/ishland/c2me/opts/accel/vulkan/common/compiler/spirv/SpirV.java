package com.ishland.c2me.opts.accel.vulkan.common.compiler.spirv;

/**
 * SPIR-V numeric constants, limited to what the density function backend emits.
 * Values are from the SPIR-V specification, unified section 3.
 */
public final class SpirV {

    public static final int MAGIC = 0x07230203;
    /**
     * SPIR-V 1.5; guaranteed available in Vulkan 1.2.
     */
    public static final int VERSION_1_5 = 0x00010500;
    /**
     * Tool id 0 ("unknown"); no registered generator magic number for C2ME.
     */
    public static final int GENERATOR = 0;
    // ---- capabilities -------------------------------------------------
    public static final int CAPABILITY_SHADER = 1;
    public static final int CAPABILITY_FLOAT64 = 10;
    public static final int CAPABILITY_INT64 = 11;
    public static final int CAPABILITY_INT16 = 22;
    public static final int CAPABILITY_PHYSICAL_STORAGE_BUFFER_ADDRESSES = 5347;
    // ---- models -------------------------------------------------------
    public static final int ADDRESSING_MODEL_LOGICAL = 0;
    public static final int ADDRESSING_MODEL_PHYSICAL_STORAGE_BUFFER64 = 5348;
    public static final int MEMORY_MODEL_GLSL450 = 1;
    public static final int MEMORY_MODEL_VULKAN = 3;
    public static final int EXECUTION_MODEL_GL_COMPUTE = 5;
    public static final int EXECUTION_MODE_LOCAL_SIZE = 17;
    // ---- storage classes ----------------------------------------------
    public static final int STORAGE_CLASS_UNIFORM_CONSTANT = 0;
    public static final int STORAGE_CLASS_INPUT = 1;
    public static final int STORAGE_CLASS_WORKGROUP = 4;
    public static final int STORAGE_CLASS_PRIVATE = 6;
    public static final int STORAGE_CLASS_FUNCTION = 7;
    public static final int STORAGE_CLASS_PUSH_CONSTANT = 9;
    public static final int STORAGE_CLASS_STORAGE_BUFFER = 12;
    public static final int STORAGE_CLASS_PHYSICAL_STORAGE_BUFFER = 5349;
    // ---- decorations ---------------------------------------------------
    public static final int DECORATION_SPEC_ID = 1;
    public static final int DECORATION_BLOCK = 2;
    public static final int DECORATION_ARRAY_STRIDE = 6;
    public static final int DECORATION_BUILT_IN = 11;
    public static final int DECORATION_NON_WRITABLE = 24;
    public static final int DECORATION_BINDING = 33;
    public static final int DECORATION_DESCRIPTOR_SET = 34;
    public static final int DECORATION_OFFSET = 35;
    public static final int DECORATION_RESTRICT_POINTER = 5355;
    public static final int DECORATION_ALIASED_POINTER = 5356;
    // ---- builtins -------------------------------------------------------
    public static final int BUILT_IN_NUM_WORKGROUPS = 24;
    public static final int BUILT_IN_WORKGROUP_ID = 26;
    public static final int BUILT_IN_LOCAL_INVOCATION_ID = 27;
    public static final int BUILT_IN_GLOBAL_INVOCATION_ID = 28;
    // ---- function control (bitfield) -------------------------------------
    public static final int FUNCTION_CONTROL_NONE = 0x0;
    public static final int FUNCTION_CONTROL_INLINE = 0x1;
    public static final int FUNCTION_CONTROL_DONT_INLINE = 0x2;
    public static final int FUNCTION_CONTROL_PURE = 0x4;
    public static final int FUNCTION_CONTROL_CONST = 0x8;
    // ---- selection / loop control ----------------------------------------
    public static final int SELECTION_CONTROL_NONE = 0x0;
    public static final int LOOP_CONTROL_NONE = 0x0;
    // ---- memory operands --------------------------------------------------
    public static final int MEMORY_ACCESS_NONE = 0x0;
    public static final int MEMORY_ACCESS_ALIGNED = 0x2;
    // ---- opcodes ---------------------------------------------------------
    public static final int OP_NOP = 0;
    public static final int OP_UNDEF = 1;
    public static final int OP_SOURCE_CONTINUED = 2;
    public static final int OP_SOURCE = 3;
    public static final int OP_SOURCE_EXTENSION = 4;
    public static final int OP_NAME = 5;
    public static final int OP_MEMBER_NAME = 6;
    public static final int OP_STRING = 7;
    public static final int OP_LINE = 8;
    public static final int OP_EXTENSION = 10;
    public static final int OP_EXT_INST_IMPORT = 11;
    public static final int OP_EXT_INST = 12;
    public static final int OP_MEMORY_MODEL = 14;
    public static final int OP_ENTRY_POINT = 15;
    public static final int OP_EXECUTION_MODE = 16;
    public static final int OP_CAPABILITY = 17;
    public static final int OP_TYPE_VOID = 19;
    public static final int OP_TYPE_BOOL = 20;
    public static final int OP_TYPE_INT = 21;
    public static final int OP_TYPE_FLOAT = 22;
    public static final int OP_TYPE_VECTOR = 23;
    public static final int OP_TYPE_ARRAY = 28;
    public static final int OP_TYPE_RUNTIME_ARRAY = 29;
    public static final int OP_TYPE_STRUCT = 30;
    public static final int OP_TYPE_POINTER = 32;
    public static final int OP_TYPE_FUNCTION = 33;
    public static final int OP_CONSTANT_TRUE = 41;
    public static final int OP_CONSTANT_FALSE = 42;
    public static final int OP_CONSTANT = 43;
    public static final int OP_CONSTANT_COMPOSITE = 44;
    public static final int OP_SPEC_CONSTANT_TRUE = 48;
    public static final int OP_SPEC_CONSTANT_FALSE = 49;
    public static final int OP_SPEC_CONSTANT = 50;
    public static final int OP_SPEC_CONSTANT_COMPOSITE = 51;
    public static final int OP_SPEC_CONSTANT_OP = 52;
    public static final int OP_FUNCTION = 54;
    public static final int OP_FUNCTION_PARAMETER = 55;
    public static final int OP_FUNCTION_END = 56;
    public static final int OP_FUNCTION_CALL = 57;
    public static final int OP_VARIABLE = 59;
    public static final int OP_LOAD = 61;
    public static final int OP_STORE = 62;
    public static final int OP_ACCESS_CHAIN = 65;
    public static final int OP_DECORATE = 71;
    public static final int OP_MEMBER_DECORATE = 72;
    public static final int OP_DECORATION_GROUP = 73;
    public static final int OP_GROUP_DECORATE = 74;
    public static final int OP_GROUP_MEMBER_DECORATE = 75;
    public static final int OP_COMPOSITE_CONSTRUCT = 80;
    public static final int OP_COMPOSITE_EXTRACT = 81;
    public static final int OP_CONVERT_PTR_TO_U = 117;
    public static final int OP_CONVERT_U_TO_PTR = 120;
    public static final int OP_CONVERT_F_TO_U = 109;
    public static final int OP_CONVERT_F_TO_S = 110;
    public static final int OP_CONVERT_S_TO_F = 111;
    public static final int OP_CONVERT_U_TO_F = 112;
    public static final int OP_U_CONVERT = 113;
    public static final int OP_S_CONVERT = 114;
    public static final int OP_F_CONVERT = 115;
    public static final int OP_BITCAST = 124;
    public static final int OP_S_NEGATE = 126;
    public static final int OP_F_NEGATE = 127;
    public static final int OP_I_ADD = 128;
    public static final int OP_F_ADD = 129;
    public static final int OP_I_SUB = 130;
    public static final int OP_F_SUB = 131;
    public static final int OP_I_MUL = 132;
    public static final int OP_F_MUL = 133;
    public static final int OP_U_DIV = 134;
    public static final int OP_S_DIV = 135;
    public static final int OP_F_DIV = 136;
    public static final int OP_U_MOD = 137;
    public static final int OP_S_REM = 138;
    public static final int OP_S_MOD = 139;
    public static final int OP_F_REM = 140;
    public static final int OP_F_MOD = 141;
    public static final int OP_LOGICAL_OR = 166;
    public static final int OP_LOGICAL_AND = 167;
    public static final int OP_LOGICAL_NOT = 168;
    public static final int OP_SELECT = 169;
    public static final int OP_I_EQUAL = 170;
    public static final int OP_I_NOT_EQUAL = 171;
    public static final int OP_U_GREATER_THAN = 172;
    public static final int OP_S_GREATER_THAN = 173;
    public static final int OP_U_GREATER_THAN_EQUAL = 174;
    public static final int OP_S_GREATER_THAN_EQUAL = 175;
    public static final int OP_U_LESS_THAN = 176;
    public static final int OP_S_LESS_THAN = 177;
    public static final int OP_U_LESS_THAN_EQUAL = 178;
    public static final int OP_S_LESS_THAN_EQUAL = 179;
    public static final int OP_F_ORD_EQUAL = 180;
    public static final int OP_F_ORD_NOT_EQUAL = 182;
    public static final int OP_F_ORD_LESS_THAN = 184;
    public static final int OP_F_ORD_GREATER_THAN = 186;
    public static final int OP_F_ORD_LESS_THAN_EQUAL = 188;
    public static final int OP_F_ORD_GREATER_THAN_EQUAL = 190;
    public static final int OP_SHIFT_RIGHT_LOGICAL = 194;
    public static final int OP_SHIFT_RIGHT_ARITHMETIC = 195;
    public static final int OP_SHIFT_LEFT_LOGICAL = 196;
    public static final int OP_BITWISE_OR = 197;
    public static final int OP_BITWISE_XOR = 198;
    public static final int OP_BITWISE_AND = 199;
    public static final int OP_NOT = 200;
    public static final int OP_PHI = 245;
    public static final int OP_LOOP_MERGE = 246;
    public static final int OP_SELECTION_MERGE = 247;
    public static final int OP_LABEL = 248;
    public static final int OP_BRANCH = 249;
    public static final int OP_BRANCH_CONDITIONAL = 250;
    public static final int OP_SWITCH = 251;
    public static final int OP_RETURN = 253;
    public static final int OP_RETURN_VALUE = 254;
    public static final int OP_UNREACHABLE = 255;
    public static final int OP_NO_LINE = 317;
    public static final int OP_MODULE_PROCESSED = 330;
    public static final int OP_EXECUTION_MODE_ID = 331;
    public static final int OP_DECORATE_ID = 332;
    public static final int OP_DECORATE_STRING = 5632;
    public static final int OP_MEMBER_DECORATE_STRING = 5633;
    /**
     * OpTypeVoid .. OpTypeForwardPointer occupy a contiguous opcode range.
     */
    public static final int OP_TYPE_FIRST = 19;
    public static final int OP_TYPE_LAST = 39;
    public static final int OP_TYPE_FORWARD_POINTER = 39;
    private SpirV() {
    }

    /**
     * Instruction numbers within the {@code GLSL.std.450} extended instruction set.
     */
    public static final class Glsl450 {

        public static final String NAME = "GLSL.std.450";
        public static final int ROUND = 1;
        public static final int TRUNC = 3;
        public static final int F_ABS = 4;
        public static final int S_ABS = 5;
        public static final int F_SIGN = 6;
        public static final int FLOOR = 8;
        public static final int CEIL = 9;
        public static final int FRACT = 10;
        public static final int SIN = 13;
        public static final int COS = 14;
        public static final int POW = 26;
        public static final int EXP = 27;
        public static final int LOG = 28;
        public static final int SQRT = 31;
        public static final int F_MIN = 37;
        public static final int U_MIN = 38;
        public static final int S_MIN = 39;
        public static final int F_MAX = 40;
        public static final int U_MAX = 41;
        public static final int S_MAX = 42;
        public static final int F_CLAMP = 43;
        public static final int F_MIX = 46;
        public static final int STEP = 48;
        public static final int SMOOTH_STEP = 49;
        public static final int FMA = 50;
        private Glsl450() {
        }
    }

}
