package com.ishland.c2me.threading.chunkio.mixin;

import com.ishland.c2me.base.mixin.access.IBlender;
import com.ishland.c2me.threading.chunkio.common.ProtoChunkExtension;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ProtoChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.BitSet;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Mixin(ProtoChunk.class)
public class MixinProtoChunk implements ProtoChunkExtension {

    @Unique
    private CompletableFuture<Void> initialMainThreadComputeFuture = CompletableFuture.completedFuture(null);

    @Unique
    private boolean needBlending = false;

    @Override
    public void setBlendingInfo(ChunkPos pos, List<BitSet> bitSets) {
        final int radius = IBlender.getBLENDING_CHUNK_DISTANCE_THRESHOLD();
        final int width = (radius * 2 + 1);
        ChunkPos chunkPos2 = new ChunkPos(pos.x - radius, pos.z - radius);
        ChunkPos chunkPos3 = new ChunkPos(pos.x + radius, pos.z + radius);

        int index = 0;
        for(int i = chunkPos2.getRegionX(); i <= chunkPos3.getRegionX(); ++i) {
            for(int j = chunkPos2.getRegionZ(); j <= chunkPos3.getRegionZ(); ++j) {
                BitSet bitSet = bitSets.get(index ++);
                if (!bitSet.isEmpty()) {
                    ChunkPos chunkPos4 = ChunkPos.fromRegion(i, j);
                    int k = Math.max(chunkPos2.x - chunkPos4.x, 0);
                    int l = Math.max(chunkPos2.z - chunkPos4.z, 0);
                    int m = Math.min(chunkPos3.x - chunkPos4.x, 31);
                    int n = Math.min(chunkPos3.z - chunkPos4.z, 31);

                    for(int o = k; o <= m; ++o) {
                        for(int p = l; p <= n; ++p) {
                            int q = p * 32 + o;
                            if (bitSet.get(q)) {
                                this.needBlending = true;
                                return;
                            }
                        }
                    }
                }
            }
        }

        this.needBlending = false;
    }

    @Override
    public boolean getNeedBlending() {
        return needBlending; // blending determined early
    }

    @Override
    public void setInitialMainThreadComputeFuture(CompletableFuture<Void> future) {
        this.initialMainThreadComputeFuture = future;
    }

    @Override
    public CompletableFuture<Void> getInitialMainThreadComputeFuture() {
        return this.initialMainThreadComputeFuture;
    }

}
