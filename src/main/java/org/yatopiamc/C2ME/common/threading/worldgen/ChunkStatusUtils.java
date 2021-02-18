package org.yatopiamc.C2ME.common.threading.worldgen;

import com.ibm.asyncutil.locks.AsyncLock;
import com.ibm.asyncutil.locks.AsyncNamedLock;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ChunkStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
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

    private static <T> CompletableFuture<T> buildChain0(@NotNull List<CompletableFuture<AsyncLock.LockToken>> list, int index, Supplier<CompletableFuture<T>> code) {
        if (index < list.size()) {
            return list.get(index).thenCompose(lockToken -> {
                final CompletableFuture<T> future = buildChain0(list, index + 1, code);
                future.thenRun(lockToken::releaseLock);
                return future;
            });
        } else {
            return code.get();
        }
    }

    public static <T> CompletableFuture<T> runChunkGenWithLock(ChunkPos target, int radius, AsyncNamedLock<ChunkPos> chunkLock, Supplier<CompletableFuture<T>> action) {
        List<CompletableFuture<AsyncLock.LockToken>> acquiredLocks = new ArrayList<>((radius + 1) * (radius + 1));
        for (int x = target.x - radius; x <= target.x + radius; x++)
            for (int z = target.z - radius; z <= target.z + radius; z++)
                acquiredLocks.add(chunkLock.acquireLock(new ChunkPos(x, z)).toCompletableFuture());

        return buildChain0(acquiredLocks, 0, action);
    }

}
