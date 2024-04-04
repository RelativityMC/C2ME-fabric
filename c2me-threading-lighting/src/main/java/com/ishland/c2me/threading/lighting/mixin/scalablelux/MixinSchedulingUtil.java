package com.ishland.c2me.threading.lighting.mixin.scalablelux;

import com.ishland.c2me.base.common.GlobalExecutors;
import com.ishland.c2me.base.common.scheduler.LockTokenImpl;
import com.ishland.c2me.base.common.scheduler.SimplePrioritizedTask;
import com.ishland.flowsched.executor.LockToken;
import net.minecraft.util.math.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Pseudo;

import java.util.ArrayList;

@Pseudo
@Mixin(targets = "ca.spottedleaf.starlight.common.thread.SchedulingUtil")
public class MixinSchedulingUtil {

    /**
     * @author ishland
     * @reason merge thread pool
     */
    @Overwrite(remap = false)
    public static void scheduleTask(int ownerTag, Runnable task, int x, int z, int radius) {
        final ArrayList<LockToken> lockTokens = new ArrayList<>((radius * 2 + 1) * (radius * 2 + 1));
        for (int i = -radius; i <= radius; i++) {
            for (int j = -radius; j <= radius; j++) {
                lockTokens.add(new LockTokenImpl(ownerTag, ChunkPos.toLong(x + i, z + j), LockTokenImpl.Usage.LIGHTING));
            }
        }
        final SimplePrioritizedTask simpleTask = new SimplePrioritizedTask(task, lockTokens.toArray(LockToken[]::new), 17);
        GlobalExecutors.prioritizedScheduler.schedule(simpleTask);
    }

    /**
     * @author ishlabd
     * @reason merge thread pool
     */
    @Overwrite(remap = false)
    public static boolean isExternallyManaged() {
        return true;
    }

}
