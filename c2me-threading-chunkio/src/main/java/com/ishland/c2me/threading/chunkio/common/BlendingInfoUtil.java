package com.ishland.c2me.threading.chunkio.common;

import com.ibm.asyncutil.util.Combinators;
import com.ishland.c2me.base.mixin.access.IBlender;
import com.ishland.c2me.base.mixin.access.IStorageIoWorker;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.StorageIoWorker;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class BlendingInfoUtil {

    public static CompletionStage<List<BitSet>> getBlendingInfos(StorageIoWorker worker, ChunkPos pos) {
        final int radius = IBlender.getBLENDING_CHUNK_DISTANCE_THRESHOLD();
        List<CompletableFuture<BitSet>> futures = new ArrayList<>((radius * 2 + 1) * (radius * 2 + 1));
        ChunkPos chunkPos2 = new ChunkPos(pos.x - radius, pos.z - radius);
        ChunkPos chunkPos3 = new ChunkPos(pos.x + radius, pos.z + radius);
        for(int i = chunkPos2.getRegionX(); i <= chunkPos3.getRegionX(); ++i) {
            for(int j = chunkPos2.getRegionZ(); j <= chunkPos3.getRegionZ(); ++j) {
                final CompletableFuture<BitSet> future = ((IStorageIoWorker) worker).invokeGetOrComputeBlendingStatus(i, j);
                futures.add(future);
            }
        }
        return Combinators.collect(futures, Collectors.toList());
    }

}
