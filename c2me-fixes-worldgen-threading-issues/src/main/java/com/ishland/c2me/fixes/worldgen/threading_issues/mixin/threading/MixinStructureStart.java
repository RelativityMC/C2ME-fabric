package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import net.minecraft.structure.StructureStart;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.atomic.AtomicInteger;

@Mixin(StructureStart.class)
public class MixinStructureStart {

    private final AtomicInteger referencesAtomic = new AtomicInteger();

    @Dynamic
    @Redirect(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/StructureStart;references:I", opcode = Opcodes.GETFIELD))
    private int redirectGetReferences(StructureStart structureStart) {
        return referencesAtomic.get();
    }

    /**
     * @author ishland
     * @reason atomic operation
     */
    @Overwrite
    public void incrementReferences() {
        this.referencesAtomic.incrementAndGet();
    }

}
