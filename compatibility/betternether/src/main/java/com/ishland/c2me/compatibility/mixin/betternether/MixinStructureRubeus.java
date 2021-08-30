package com.ishland.c2me.compatibility.mixin.betternether;

import net.minecraft.util.math.BlockPos;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import paulevs.betternether.structures.plants.StructureRubeus;

import java.util.HashSet;
import java.util.Set;

@Mixin(StructureRubeus.class)
public class MixinStructureRubeus {

    private static final ThreadLocal<Set<BlockPos>> POINTSThreadLocal = ThreadLocal.withInitial(HashSet::new);
    private static final ThreadLocal<Set<BlockPos>> MIDDLEThreadLocal = ThreadLocal.withInitial(HashSet::new);
    private static final ThreadLocal<Set<BlockPos>> TOPThreadLocal = ThreadLocal.withInitial(HashSet::new);

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lpaulevs/betternether/structures/plants/StructureRubeus;POINTS:Ljava/util/Set;", opcode = Opcodes.GETSTATIC), remap = false)
    private Set<BlockPos> redirectGetPoints() {
        return POINTSThreadLocal.get();
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lpaulevs/betternether/structures/plants/StructureRubeus;MIDDLE:Ljava/util/Set;", opcode = Opcodes.GETSTATIC), remap = false)
    private Set<BlockPos> redirectGetMiddle() {
        return MIDDLEThreadLocal.get();
    }

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lpaulevs/betternether/structures/plants/StructureRubeus;TOP:Ljava/util/Set;", opcode = Opcodes.GETSTATIC), remap = false)
    private Set<BlockPos> redirectGetTop() {
        return TOPThreadLocal.get();
    }

}
