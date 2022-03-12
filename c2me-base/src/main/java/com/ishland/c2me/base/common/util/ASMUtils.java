package com.ishland.c2me.base.common.util;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.objectweb.asm.Type;

import java.util.Arrays;

public class ASMUtils {
    public static final String INTERMEDIARY = "intermediary";
    public static final MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();

    public static String remapMethodDescriptor(String desc) {
        final Type returnType = Type.getReturnType(desc);
        final Type[] argumentTypes = Type.getArgumentTypes(desc);
        return Type.getMethodDescriptor(
                Type.getType(remapFieldDescriptor(returnType.getDescriptor())),
                Arrays.stream(argumentTypes)
                        .map(type -> Type.getType(remapFieldDescriptor(type.getDescriptor())))
                        .toArray(Type[]::new)
        );
    }

    public static String remapFieldDescriptor(String desc) {
        final Type type = Type.getType(desc);
        if (type.getSort() == Type.ARRAY) { // remap arrays
            return "[".repeat(type.getDimensions()) + remapFieldDescriptor(type.getElementType().getDescriptor());
        }
        if (type.getSort() != Type.OBJECT) { // no need to remap primitives
            return desc;
        }
        final String unmappedClassDesc = type.getClassName();
        final String unmappedClass;
        if (unmappedClassDesc.endsWith(";") && unmappedClassDesc.startsWith("L")) {
            unmappedClass = unmappedClassDesc.substring(1, unmappedClassDesc.length() - 1); // trim starting "L" and ending ";"
        } else {
            unmappedClass = unmappedClassDesc;
        }
        return 'L' + mappingResolver.mapClassName(INTERMEDIARY, unmappedClass.replace('/', '.')).replace('.', '/') + ";";
    }
}
