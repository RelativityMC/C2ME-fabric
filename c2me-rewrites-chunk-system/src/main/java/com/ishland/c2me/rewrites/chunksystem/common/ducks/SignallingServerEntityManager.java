package com.ishland.c2me.rewrites.chunksystem.common.ducks;

import java.util.concurrent.CompletableFuture;

public interface SignallingServerEntityManager {

    CompletableFuture<Void> c2me$getReadFuture(long pos);

    CompletableFuture<Void> c2me$getUnloadFuture(long pos);

}
