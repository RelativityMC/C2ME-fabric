package com.ishland.c2me.natives.mixin.density_functions;

import com.ishland.c2me.natives.common.NativeStruct;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/world/gen/chunk/ChunkNoiseSampler$1")
public class MixinChunkNoiseSampler1 implements NativeStruct {

    @Shadow @Final private ChunkNoiseSampler field_36595;

    @Override
    public long getNativePointer() {
        return ((NativeStruct) this.field_36595).getNativePointer();
    }
}
