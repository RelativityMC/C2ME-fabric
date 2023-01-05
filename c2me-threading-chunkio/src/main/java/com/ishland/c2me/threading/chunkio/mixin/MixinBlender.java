package com.ishland.c2me.threading.chunkio.mixin;

import com.ishland.c2me.threading.chunkio.common.ProtoChunkExtension;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.gen.chunk.Blender;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Blender.class)
public class MixinBlender {

    @Redirect(method = "getBlender", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/ChunkRegion;needsBlending(Lnet/minecraft/util/math/ChunkPos;I)Z"))
    private static boolean redirectNeedsBlending(ChunkRegion instance, ChunkPos chunkPos, int checkRadius) {
        return ((ProtoChunkExtension) instance.getChunk(chunkPos.x, chunkPos.z)).getNeedBlending();
    }

}
