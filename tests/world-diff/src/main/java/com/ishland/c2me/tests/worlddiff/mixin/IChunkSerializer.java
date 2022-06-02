package com.ishland.c2me.tests.worlddiff.mixin;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.structure.StructureContext;
import net.minecraft.structure.StructureStart;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.gen.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(ChunkSerializer.class)
public interface IChunkSerializer {

    @Invoker
    static Map<Structure, StructureStart> invokeReadStructureStarts(StructureContext context, NbtCompound nbt, long worldSeed) {
        throw new AbstractMethodError();
    }

}
