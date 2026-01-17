package com.ishland.c2me.rewrites.chunksystem.common.statuses;

import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.flowsched.scheduler.Cancellable;
import io.reactivex.rxjava3.core.Completable;
import net.minecraft.world.chunk.ChunkStatus;

public class LazyWorldGenerationDelegate extends VanillaWorldGenerationDelegate {

    public LazyWorldGenerationDelegate(int ordinal, ChunkStatus status) {
        super(ordinal, status);
    }

    @Override
    public Completable upgradeToThis(ChunkLoadingContext context, Cancellable cancellable) {
        // Logic to check distance to nearest player can be added here
        // If too far, we could potentially delay or use a lower priority
        // For now, let's just use the super implementation but we have the hook
        return super.upgradeToThis(context, cancellable);
    }
}
