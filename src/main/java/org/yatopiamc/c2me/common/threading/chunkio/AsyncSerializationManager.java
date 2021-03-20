package org.yatopiamc.c2me.common.threading.chunkio;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.server.world.ServerTickScheduler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.server.world.SimpleTickScheduler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.LightType;
import net.minecraft.world.TickScheduler;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkNibbleArray;
import net.minecraft.world.chunk.light.ChunkLightingView;
import net.minecraft.world.chunk.light.LightingProvider;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.yatopiamc.c2me.common.util.DeepCloneable;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AsyncSerializationManager {

    private static final Logger LOGGER = LogManager.getLogger("C2ME Async Serialization Manager");

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
            LOGGER.error("Scope position mismatch! Expected: {} but got {}. This will impact stability. Incompatible mods?", scope.pos, pos, new Throwable());
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
        public final TickScheduler<Block> blockTickScheduler;
        public final TickScheduler<Fluid> fluidTickScheduler;
        public final Map<BlockPos, BlockEntity> blockEntities;
        private final AtomicBoolean isOpen = new AtomicBoolean(false);

        public Scope(Chunk chunk, ServerWorld world) {
            this.pos = chunk.getPos();
            this.lighting = Arrays.stream(LightType.values()).map(type -> new CachedLightingView(world.getLightingProvider(), chunk.getPos(), type)).collect(Collectors.toMap(CachedLightingView::getLightType, Function.identity()));
            final TickScheduler<Block> blockTickScheduler = chunk.getBlockTickScheduler();
            if (blockTickScheduler instanceof DeepCloneable) {
                this.blockTickScheduler = (TickScheduler<Block>) ((DeepCloneable) blockTickScheduler).deepClone();
            } else {
                final ServerTickScheduler<Block> worldBlockTickScheduler = world.getBlockTickScheduler();
                this.blockTickScheduler = new SimpleTickScheduler<>(Registry.BLOCK::getId, worldBlockTickScheduler.getScheduledTicksInChunk(chunk.getPos(), false, true), world.getTime());
            }
            final TickScheduler<Fluid> fluidTickScheduler = chunk.getFluidTickScheduler();
            if (fluidTickScheduler instanceof DeepCloneable) {
                this.fluidTickScheduler = (TickScheduler<Fluid>) ((DeepCloneable) fluidTickScheduler).deepClone();
            } else {
                final ServerTickScheduler<Fluid> worldFluidTickScheduler = world.getFluidTickScheduler();
                this.fluidTickScheduler = new SimpleTickScheduler<>(Registry.FLUID::getId, worldFluidTickScheduler.getScheduledTicksInChunk(chunk.getPos(), false, true), world.getTime());
            }
            this.blockEntities = chunk.getBlockEntityPositions().stream().map(chunk::getBlockEntity).filter(Objects::nonNull).filter(blockEntity -> !blockEntity.isRemoved()).collect(Collectors.toMap(BlockEntity::getPos, Function.identity()));
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
                for (int i = provider.method_31929(); i < provider.method_31930(); i++) {
                    final ChunkSectionPos sectionPos = ChunkSectionPos.from(pos, i);
                    ChunkNibbleArray lighting = provider.get(type).getLightSection(sectionPos);
                    cachedData.put(sectionPos, lighting != null ? lighting.copy() : null);
                }
            }

            public LightType getLightType() {
                return this.lightType;
            }

            @Override
            public void setSectionStatus(ChunkSectionPos pos, boolean notReady) {
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
