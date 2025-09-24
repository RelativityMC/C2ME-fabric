package com.ishland.c2me.rewrites.chunksystem.common.statuses;

import com.ishland.c2me.rewrites.chunksystem.common.ChunkLoadingContext;
import com.ishland.c2me.rewrites.chunksystem.common.NewChunkStatus;
import com.ishland.flowsched.scheduler.Cancellable;
import io.reactivex.rxjava3.core.Completable;
import net.minecraft.world.chunk.ChunkStatus;

public class Deferred extends NewChunkStatus {

    public Deferred(int ordinal) {
        super(ordinal, ChunkStatus.EMPTY);
    }

    @Override
    public Completable upgradeToThis(ChunkLoadingContext context, Cancellable cancellable) {
        return Completable.complete();
    }

    @Override
    public Completable postUpgradeToThis(ChunkLoadingContext context) {
        return Completable.complete();
    }

    @Override
    public Completable preDowngradeFromThis(ChunkLoadingContext context, Cancellable cancellable) {
        return Completable.complete();
    }

    @Override
    public Completable downgradeFromThis(ChunkLoadingContext context, Cancellable cancellable) {
        return Completable.complete();
    }

    @Override
    public String toString() {
        return "deferred";
    }
}
