package com.ishland.c2me.mixin.access;

import net.minecraft.world.gen.chunk.ChunkGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ChunkGenerator.class)
public interface IChunkGenerator {

    @Invoker
    void invokeGenerateStrongholdPositions();

}
