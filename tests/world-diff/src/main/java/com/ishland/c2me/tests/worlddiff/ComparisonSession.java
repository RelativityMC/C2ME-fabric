package com.ishland.c2me.tests.worlddiff;

import com.google.common.collect.ImmutableSet;
import com.ibm.asyncutil.locks.AsyncSemaphore;
import com.ibm.asyncutil.locks.FairAsyncSemaphore;
import com.ishland.c2me.tests.worlddiff.mixin.IStorageIoWorker;
import com.ishland.c2me.tests.worlddiff.mixin.IWorldUpdater;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.class_6880;
import net.minecraft.class_6903;
import net.minecraft.class_6904;
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
import net.minecraft.resource.VanillaDataPackProvider;
import net.minecraft.server.command.CommandManager;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.PalettedContainer;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.storage.StorageIoWorker;
import net.minecraft.world.updater.WorldUpdater;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class ComparisonSession implements Closeable {

    // From ChunkSerializer
    private static final Codec<PalettedContainer<BlockState>> CODEC = PalettedContainer.createCodec(Block.STATE_IDS, BlockState.CODEC, PalettedContainer.PaletteProvider.BLOCK_STATE, Blocks.AIR.getDefaultState());

    private static final Logger LOGGER = LogManager.getLogger();

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
                                    final Map<ChunkSectionPos, ChunkSection> sectionsBase = readSections(pos, chunkDataBase, baseWorld.dynamicRegistryManager.get(Registry.BIOME_KEY));
                                    final Map<ChunkSectionPos, ChunkSection> sectionsTarget = readSections(pos, chunkDataTarget, baseWorld.dynamicRegistryManager.get(Registry.BIOME_KEY));
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

    private static Map<ChunkSectionPos, ChunkSection> readSections(ChunkPos pos, NbtCompound chunkData, Registry<Biome> registry) {
        NbtList nbtList = chunkData.getList("sections", 10);
        Codec<PalettedContainer<class_6880<Biome>>> codec = PalettedContainer.createCodec(registry.method_40295(), registry.method_40294(), PalettedContainer.PaletteProvider.BIOME, registry.method_40290(BiomeKeys.PLAINS));
        HashMap<ChunkSectionPos, ChunkSection> result = new HashMap<>();
        for (int i = 0; i < nbtList.size(); i++) {
            final NbtCompound sectionData = nbtList.getCompound(i);
            int y = sectionData.getByte("Y");
            if (sectionData.contains("block_states", 10)) {
                PalettedContainer<BlockState> palettedContainer;
                if (sectionData.contains("block_states", 10)) {
                    palettedContainer = CODEC.parse(NbtOps.INSTANCE, sectionData.getCompound("block_states"))
                            .promotePartial(s -> LOGGER.error("Recoverable errors when loading section [" + pos.x + ", " + y + ", " + pos.z + "]: " + s))
                            .getOrThrow(false, LOGGER::error);
                } else {
                    palettedContainer = new PalettedContainer<>(Block.STATE_IDS, Blocks.AIR.getDefaultState(), PalettedContainer.PaletteProvider.BLOCK_STATE);
                }

                PalettedContainer<class_6880<Biome>> palettedContainer3;
                if (sectionData.contains("biomes", 10)) {
                    palettedContainer3 = codec.parse(NbtOps.INSTANCE, sectionData.getCompound("biomes"))
                            .promotePartial(s -> LOGGER.error("Recoverable errors when loading section [" + pos.x + ", " + y + ", " + pos.z + "]: " + s))
                            .getOrThrow(false, LOGGER::error);
                } else {
                    palettedContainer3 = new PalettedContainer<>(registry.method_40295(), registry.method_40290(BiomeKeys.PLAINS), PalettedContainer.PaletteProvider.BIOME);
                }
                ChunkSection chunkSection = new ChunkSection(y, palettedContainer, palettedContainer3);
                chunkSection.calculateCounts();
                result.put(ChunkSectionPos.from(pos, y), chunkSection);
            }
        }
        return result;
    }

    private static WorldHandle getWorldHandle(File worldFolder, String description) throws IOException, TimeoutException {
        final LevelStorage levelStorage = LevelStorage.create(worldFolder.toPath());
        final LevelStorage.Session session = levelStorage.createSession(worldFolder.getAbsolutePath());

        System.out.printf("Reading world data for %s\n", description);
        ResourcePackManager resourcePackManager = new ResourcePackManager(
                ResourceType.SERVER_DATA,
                new VanillaDataPackProvider(),
                new FileResourcePackProvider(session.getDirectory(WorldSavePath.DATAPACKS).toFile(), ResourcePackSource.PACK_SOURCE_WORLD)
        );

        class_6904 lv2;
        try {
            class_6904.class_6906 lv = new class_6904.class_6906(
                    resourcePackManager,
                    CommandManager.RegistrationEnvironment.DEDICATED,
                    2,
                    false
            );
            lv2 = class_6904.method_40431(
                            lv,
                            () -> {
                                DataPackSettings dataPackSettings = session.getDataPackSettings();
                                return dataPackSettings == null ? DataPackSettings.SAFE_MODE : dataPackSettings;
                            },
                            (resourceManager, dataPackSettings) -> {
                                DynamicRegistryManager.class_6893 lvx = DynamicRegistryManager.method_40314();
                                DynamicOps<NbtElement> dynamicOps = class_6903.method_40412(NbtOps.INSTANCE, lvx, resourceManager);
                                SaveProperties savePropertiesx = session.readLevelProperties(dynamicOps, dataPackSettings);
                                if (savePropertiesx != null) {
                                    return Pair.of(savePropertiesx, lvx.method_40316());
                                } else {
                                    throw new RuntimeException("Failed to load level properties");
                                }
                            },
                            Util.getMainWorkerExecutor(),
                            Runnable::run
                    )
                    .get(15, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw e;
        } catch (Exception var38) {
            LOGGER.warn(
                    "Failed to load datapacks, can't proceed with server load. You can either fix your datapacks or reset to vanilla with --safeMode", var38
            );
            resourcePackManager.close();
            throw new RuntimeException(var38);
        }

        lv2.method_40428();
        DynamicRegistryManager.class_6890 lv = lv2.registryAccess();
//        serverPropertiesLoader.getPropertiesHandler().getGeneratorOptions(lv);
        SaveProperties saveProperties = lv2.worldData();
        if (saveProperties == null) {
            resourcePackManager.close();
            throw new FileNotFoundException();
        }
        final ImmutableSet<RegistryKey<World>> worldKeys = saveProperties.getGeneratorOptions().getWorlds();
        final WorldUpdater worldUpdater = new WorldUpdater(session, Schemas.getFixer(), saveProperties.getGeneratorOptions(), false);
        final HashMap<RegistryKey<World>, List<ChunkPos>> chunkPosesMap = new HashMap<>();
        for (RegistryKey<World> world : worldKeys) {
            System.out.printf("%s: Counting chunks for world %s\n", description, world);
            //noinspection ConstantConditions
            chunkPosesMap.put(world, ((IWorldUpdater) worldUpdater).invokeGetChunkPositions(world));
        }
        final HashMap<RegistryKey<World>, StorageIoWorker> regionIoWorkers = new HashMap<>();
        final HashMap<RegistryKey<World>, StorageIoWorker> poiIoWorkers = new HashMap<>();
        for (RegistryKey<World> world : worldKeys) {
            regionIoWorkers.put(world, new StorageIoWorker(session.getWorldDirectory(world).resolve("region"), true, "chunk") {
            });
            poiIoWorkers.put(world, new StorageIoWorker(session.getWorldDirectory(world).resolve("poi"), true, "poi") {
            });
        }
        return new WorldHandle(chunkPosesMap, regionIoWorkers, poiIoWorkers, lv, () -> {
            System.out.println("Shutting down IOWorkers...");
            Stream.concat(regionIoWorkers.values().stream(), poiIoWorkers.values().stream()).forEach(storageIoWorker -> {
                storageIoWorker.completeAll(true).join();
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
                              DynamicRegistryManager dynamicRegistryManager,
                              Closeable handle) {
    }

}
