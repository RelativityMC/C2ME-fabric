package com.ishland.c2me.rewrites.chunksystem.common.async_chunkio;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.LightType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.light.ChunkLightingView;
import net.minecraft.world.chunk.light.LightingProvider;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class AsyncSerializationManager {

    public static final boolean DEBUG = Boolean.getBoolean("c2me.chunkio.debug");

    private static final Logger LOGGER = LoggerFactory.getLogger("C2ME Async Serialization Manager");

    private static final ThreadLocal<ArrayDeque<Scope>> scopeHolder = ThreadLocal.withInitial(ArrayDeque::new);

    public static void push(Scope scope) {
        scopeHolder.get().push(scope);
    }

    public static Scope getScope(ChunkPos pos) {
        final Scope scope = scopeHolder.get().peek();
        if (pos == null) return scope;
        if (scope != null) {
            if (scope.pos.equals(pos))
                return scope;
            LOGGER.error("Scope position mismatch! Expected: {} but got {}.", scope.pos, pos, new Throwable());
        }
        return null;
    }

    public static void pop(Scope scope) {
        if (scope != scopeHolder.get().peek()) throw new IllegalArgumentException("Scope mismatch");
        scopeHolder.get().pop();
    }

    public static class Scope {
        public final ChunkPos pos;
        public final Map<LightType, ChunkLightingView> lighting;
        public final Map<BlockPos, NbtCompound> blockEntities;
        private final AtomicBoolean isOpen = new AtomicBoolean(false);

        @SuppressWarnings("unchecked")
        public Scope(Chunk chunk, ServerWorld world) {
            this.pos = chunk.getPos();
            Map<LightType, ChunkLightingView> lighting = new Object2ObjectOpenHashMap<>();
            for (LightType type : LightType.values()) {
                CachedLightingView cachedLightingView = new CachedLightingView(world.getLightingProvider(), chunk.getPos(), type);
                if (lighting.put(cachedLightingView.getLightType(), cachedLightingView) != null) {
                    throw new IllegalStateException("Duplicate key");
                }
            }
            this.lighting = lighting;
            Map<BlockPos, NbtCompound> blockEntities = new Object2ReferenceLinkedOpenHashMap<>();
            for (BlockPos blockPos : chunk.getBlockEntityPositions()) {
                Pair<BlockPos, NbtCompound> blockPosNbtCompoundPair = Pair.of(blockPos, chunk.getPackedBlockEntityNbt(blockPos, world.getRegistryManager()));
                if (blockEntities.put(blockPosNbtCompoundPair.left(), blockPosNbtCompoundPair.right()) != null) {
                    LOGGER.warn("Duplicate block entity at {} in chunk {}", blockPos, chunk.getPos());
                }
            }
            this.blockEntities = blockEntities;
        }

        public void open() {
            if (!isOpen.compareAndSet(false, true)) throw new IllegalStateException("Cannot use scope twice");
        }

        private static final class CachedLightingView implements ChunkLightingView {

            private static final ChunkNibbleArray EMPTY = new ChunkNibbleArray();

            private final LightType lightType;
            private final Map<ChunkSectionPos, ChunkNibbleArray> cachedData = new Object2ObjectOpenHashMap<>();

            CachedLightingView(LightingProvider provider, ChunkPos pos, LightType type) {
                this.lightType = type;
                for (int i = provider.getBottomY(); i < provider.getTopY(); i++) {
                    final ChunkSectionPos sectionPos = ChunkSectionPos.from(pos, i);
                    ChunkNibbleArray lighting = provider.get(type).getLightSection(sectionPos);
                    cachedData.put(sectionPos, lighting != null ? lighting.copy() : null);
                }
            }

            public LightType getLightType() {
                return this.lightType;
            }

            @Override
            public void checkBlock(BlockPos blockPos) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasUpdates() {
                throw new UnsupportedOperationException();
            }

            @Override
            public int doLightUpdates() {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setSectionStatus(ChunkSectionPos pos, boolean notReady) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setColumnEnabled(ChunkPos chunkPos, boolean bl) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void propagateLight(ChunkPos chunkPos) {
                throw new UnsupportedOperationException();
            }

            @NotNull
            @Override
            public ChunkNibbleArray getLightSection(ChunkSectionPos pos) {
                return cachedData.getOrDefault(pos, EMPTY);
            }

            @Override
            public int getLightLevel(BlockPos pos) {
                throw new UnsupportedOperationException();
            }
        }
    }

}
