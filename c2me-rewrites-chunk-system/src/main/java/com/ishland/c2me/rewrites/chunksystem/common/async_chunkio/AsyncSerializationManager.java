package com.ishland.c2me.rewrites.chunksystem.common.async_chunkio;

import it.unimi.dsi.fastutil.objects.Object2ReferenceLinkedOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
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
        public final Map<BlockPos, NbtCompound> blockEntities;
        private final AtomicBoolean isOpen = new AtomicBoolean(false);

        @SuppressWarnings("unchecked")
        public Scope(Chunk chunk, ServerWorld world) {
            this.pos = chunk.getPos();
            Map<BlockPos, NbtCompound> blockEntities = new Object2ReferenceLinkedOpenHashMap<>();
            for (BlockPos blockPos : chunk.getBlockEntityPositions()) {
                final NbtCompound nbt = chunk.getPackedBlockEntityNbt(blockPos, world.getRegistryManager());
                if (nbt == null) {
                    LOGGER.warn("Block entity at {} for block {} in chunk {} is missing", blockPos, chunk.getBlockState(blockPos), chunk.getPos());
                }
                if (blockEntities.containsKey(blockPos)) {
                    LOGGER.warn("Duplicate block entity at {} in chunk {}", blockPos, chunk.getPos());
                } else {
                    blockEntities.put(blockPos, nbt);
                }
            }
            this.blockEntities = blockEntities;
        }

        public void open() {
            if (!isOpen.compareAndSet(false, true)) throw new IllegalStateException("Cannot use scope twice");
        }

    }

}
