package com.ishland.c2me.compatibility.mixin.betternether;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import paulevs.betternether.biomes.NetherBiome;
import paulevs.betternether.structures.StructureCaves;
import paulevs.betternether.structures.StructurePath;
import paulevs.betternether.world.BNWorldGenerator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Mixin(BNWorldGenerator.class)
public class MixinBNWorldGenerator {

    private static final ThreadLocal<List<BlockPos>> LIST_FLOOR_TL = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<List<BlockPos>> LIST_WALL_TL = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<List<BlockPos>> LIST_CEIL_TL = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<List<BlockPos>> LIST_LAVA_TL = ThreadLocal.withInitial(ArrayList::new);
    private static final ThreadLocal<HashSet<Biome>> MC_BIOMES_TL = ThreadLocal.withInitial(HashSet::new);

    private static final AtomicReference<StructureCaves> caves_TL = new AtomicReference<>();
    private static final AtomicReference<StructurePath> paths_TL = new AtomicReference<>();
    private static final ThreadLocal<NetherBiome> biome_TL = new ThreadLocal<>();

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lpaulevs/betternether/world/BNWorldGenerator;LIST_FLOOR:Ljava/util/List;", opcode = Opcodes.GETSTATIC))
    private static List<BlockPos> redirectGetListFloor() {
        return LIST_FLOOR_TL.get();
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lpaulevs/betternether/world/BNWorldGenerator;LIST_WALL:Ljava/util/List;", opcode = Opcodes.GETSTATIC))
    private static List<BlockPos> redirectGetListWall() {
        return LIST_WALL_TL.get();
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lpaulevs/betternether/world/BNWorldGenerator;LIST_CEIL:Ljava/util/List;", opcode = Opcodes.GETSTATIC))
    private static List<BlockPos> redirectGetListCeil() {
        return LIST_CEIL_TL.get();
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lpaulevs/betternether/world/BNWorldGenerator;LIST_LAVA:Ljava/util/List;", opcode = Opcodes.GETSTATIC))
    private static List<BlockPos> redirectGetListLava() {
        return LIST_LAVA_TL.get();
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lpaulevs/betternether/world/BNWorldGenerator;MC_BIOMES:Ljava/util/HashSet;", opcode = Opcodes.GETSTATIC))
    private static HashSet<Biome> redirectGetMcBiomes() {
        return MC_BIOMES_TL.get();
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lpaulevs/betternether/world/BNWorldGenerator;caves:Lpaulevs/betternether/structures/StructureCaves;", opcode = Opcodes.PUTSTATIC))
    private static void redirectSetCaves(StructureCaves value) {
        caves_TL.set(value);
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lpaulevs/betternether/world/BNWorldGenerator;caves:Lpaulevs/betternether/structures/StructureCaves;", opcode = Opcodes.GETSTATIC))
    private static StructureCaves redirectGetCaves() {
        return caves_TL.get();
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lpaulevs/betternether/world/BNWorldGenerator;paths:Lpaulevs/betternether/structures/StructurePath;", opcode = Opcodes.PUTSTATIC))
    private static void redirectSetPaths(StructurePath value) {
        paths_TL.set(value);
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lpaulevs/betternether/world/BNWorldGenerator;paths:Lpaulevs/betternether/structures/StructurePath;", opcode = Opcodes.GETSTATIC))
    private static StructurePath redirectGetPaths() {
        return paths_TL.get();
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lpaulevs/betternether/world/BNWorldGenerator;biome:Lpaulevs/betternether/biomes/NetherBiome;", opcode = Opcodes.PUTSTATIC))
    private static void redirectSetBiome(NetherBiome value) {
        biome_TL.set(value);
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lpaulevs/betternether/world/BNWorldGenerator;biome:Lpaulevs/betternether/biomes/NetherBiome;", opcode = Opcodes.GETSTATIC))
    private static NetherBiome redirectGetBiome() {
        return biome_TL.get();
    }

}
