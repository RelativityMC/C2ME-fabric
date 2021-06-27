package com.ishland.c2me.tests.worlddiff;

import com.google.common.collect.ImmutableSet;
import com.ishland.c2me.tests.worlddiff.mixin.IWorldUpdater;
import net.minecraft.datafixer.Schemas;
import net.minecraft.nbt.NbtElement;
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
import net.minecraft.util.Util;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.World;
import net.minecraft.world.level.storage.LevelStorage;
import net.minecraft.world.storage.StorageIoWorker;
import net.minecraft.world.updater.WorldUpdater;

import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class ComparisonSession {

    public ComparisonSession(File from, File to) {
        try {
            getWorldHandle(from, "Base world").handle.close();
            getWorldHandle(to, "Target world").handle.close();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
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
            Stream.concat(regionIoWorkers.values().stream(), poiIoWorkers.values().stream()).forEach(storageIoWorker -> {
                storageIoWorker.completeAll().join();
                try {
                    storageIoWorker.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            resourcePackManager.close();
            session.close();
        });
    }

    public record WorldHandle(HashMap<RegistryKey<World>, List<ChunkPos>> chunkPosesMap,
                              HashMap<RegistryKey<World>, StorageIoWorker> regionIoWorkers,
                              HashMap<RegistryKey<World>, StorageIoWorker> poiIoWorkers,
                              Closeable handle) {
    }

}
