package com.ishland.c2me.rewrites.chunksystem.mixin;

import com.ishland.c2me.rewrites.chunksystem.common.ducks.IPartialPostProcessing;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.chunk.UpgradeData;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.chunk.BlendingData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(WorldChunk.class)
public abstract class MixinWorldChunk extends Chunk implements IPartialPostProcessing {

    @Shadow @Final private World world;

    public MixinWorldChunk(ChunkPos pos, UpgradeData upgradeData, HeightLimitView heightLimitView, Registry<Biome> biomeRegistry, long inhabitedTime, @Nullable ChunkSection[] sectionArray, @Nullable BlendingData blendingData) {
        super(pos, upgradeData, heightLimitView, biomeRegistry, inhabitedTime, sectionArray, blendingData);
    }

    @Override
    public void c2me$runBorderPostProcessing() {
        ChunkPos chunkPos = this.getPos();

        for (int i = 0; i < this.postProcessingLists.length; i++) {
            if (this.postProcessingLists[i] != null) {
                for (ShortListIterator iterator = this.postProcessingLists[i].iterator(); iterator.hasNext(); ) {
                    short short_ = iterator.nextShort();
                    BlockPos blockPos = ProtoChunk.joinBlockPos(short_, this.sectionIndexToCoord(i), chunkPos);
                    BlockState blockState = this.getBlockState(blockPos);

                    if (blockState.getBlock() == Blocks.BROWN_MUSHROOM || blockState.getBlock() == Blocks.RED_MUSHROOM) {
                        if (!blockState.canPlaceAt(world, blockPos)) {
                            this.world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.NO_REDRAW | Block.FORCE_STATE);
                        }
                    }
                }
            }
        }
    }

}
