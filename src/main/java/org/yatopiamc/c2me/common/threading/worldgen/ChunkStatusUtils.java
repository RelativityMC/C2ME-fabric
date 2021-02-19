package org.yatopiamc.c2me.common.threading.worldgen;

import com.ibm.asyncutil.locks.AsyncLock;
import com.ibm.asyncutil.locks.AsyncNamedLock;
import com.ibm.asyncutil.util.Combinators;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.NotNull;
import org.yatopiamc.c2me.common.threading.GlobalExecutors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Supplier;

public class ChunkStatusUtils {

    public static ChunkStatusThreadingType getThreadingType(final ChunkStatus status) {
        switch (status.getId()) {
            case "structure_starts":
            case "structure_references":
            case "biomes":
            case "noise":
            case "surface":
            case "carvers":
            case "liquid_carvers":
            case "spawn":
            case "heightmaps":
                return ChunkStatusThreadingType.PARALLELIZED;
            case "features":
                return ChunkStatusThreadingType.SINGLE_THREADED;
            default:
                return ChunkStatusThreadingType.AS_IS;
        }
    }

    public static <T> CompletableFuture<T> runChunkGenWithLock(ChunkPos target, int radius, AsyncNamedLock<ChunkPos> chunkLock, Supplier<CompletableFuture<T>> action) {
        List<CompletionStage<AsyncLock.LockToken>> acquiredLocks = new ArrayList<>((radius + 1) * (radius + 1));
        for (int x = target.x - radius; x <= target.x + radius; x++)
            for (int z = target.z - radius; z <= target.z + radius; z++)
                acquiredLocks.add(chunkLock.acquireLock(new ChunkPos(x, z)));

        return Combinators.collect(acquiredLocks).toCompletableFuture().thenComposeAsync(lockTokens -> {
            final CompletableFuture<T> future = action.get();
            future.thenRun(() -> lockTokens.forEach(AsyncLock.LockToken::releaseLock));
            return future;
        }, GlobalExecutors.scheduler);
    }

}
