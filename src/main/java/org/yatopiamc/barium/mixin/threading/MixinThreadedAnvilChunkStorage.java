package org.yatopiamc.barium.mixin.threading;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.yatopiamc.barium.common.threading.ThreadingExecutorUtils;

@Mixin(ThreadedAnvilChunkStorage.class)
public class MixinThreadedAnvilChunkStorage {

    /**
     * @author ishland
     * @reason move to scheduler
     */
    @SuppressWarnings("OverwriteTarget")
    @Dynamic
    @Overwrite
    private void method_17259(ChunkHolder chunkHolder, Runnable runnable) { // synthetic method for worldGenExecutor scheduling
        ThreadingExecutorUtils.schedule(runnable);
    }

}
