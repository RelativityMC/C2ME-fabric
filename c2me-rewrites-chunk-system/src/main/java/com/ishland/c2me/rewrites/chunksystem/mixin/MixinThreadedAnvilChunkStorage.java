package com.ishland.c2me.rewrites.chunksystem.mixin;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ChunkLevels;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import net.minecraft.util.math.ChunkPos;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ThreadedAnvilChunkStorage.class)
public class MixinThreadedAnvilChunkStorage {

    /**
     * @author ishland
     * @reason replace chunk system
     */
    @Overwrite
    @Nullable
    ChunkHolder setLevel(long pos, int level, @Nullable ChunkHolder holder, int i) {

    }

}
