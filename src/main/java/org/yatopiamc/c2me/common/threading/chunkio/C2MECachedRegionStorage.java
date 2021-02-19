package org.yatopiamc.c2me.common.threading.chunkio;

import com.google.common.base.Preconditions;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalNotification;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.ibm.asyncutil.locks.AsyncLock;
import com.ibm.asyncutil.locks.AsyncNamedLock;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.RegionBasedStorage;
import net.minecraft.world.storage.RegionFile;
import net.minecraft.world.storage.StorageIoWorker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.yatopiamc.c2me.common.threading.GlobalExecutors;
import org.yatopiamc.c2me.common.util.C2MEForkJoinWorkerThreadFactory;
import org.yatopiamc.c2me.common.util.SneakyThrow;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class C2MECachedRegionStorage extends StorageIoWorker {

    private static final CompoundTag EMPTY_VALUE = new CompoundTag();
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ForkJoinPool IOExecutor = new ForkJoinPool(
            Math.min(6, Runtime.getRuntime().availableProcessors()),
            new C2MEForkJoinWorkerThreadFactory("C2ME chunkio io worker #%d", Thread.NORM_PRIORITY - 3),
            null, true
    );

    private final RegionBasedStorage storage;
    private final Cache<ChunkPos, CompoundTag> chunkCache;
    private final ConcurrentHashMap<ChunkPos, CompletableFuture<Void>> writeFutures = new ConcurrentHashMap<>();
    private final AsyncNamedLock<ChunkPos> chunkLocks = AsyncNamedLock.createFair();
    private final AsyncNamedLock<RegionPos> regionLocks = AsyncNamedLock.createFair();
    private final AsyncLock storageLock = AsyncLock.createFair();

    private final AtomicBoolean isClosed = new AtomicBoolean(false);

    public C2MECachedRegionStorage(File file, boolean bl, String string) {
        super(file, bl, string);
        this.storage = new RegionBasedStorage(file, bl);
        this.chunkCache = CacheBuilder.newBuilder()
                .concurrencyLevel(Runtime.getRuntime().availableProcessors() * 2)
                .expireAfterAccess(3, TimeUnit.SECONDS)
                .maximumSize(8192)
                .removalListener((RemovalNotification<ChunkPos, CompoundTag> notification) -> {
                    if (notification.getValue() != EMPTY_VALUE)
                        scheduleWrite(notification.getKey(), notification.getValue());
                })
                .build();
        this.tick();
    }

    private void tick() {
        long startTime = System.currentTimeMillis();
        chunkCache.cleanUp();
        if (!isClosed.get())
            GlobalExecutors.scheduler.schedule(this::tick, 1000 - (System.currentTimeMillis() - startTime), TimeUnit.MILLISECONDS);
    }

    private CompletableFuture<RegionFile> getRegionFile(ChunkPos pos) {
        return storageLock.acquireLock().toCompletableFuture().thenApplyAsync(lockToken -> {
            try {
                return storage.getRegionFile(pos);
            } catch (IOException e) {
                SneakyThrow.sneaky(e);
                throw new RuntimeException(e);
            } finally {
                lockToken.releaseLock();
            }
        }, GlobalExecutors.scheduler);
    }

    private void scheduleWrite(ChunkPos pos, CompoundTag chunkData) {
        writeFutures.put(pos, regionLocks.acquireLock(new RegionPos(pos)).toCompletableFuture().thenCombineAsync(getRegionFile(pos), (lockToken, regionFile) -> {
            try (final DataOutputStream dataOutputStream = regionFile.getChunkOutputStream(pos)) {
                NbtIo.write(chunkData, dataOutputStream);
            } catch (Throwable t) {
                LOGGER.error("Failed to store chunk {}", pos, t);
            } finally {
                lockToken.releaseLock();
            }
            return null;
        }, IOExecutor).exceptionally(t -> null).thenAccept(unused -> {
        }));
    }

    @Override
    public CompletableFuture<Void> setResult(ChunkPos pos, CompoundTag nbt) {
        ensureOpen();
        Preconditions.checkNotNull(pos);
        Preconditions.checkNotNull(nbt);
        this.chunkCache.put(pos, nbt);
        return CompletableFuture.completedFuture(null);
    }

    public CompletableFuture<CompoundTag> getNbtAtAsync(ChunkPos pos) {
        ensureOpen();
        // Check cache
        {
            final CompoundTag cachedValue = this.chunkCache.getIfPresent(pos);
            if (cachedValue != null) {
                if (cachedValue == EMPTY_VALUE)
                    return CompletableFuture.completedFuture(null);
                else
                    return CompletableFuture.completedFuture(cachedValue);
            }
        }
        return chunkLocks.acquireLock(pos).toCompletableFuture().thenComposeAsync(lockToken -> {
            try {
                // Check again in single-threaded environment
                final CompoundTag cachedValue = this.chunkCache.getIfPresent(pos);
                if (cachedValue != null) {
                    if (cachedValue == EMPTY_VALUE)
                        return CompletableFuture.completedFuture(null);
                    else
                        return CompletableFuture.completedFuture(cachedValue);
                }
                return regionLocks.acquireLock(new RegionPos(pos)).thenCombineAsync(getRegionFile(pos), (lockToken1, regionFile) -> {
                    try {
                        final CompoundTag queriedTag;
                        try (final DataInputStream dataInputStream = regionFile.getChunkInputStream(pos)) {
                            if (dataInputStream != null)
                                queriedTag = NbtIo.read(dataInputStream);
                            else
                                queriedTag = null;
                        }
                        chunkLocks.acquireLock(pos).thenAccept(lockToken2 -> {
                            try {
                                chunkCache.put(pos, queriedTag != null ? queriedTag : EMPTY_VALUE);
                            } finally {
                                lockToken2.releaseLock();
                            }
                        });
                        return queriedTag;
                    } catch (Throwable t) {
                        LOGGER.warn("Failed to read chunk {}", pos, t);
                        return null;
                    } finally {
                        lockToken1.releaseLock();
                    }
                }, IOExecutor);
            } catch (Throwable t) {
                LOGGER.warn("Failed to read chunk {}", pos, t);
                return CompletableFuture.completedFuture(null);
            } finally {
                lockToken.releaseLock();
            }
        }, GlobalExecutors.scheduler);
    }

    @Nullable
    @Override
    public CompoundTag getNbt(ChunkPos pos) throws IOException {
        return getNbtAtAsync(pos).join();
    }

    @Override
    public CompletableFuture<Void> completeAll() {
        chunkCache.invalidateAll();
        try {
            storage.method_26982();
        } catch (Throwable t) {
            LOGGER.warn("Failed to synchronized chunks", t);
        }
        return CompletableFuture.allOf(writeFutures.values().toArray(CompletableFuture[]::new));
    }

    @Override
    public void close() throws IOException {
        if (this.isClosed.compareAndSet(false, true)) {
            completeAll().join();
            this.storage.close();
        }
    }

    private void ensureOpen() {
        Preconditions.checkState(!isClosed.get(), "Tried to modify a closed instance");
    }

    private static class RegionPos {
        private final int x;
        private final int z;

        private RegionPos(int x, int z) {
            this.x = x;
            this.z = z;
        }

        private RegionPos(ChunkPos chunkPos) {
            this.x = chunkPos.getRegionX();
            this.z = chunkPos.getRegionZ();
        }

        public int getX() {
            return x;
        }

        public int getZ() {
            return z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RegionPos regionPos = (RegionPos) o;
            return x == regionPos.x && z == regionPos.z;
        }

        @Override
        public int hashCode() {
            return Objects.hash(x, z);
        }

        @Override
        public String toString() {
            return "RegionPos{" +
                    "x=" + x +
                    ", z=" + z +
                    '}';
        }
    }
}
