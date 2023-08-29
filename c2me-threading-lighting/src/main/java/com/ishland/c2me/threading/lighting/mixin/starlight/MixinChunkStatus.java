package com.ishland.c2me.threading.lighting.mixin.starlight;

import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import java.util.EnumSet;

@Mixin(ChunkStatus.class)
public class MixinChunkStatus {

    @ModifyArgs(method = "register(Ljava/lang/String;Lnet/minecraft/world/chunk/ChunkStatus;IZLjava/util/EnumSet;Lnet/minecraft/world/chunk/ChunkStatus$ChunkType;Lnet/minecraft/world/chunk/ChunkStatus$GenerationTask;Lnet/minecraft/world/chunk/ChunkStatus$LoadTask;)Lnet/minecraft/world/chunk/ChunkStatus;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/ChunkStatus;<init>(Lnet/minecraft/world/chunk/ChunkStatus;IZLjava/util/EnumSet;Lnet/minecraft/world/chunk/ChunkStatus$ChunkType;Lnet/minecraft/world/chunk/ChunkStatus$GenerationTask;Lnet/minecraft/world/chunk/ChunkStatus$LoadTask;)V"))
    private static void modifyLightRadius(Args args,
                                          String id,
                                          ChunkStatus previous,
                                          int taskMargin,
                                          boolean shouldAlwaysUpgrade,
                                          EnumSet<Heightmap.Type> heightMapTypes,
                                          ChunkStatus.ChunkType chunkType,
                                          ChunkStatus.GenerationTask generationTask,
                                          ChunkStatus.LoadTask loadTask) {
        if (id.equals("light")) {
            args.set(1, 2);
        }
    }

    @Redirect(method = "getInitializeLightingFuture", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/Chunk;refreshSurfaceY()V"))
    private static void removeUnnecessaryInit(Chunk instance) {
        // no-op
    }

}
