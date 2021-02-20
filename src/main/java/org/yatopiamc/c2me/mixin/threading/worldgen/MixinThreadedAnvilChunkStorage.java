package org.yatopiamc.c2me.mixin.threading.worldgen;

import net.minecraft.server.world.ChunkHolder;
import net.minecraft.server.world.ThreadedAnvilChunkStorage;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.yatopiamc.c2me.common.threading.GlobalExecutors;
import org.yatopiamc.c2me.common.threading.worldgen.WorldGenThreadingExecutorUtils;

@Mixin(ThreadedAnvilChunkStorage.class)
public class MixinThreadedAnvilChunkStorage {

    /**
     * @author ishland
     * @reason move to scheduler
     */
    @Dynamic
    @Overwrite
    private void method_17259(ChunkHolder chunkHolder, Runnable runnable) { // synthetic method for worldGenExecutor scheduling
        GlobalExecutors.scheduler.execute(runnable);
    }

}
