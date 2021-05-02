package org.yatopiamc.c2me.common.threading.worldgen;

import com.ibm.asyncutil.locks.AsyncLock;
import com.ibm.asyncutil.locks.AsyncNamedLock;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import org.yatopiamc.c2me.common.config.C2MEConfig;
import org.yatopiamc.c2me.common.threading.GlobalExecutors;
import org.yatopiamc.c2me.common.util.AsyncCombinedLock;
import org.yatopiamc.c2me.common.util.AsyncNamedLockDelegateAsyncLock;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.yatopiamc.c2me.common.threading.worldgen.ChunkStatusThreadingType.AS_IS;
import static org.yatopiamc.c2me.common.threading.worldgen.ChunkStatusThreadingType.PARALLELIZED;
import static org.yatopiamc.c2me.common.threading.worldgen.ChunkStatusThreadingType.SINGLE_THREADED;

public class ChunkStatusUtils {

    public static ChunkStatusThreadingType getThreadingType(final ChunkStatus status) {
        if (status.equals(ChunkStatus.STRUCTURE_STARTS)
                || status.equals(ChunkStatus.STRUCTURE_REFERENCES)
                || status.equals(ChunkStatus.BIOMES)
                || status.equals(ChunkStatus.NOISE)
                || status.equals(ChunkStatus.SURFACE)
                || status.equals(ChunkStatus.CARVERS)
                || status.equals(ChunkStatus.LIQUID_CARVERS)
                || status.equals(ChunkStatus.HEIGHTMAPS)) {
            return PARALLELIZED;
        } else if (status.equals(ChunkStatus.SPAWN)) {
            return SINGLE_THREADED;
        } else if (status.equals(ChunkStatus.FEATURES)) {
            return C2MEConfig.threadedWorldGenConfig.allowThreadedFeatures ? PARALLELIZED : SINGLE_THREADED;
        }
        return AS_IS;
    }

    public static <T> CompletableFuture<T> runChunkGenWithLock(ChunkPos target, int radius, AsyncNamedLock<ChunkPos> chunkLock, Supplier<CompletableFuture<T>> action) {
        return CompletableFuture.supplyAsync(() -> {
            List<ChunkPos> fetchedLocks = new ArrayList<>((2 * radius + 1) * (2 * radius + 1));
            for (int x = target.x - radius; x <= target.x + radius; x++)
                for (int z = target.z - radius; z <= target.z + radius; z++)
                    fetchedLocks.add(new ChunkPos(x, z));

            return new AsyncCombinedLock(chunkLock, new HashSet<>(fetchedLocks)).getFuture().thenComposeAsync(lockToken -> {
                final CompletableFuture<T> future = action.get();
                future.thenRun(lockToken::releaseLock);
                return future;
            }, GlobalExecutors.scheduler);
        }, AsyncCombinedLock.lockWorker).thenCompose(Function.identity());
    }

}
