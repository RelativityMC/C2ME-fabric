package com.ishland.c2me.asm;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ASMTransformerMakeVolatile {

    private static final String INTERMEDIARY = "intermediary";

    private static final Map<String, List<String>> makeVolatileFields = new HashMap<>();
    private static final Map<String, List<String>> makeVolatileFieldsMapped;
    private static final MappingResolver mappingResolver = FabricLoader.getInstance().getMappingResolver();

    static {
        makeVolatileFields.put(
                "net/minecraft/class_3353$class_3354", // net/minecraft/structure/MineshaftGenerator$MineshaftCorridor
                List.of(
                        "field_14414:Z" // hasSpawner:Z
                )
        );
        makeVolatileFields.put(
                "net/minecraft/class_3390$class_3402", // net/minecraft/structure/NetherFortressGenerator$BridgePlatform
                List.of(
                        "field_14498:Z" // hasBlazeSpawner:Z
                )
        );
        makeVolatileFields.put(
                "net/minecraft/class_3390$class_3398", // net/minecraft/structure/NetherFortressGenerator$CorridorLeftTurn
                List.of(
                        "field_14496:Z" // containsChest:Z
                )
        );
        makeVolatileFields.put(
                "net/minecraft/class_3390$class_3400", // net/minecraft/structure/NetherFortressGenerator$CorridorRightTurn
                List.of(
                        "field_14497:Z" // containsChest:Z
                )
        );
        makeVolatileFields.put(
                "net/minecraft/class_3390$class_3407", // net/minecraft/structure/NetherFortressGenerator$Start
                List.of(
                        "field_14506:Lnet/minecraft/class_3390$class_3404;" // lastPiece:Lnet/minecraft/structure/NetherFortressGenerator$PieceData;
                )
        );
        makeVolatileFields.put(
                "net/minecraft/class_3366$class_3388", // net/minecraft/structure/OceanMonumentGenerator$PieceSetting
                List.of(
                        "field_14485:Z", // used:Z
                        "field_14484:Z", // not mapped by yarn
                        "field_14483:I"  // not mapped by yarn
                )
        );
        makeVolatileFields.put(
                "net/minecraft/class_3366$class_3374", // net/minecraft/structure/OceanMonumentGenerator$Base
                List.of(
                        "field_14464:Lnet/minecraft/class_3366$class_3388;", // not mapped by yarn
                        "field_14466:Lnet/minecraft/class_3366$class_3388;"  // not mapped by yarn
                )
        );
        makeVolatileFields.put(
                "net/minecraft/class_3470", // net/minecraft/structure/SimpleStructurePiece
                List.of(
                        "field_15432:Lnet/minecraft/class_2338;" // pos:Lnet/minecraft/util/math/BlockPos;
                )
        );
        makeVolatileFields.put(
                "net/minecraft/class_3421$class_3422", // net/minecraft/structure/StrongholdGenerator$ChestCorridor
                List.of(
                        "field_15268:Z" // chestGenerated:Z
                )
        );
        makeVolatileFields.put(
                "net/minecraft/class_3421$class_3428", // net/minecraft/structure/StrongholdGenerator$PortalRoom
                List.of(
                        "field_15279:Z" // spawnerPlaced:Z
                )
        );
        makeVolatileFields.put(
                "net/minecraft/class_3421$class_3434", // net/minecraft/structure/StrongholdGenerator$Start
                List.of(
                        "field_15284:Lnet/minecraft/class_3421$class_3427;", // lastPiece:Lnet/minecraft/structure/StrongholdGenerator$PieceData;
                        "field_15283:Lnet/minecraft/class_3421$class_3428;"  // portalRoom:Lnet/minecraft/structure/StrongholdGenerator$PortalRoom;
                )
        );
        makeVolatileFields.put(
                "net/minecraft/class_3418", // net/minecraft/structure/StructurePieceWithDimensions
                List.of(
                        "field_15241:I" // hPos:I
                )
        );
        makeVolatileFields.put(
                "net/minecraft/class_3447", // net/minecraft/structure/SwampHutGenerator
                List.of(
                        "field_15322:Z", // hasWitch:Z
                        "field_16445:Z"  // hasCat:Z
                )
        );
        makeVolatileFields.put(
                "net/minecraft/class_3471$class_3476", // net/minecraft/structure/WoodlandMansionGenerator$GenerationPiece
                List.of(
                        "field_15450:Lnet/minecraft/class_2470;", // rotation:Lnet/minecraft/util/BlockRotation;
                        "field_15449:Lnet/minecraft/class_2338;", // position:Lnet/minecraft/util/math/BlockPos;
                        "field_15448:Ljava/lang/String;"          // template:Ljava/lang/String;
                )
        );

        makeVolatileFieldsMapped = makeVolatileFields.entrySet().stream()
                .map(entry -> {
                    String mappedClassName = mappingResolver.mapClassName(INTERMEDIARY, entry.getKey().replace('/', '.')).replace('.', '/');
                    List<String> mappedFieldNames = entry.getValue().stream()
                            .map(fieldName -> {
                                String[] split = fieldName.split(":");
                                return mappingResolver.mapFieldName(INTERMEDIARY, entry.getKey().replace('/', '.'), split[0], split[1]) + ":" + remapDescriptor(split[1]);
                            }).toList();
                    return new KeyValue<>(mappedClassName, mappedFieldNames);
                }).collect(Collectors.toMap(KeyValue::key, KeyValue::value));
    }

    static String remapDescriptor(String desc) {
        final Type type = Type.getType(desc);
        if (type.getSort() == Type.ARRAY) { // remap arrays
            return "[".repeat(type.getDimensions()) + remapDescriptor(type.getElementType().getDescriptor());
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

    public static void transform(ClassNode classNode) {
        final List<String> pendingFields = makeVolatileFieldsMapped.get(classNode.name);
        if (pendingFields != null) {
            ASMMixinPlugin.LOGGER.info("Transforming class {}", classNode.name.replace('/', '.'));
            classNode.fields.stream()
                    .filter(fieldNode -> pendingFields.contains(fieldNode.name + ":" + fieldNode.desc))
                    .forEach(fieldNode -> {
                        ASMMixinPlugin.LOGGER.info("Making field L{};{}:{} volatile", classNode.name, fieldNode.name, fieldNode.desc);
                        fieldNode.access |= Opcodes.ACC_VOLATILE;
                    });
        }
    }

    private record KeyValue<K, V>(K key, V value) {
    }

}
