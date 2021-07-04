package com.ishland.c2me.tests.worlddiff;

import com.google.common.collect.ImmutableSet;
import com.ibm.asyncutil.locks.AsyncSemaphore;
import com.ibm.asyncutil.locks.FairAsyncSemaphore;
import com.ishland.c2me.tests.worlddiff.mixin.IStorageIoWorker;
import com.ishland.c2me.tests.worlddiff.mixin.IWorldUpdater;
import net.minecraft.block.BlockState;
import net.minecraft.datafixer.Schemas;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.FileResourcePackProvider;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ResourcePackSource;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.storage.StorageIoWorker;
import net.minecraft.world.updater.WorldUpdater;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class ComparisonSession implements Closeable {

    private final WorldHandle baseWorld;
    private final WorldHandle targetWorld;

    public ComparisonSession(File from, File to) {
        try {
            baseWorld = getWorldHandle(from, "Base world");
            targetWorld = getWorldHandle(to, "Target world");
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public void compareChunks() {
        System.out.println("Starting comparison of chunks");
        AsyncSemaphore working = new FairAsyncSemaphore(Runtime.getRuntime().availableProcessors() * 4);
        final HashSet<RegistryKey<World>> worlds = new HashSet<>(baseWorld.chunkPosesMap.keySet());
        worlds.retainAll(targetWorld.chunkPosesMap.keySet());
        for (RegistryKey<World> world : worlds) {
            System.out.printf("Filtering chunks in world %s\n", world);
            final HashSet<ChunkPos> chunks = new HashSet<>(baseWorld.chunkPosesMap.get(world));
            chunks.retainAll(targetWorld.chunkPosesMap.get(world));
            final int totalChunks = chunks.size();
            final StorageIoWorker regionBaseIo = baseWorld.regionIoWorkers.get(world);
            final StorageIoWorker regionTargetIo = targetWorld.regionIoWorkers.get(world);
            AtomicLong completedChunks = new AtomicLong();
            AtomicLong completedBlocks = new AtomicLong();
            AtomicLong differenceBlocks = new AtomicLong();
            ConcurrentHashMap<Identifier, AtomicLong> blockDifference = new ConcurrentHashMap<>();
            final CompletableFuture<Void> future = CompletableFuture.allOf(chunks.stream().map(pos -> working.acquire().toCompletableFuture().thenCompose(unused ->
                    ((IStorageIoWorker) regionBaseIo).invokeReadChunkData(pos)
                            .thenCombineAsync(((IStorageIoWorker) regionTargetIo).invokeReadChunkData(pos), (chunkDataBase, chunkDataTarget) -> {
                                try {
                                    if (ChunkSerializer.getChunkType(chunkDataBase) == ChunkStatus.ChunkType.PROTOCHUNK
                                            || ChunkSerializer.getChunkType(chunkDataBase) == ChunkStatus.ChunkType.PROTOCHUNK)
                                        return null;
                                    final Map<ChunkSectionPos, ChunkSection> sectionsBase = readSections(pos, chunkDataBase);
                                    final Map<ChunkSectionPos, ChunkSection> sectionsTarget = readSections(pos, chunkDataTarget);
                                    sectionsBase.forEach((chunkSectionPos, chunkSectionBase) -> {
                                        final ChunkSection chunkSectionTarget = sectionsTarget.get(chunkSectionPos);
                                        if (chunkSectionBase == null || chunkSectionTarget == null) {
                                            completedBlocks.addAndGet(16 * 16 * 16);
                                            differenceBlocks.addAndGet(16 * 16 * 16);
                                            return;
                                        }
                                        for (int x = 0; x < 16; x++)
                                            for (int y = 0; y < 16; y++)
                                                for (int z = 0; z < 16; z++) {
                                                    final BlockState state1 = chunkSectionBase.getBlockState(x, y, z);
                                                    final BlockState state2 = chunkSectionTarget.getBlockState(x, y, z);
                                                    if (!blockStateEquals(state1, state2)) {
                                                        differenceBlocks.incrementAndGet();
                                                        if (!Registry.BLOCK.getId(state1.getBlock()).equals(Registry.BLOCK.getId(state2.getBlock()))) {
                                                            blockDifference.computeIfAbsent(Registry.BLOCK.getId(state1.getBlock()), unused1 -> new AtomicLong()).incrementAndGet();
                                                            blockDifference.computeIfAbsent(Registry.BLOCK.getId(state2.getBlock()), unused1 -> new AtomicLong()).incrementAndGet();
                                                        }
                                                    }
                                                    completedBlocks.incrementAndGet();
                                                }
                                    });
                                    return null;
                                } catch (Throwable t) {
                                    t.printStackTrace(System.err);
                                    throw new RuntimeException(t);
                                } finally {
                                    completedChunks.incrementAndGet();
                                    working.release();
                                }
                            })
            )).distinct().toArray(CompletableFuture[]::new));
            while (!future.isDone()) {
                System.out.printf("[noprint]%s: %d / %d (%.1f%%) chunks, %d blocks, %d block differences (%.4f%%)\n",
                        world, completedChunks.get(), totalChunks, completedChunks.get() / (float) totalChunks * 100.0,
                        completedBlocks.get(), differenceBlocks.get(), differenceBlocks.get() / completedBlocks.floatValue() * 100.0);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException ignored) {
                }
            }
            System.err.printf("Comparison completed for %s: block state differences: %d / %d (%.4f%%)\n",
                    world, differenceBlocks.get(), completedBlocks.get(), differenceBlocks.get() / completedBlocks.floatValue() * 100.0);
            System.err.print(blockDifference + "\n");
        }

    }

    @Override
    public void close() throws IOException {
        this.baseWorld.handle.close();
        this.targetWorld.handle.close();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static boolean blockStateEquals(BlockState state1, BlockState state2) {
        if (!Registry.BLOCK.getId(state1.getBlock()).equals(Registry.BLOCK.getId(state2.getBlock()))) return false;
        for (Property property : state1.getProperties()) {
            if (state1.get(property).compareTo(state2.get(property)) != 0) return false;
        }
        return true;
    }

    private static Map<ChunkSectionPos, ChunkSection> readSections(ChunkPos pos, NbtCompound chunkData) {
        NbtList nbtList = chunkData.getCompound("Level").getList("Sections", 10);
        HashMap<ChunkSectionPos, ChunkSection> result = new HashMap<>();
        for (int i = 0; i < nbtList.size(); i++) {
            final NbtCompound sectionData = nbtList.getCompound(i);
            int y = sectionData.getByte("Y");
            if (sectionData.contains("Palette", 9) && sectionData.contains("BlockStates", 12)) {
                final ChunkSection chunkSection = new ChunkSection(y);
                chunkSection.getContainer().read(sectionData.getList("Palette", 10), sectionData.getLongArray("BlockStates"));
                chunkSection.calculateCounts();
                result.put(ChunkSectionPos.from(pos, y), chunkSection);
            }
        }
        return result;
    }

    private static WorldHandle getWorldHandle(File worldFolder, String description) throws IOException {
        final LevelStorage levelStorage = LevelStorage.create(worldFolder.toPath());
        final LevelStorage.Session session = levelStorage.createSession(worldFolder.getAbsolutePath());

        System.out.printf("Reading world data for %s\n", description);
        DynamicRegistryManager.Impl impl = DynamicRegistryManager.create();
        DataPackSettings dataPackSettings = session.getDataPackSettings();
        ResourcePackManager resourcePackManager = new ResourcePackManager(ResourceType.SERVER_DATA, new VanillaDataPackProvider(), new FileResourcePackProvider(session.getDirectory(WorldSavePath.DATAPACKS).toFile(), ResourcePackSource.PACK_SOURCE_WORLD));
        DataPackSettings dataPackSettings2 = MinecraftServer.loadDataPacks(resourcePackManager, dataPackSettings == null ? DataPackSettings.SAFE_MODE : dataPackSettings, false);
        ServerResourceManager serverResourceManager2;
        try {
            serverResourceManager2 = ServerResourceManager.reload(resourcePackManager.createResourcePacks(), impl, CommandManager.RegistrationEnvironment.DEDICATED, 2, Util.getMainWorkerExecutor(), Runnable::run).get();
        } catch (Throwable t) {
            resourcePackManager.close();
            throw new RuntimeException("Cannot load data packs", t);
        }
        final RegistryOps<NbtElement> dynamicOps = RegistryOps.method_36574(NbtOps.INSTANCE, serverResourceManager2.getResourceManager(), impl);
        SaveProperties saveProperties = session.readLevelProperties(dynamicOps, dataPackSettings2);
        if (saveProperties == null) {
            resourcePackManager.close();
            throw new FileNotFoundException();
        }
        final ImmutableSet<RegistryKey<World>> worldKeys = saveProperties.getGeneratorOptions().getWorlds();
        final WorldUpdater worldUpdater = new WorldUpdater(session, Schemas.getFixer(), worldKeys, false);
        final HashMap<RegistryKey<World>, List<ChunkPos>> chunkPosesMap = new HashMap<>();
        for (RegistryKey<World> world : worldKeys) {
            System.out.printf("%s: Counting chunks for world %s\n", description, world);
            //noinspection ConstantConditions
            chunkPosesMap.put(world, ((IWorldUpdater) worldUpdater).invokeGetChunkPositions(world));
        }
        final HashMap<RegistryKey<World>, StorageIoWorker> regionIoWorkers = new HashMap<>();
        final HashMap<RegistryKey<World>, StorageIoWorker> poiIoWorkers = new HashMap<>();
        for (RegistryKey<World> world : worldKeys) {
            regionIoWorkers.put(world, new StorageIoWorker(new File(session.getWorldDirectory(world), "region"), true, "chunk") {
            });
            poiIoWorkers.put(world, new StorageIoWorker(new File(session.getWorldDirectory(world), "poi"), true, "poi") {
            });
        }
        return new WorldHandle(chunkPosesMap, regionIoWorkers, poiIoWorkers, () -> {
            System.out.println("Shutting down IOWorkers...");
            Stream.concat(regionIoWorkers.values().stream(), poiIoWorkers.values().stream()).forEach(storageIoWorker -> {
                storageIoWorker.completeAll().join();
                try {
                    storageIoWorker.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            System.out.println("Closing world");
            resourcePackManager.close();
            session.close();
            System.out.println("World closed");
        });
    }

    public record WorldHandle(HashMap<RegistryKey<World>, List<ChunkPos>> chunkPosesMap,
                              HashMap<RegistryKey<World>, StorageIoWorker> regionIoWorkers,
                              HashMap<RegistryKey<World>, StorageIoWorker> poiIoWorkers,
                              Closeable handle) {
    }

}
