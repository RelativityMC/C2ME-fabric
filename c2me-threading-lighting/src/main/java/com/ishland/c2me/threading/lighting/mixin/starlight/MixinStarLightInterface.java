package com.ishland.c2me.threading.lighting.mixin.starlight;

import ca.spottedleaf.starlight.common.light.StarLightInterface;
import ca.spottedleaf.starlight.common.util.WorldUtil;
import com.ishland.c2me.threading.lighting.common.starlight.StarLightQueue;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.concurrent.CompletableFuture;

@Mixin(StarLightInterface.class)
public class MixinStarLightInterface {

    @Shadow @Final protected World world;
    @Shadow @Final protected ChunkProvider lightAccess;
    @Unique
    protected final StarLightQueue c2me$starlightQueue = new StarLightQueue((StarLightInterface) (Object) this);

    /**
     * @author ishland
     * @reason threaded starlight
     */
    @Overwrite(remap = false)
    public boolean hasUpdates() {
        return !this.c2me$starlightQueue.isEmpty();
    }

    /**
     * @author ishland
     * @reason threaded starlight
     */
    @Overwrite(remap = false)
    public CompletableFuture<Void> blockChange(BlockPos pos) {
        if (this.world != null && pos.getY() >= WorldUtil.getMinBlockY(this.world) && pos.getY() <= WorldUtil.getMaxBlockY(this.world)) {
            return this.c2me$starlightQueue.queueBlockChange(pos);
        } else {
            return null;
        }
    }

    /**
     * @author ishland
     * @reason threaded starlight
     */
    @Overwrite(remap = false)
    public CompletableFuture<Void> sectionChange(ChunkSectionPos pos, boolean newEmptyValue) {
        if (this.world != null) {
            return this.c2me$starlightQueue.queueSectionChange(pos, newEmptyValue);
        } else {
            return null;
        }
    }

    /**
     * @author ishland
     * @reason threaded starlight
     */
    @Overwrite(remap = false)
    public void scheduleChunkLight(final ChunkPos pos, final Runnable run) {
        this.c2me$starlightQueue.queueChunkLighting(pos, run);
    }

    /**
     * @author ishland
     * @reason threaded starlight
     */
    @Overwrite(remap = false)
    public void removeChunkTasks(ChunkPos pos) {
        this.c2me$starlightQueue.removeChunk(pos);
    }

    /**
     * @author ishland
     * @reason threaded starlight
     */
    @Overwrite(remap = false)
    public void propagateChanges() {
        this.c2me$starlightQueue.scheduleAll();
    }

}
