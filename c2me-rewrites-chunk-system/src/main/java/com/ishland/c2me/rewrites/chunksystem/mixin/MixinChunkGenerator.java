package com.ishland.c2me.rewrites.chunksystem.mixin;

import com.ishland.c2me.base.common.util.InvokingExecutorService;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import net.minecraft.SharedConstants;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.entry.RegistryEntryList;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.random.ChunkRandom;
import net.minecraft.util.math.random.RandomSeed;
import net.minecraft.util.math.random.Xoroshiro128PlusPlusRandom;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.GenerationSettings;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.feature.PlacedFeature;
import net.minecraft.world.gen.feature.util.PlacedFeatureIndexer;
import net.minecraft.world.gen.structure.Structure;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

@Mixin(ChunkGenerator.class)
public abstract class MixinChunkGenerator {

    @Unique
    private static final int[] C2ME$EMPTY_INT_ARRAY = new int[0];

    @Unique
    private static final int C2ME$FEATURE_SELECTION_CACHE_LIMIT = 65536;

    @Shadow
    @Final
    protected BiomeSource biomeSource;

    @Shadow
    @Final
    private Supplier<List<PlacedFeatureIndexer.IndexedFeatures>> indexedFeaturesListSupplier;

    @Shadow
    @Final
    private Function<RegistryEntry<Biome>, GenerationSettings> generationSettingsGetter;

    @Unique
    private final ConcurrentHashMap<FeatureSelectionKey, int[][]> c2me$featureSelectionCache = new ConcurrentHashMap<>();

    @Unique
    private final ConcurrentHashMap<Registry<Structure>, List<Structure>[]> c2me$structuresByStepCache = new ConcurrentHashMap<>();

    @Redirect(method = "populateBiomes", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Util;getMainWorkerExecutor()Ljava/util/concurrent/ExecutorService;"))
    private ExecutorService redirectBiomeExecutor() {
        return InvokingExecutorService.INSTANCE;
    }

    /**
     * @author ishland
     * @reason cache the per-biome-set sorted feature id selection while keeping vanilla generation order and seeds
     */
    @Overwrite
    public void generateFeatures(StructureWorldAccess world, Chunk chunk, StructureAccessor structureAccessor) {
        ChunkPos chunkPos = chunk.getPos();
        if (SharedConstants.isOutsideGenerationArea(chunkPos)) return;

        ChunkSectionPos sectionPos = ChunkSectionPos.from(chunkPos, world.getBottomSectionCoord());
        BlockPos origin = sectionPos.getMinPos();
        Registry<Structure> structureRegistry = world.getRegistryManager().get(RegistryKeys.STRUCTURE);
        List<Structure>[] structuresByStep = this.c2me$getStructuresByStep(structureRegistry);
        List<PlacedFeatureIndexer.IndexedFeatures> indexedFeaturesList = this.indexedFeaturesListSupplier.get();
        ChunkRandom random = new ChunkRandom(new Xoroshiro128PlusPlusRandom(RandomSeed.getSeed()));
        long populationSeed = random.setPopulationSeed(world.getSeed(), origin.getX(), origin.getZ());
        Set<RegistryEntry<Biome>> possibleBiomes = this.c2me$collectPossibleBiomes(world, sectionPos);
        int[][] selectedFeatureIds = this.c2me$getSelectedFeatureIds(possibleBiomes, indexedFeaturesList);
        Registry<PlacedFeature> placedFeatureRegistry = world.getRegistryManager().get(RegistryKeys.PLACED_FEATURE);
        int stepCount = Math.max(GenerationStep.Feature.values().length, indexedFeaturesList.size());

        for (int step = 0; step < stepCount; ++step) {
            this.c2me$generateStructuresForStep(world, chunk, structureAccessor, structureRegistry, sectionPos, chunkPos, random, populationSeed, step, structuresByStep);
            if (step < indexedFeaturesList.size()) {
                this.c2me$generateFeaturesForStep(world, placedFeatureRegistry, indexedFeaturesList, selectedFeatureIds, random, populationSeed, origin, step);
            }
        }

        world.setCurrentlyGeneratingStructureName(null);
    }

    @Unique
    private Set<RegistryEntry<Biome>> c2me$collectPossibleBiomes(StructureWorldAccess world, ChunkSectionPos sectionPos) {
        ObjectArraySet<RegistryEntry<Biome>> possibleBiomes = new ObjectArraySet<>();
        ChunkPos center = sectionPos.toChunkPos();
        for (int chunkX = center.x - 1; chunkX <= center.x + 1; ++chunkX) {
            for (int chunkZ = center.z - 1; chunkZ <= center.z + 1; ++chunkZ) {
                for (ChunkSection section : world.getChunk(chunkX, chunkZ).getSectionArray()) {
                    section.getBiomeContainer().forEachValue(possibleBiomes::add);
                }
            }
        }
        possibleBiomes.retainAll(this.biomeSource.getBiomes());
        return possibleBiomes;
    }

    @Unique
    private int[][] c2me$getSelectedFeatureIds(Set<RegistryEntry<Biome>> possibleBiomes, List<PlacedFeatureIndexer.IndexedFeatures> indexedFeaturesList) {
        FeatureSelectionKey key = new FeatureSelectionKey(indexedFeaturesList, possibleBiomes);
        int[][] cached = this.c2me$featureSelectionCache.get(key);
        if (cached != null) return cached;

        int[][] selected = this.c2me$collectSelectedFeatureIds(key.biomes, indexedFeaturesList);
        if (this.c2me$featureSelectionCache.size() >= C2ME$FEATURE_SELECTION_CACHE_LIMIT) {
            return selected;
        }
        int[][] previous = this.c2me$featureSelectionCache.putIfAbsent(key, selected);
        return previous != null ? previous : selected;
    }

    @Unique
    private List<Structure>[] c2me$getStructuresByStep(Registry<Structure> structureRegistry) {
        List<Structure>[] cached = this.c2me$structuresByStepCache.get(structureRegistry);
        if (cached != null) return cached;

        List<Structure>[] grouped = c2me$collectStructuresByStep(structureRegistry);
        List<Structure>[] previous = this.c2me$structuresByStepCache.putIfAbsent(structureRegistry, grouped);
        return previous != null ? previous : grouped;
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static List<Structure>[] c2me$collectStructuresByStep(Registry<Structure> structureRegistry) {
        List<Structure>[] grouped = new List[GenerationStep.Feature.values().length];
        structureRegistry.stream().forEach(structure -> {
            int step = structure.getFeatureGenerationStep().ordinal();
            List<Structure> structures = grouped[step];
            if (structures == null) {
                structures = new ArrayList<>();
                grouped[step] = structures;
            }
            structures.add(structure);
        });
        for (int i = 0; i < grouped.length; ++i) {
            grouped[i] = grouped[i] == null ? Collections.emptyList() : List.copyOf(grouped[i]);
        }
        return grouped;
    }

    @Unique
    private int[][] c2me$collectSelectedFeatureIds(RegistryEntry<Biome>[] biomes, List<PlacedFeatureIndexer.IndexedFeatures> indexedFeaturesList) {
        IntArraySet[] selectedByStep = new IntArraySet[indexedFeaturesList.size()];
        for (RegistryEntry<Biome> biome : biomes) {
            List<RegistryEntryList<PlacedFeature>> features = this.generationSettingsGetter.apply(biome).getFeatures();
            int stepLimit = Math.min(features.size(), indexedFeaturesList.size());
            for (int step = 0; step < stepLimit; ++step) {
                RegistryEntryList<PlacedFeature> featureEntries = features.get(step);
                if (featureEntries.size() == 0) continue;
                IntArraySet selected = selectedByStep[step];
                if (selected == null) {
                    selected = new IntArraySet();
                    selectedByStep[step] = selected;
                }
                PlacedFeatureIndexer.IndexedFeatures indexedFeatures = indexedFeaturesList.get(step);
                for (int i = 0, size = featureEntries.size(); i < size; ++i) {
                    selected.add(indexedFeatures.indexMapping().applyAsInt(featureEntries.get(i).value()));
                }
            }
        }

        int[][] selectedArrays = new int[indexedFeaturesList.size()][];
        for (int step = 0; step < selectedArrays.length; ++step) {
            IntArraySet selected = selectedByStep[step];
            if (selected == null || selected.isEmpty()) {
                selectedArrays[step] = C2ME$EMPTY_INT_ARRAY;
            } else {
                int[] ids = selected.toIntArray();
                Arrays.sort(ids);
                selectedArrays[step] = ids;
            }
        }
        return selectedArrays;
    }

    @Unique
    private void c2me$generateStructuresForStep(
            StructureWorldAccess world,
            Chunk chunk,
            StructureAccessor structureAccessor,
            Registry<Structure> structureRegistry,
            ChunkSectionPos sectionPos,
            ChunkPos chunkPos,
            ChunkRandom random,
            long populationSeed,
            int step,
            List<Structure>[] structuresByStep
    ) {
        if (!structureAccessor.shouldGenerateStructures()) return;
        int structureIndex = 0;
        List<Structure> structures = step < structuresByStep.length ? structuresByStep[step] : Collections.emptyList();
        for (Structure structure : structures) {
            random.setDecoratorSeed(populationSeed, structureIndex, step);
            Supplier<String> name = () -> c2me$getRegistryName(structureRegistry, structure);
            world.setCurrentlyGeneratingStructureName(name);
            try {
                for (StructureStart start : structureAccessor.getStructureStarts(sectionPos, structure)) {
                    start.place(world, structureAccessor, (ChunkGenerator) (Object) this, random, c2me$getBlockBoxForChunk(chunk), chunkPos);
                }
            } catch (Exception e) {
                throw c2me$createFeatureCrash(e, name);
            }
            ++structureIndex;
        }
    }

    @Unique
    private void c2me$generateFeaturesForStep(
            StructureWorldAccess world,
            Registry<PlacedFeature> placedFeatureRegistry,
            List<PlacedFeatureIndexer.IndexedFeatures> indexedFeaturesList,
            int[][] selectedFeatureIds,
            ChunkRandom random,
            long populationSeed,
            BlockPos origin,
            int step
    ) {
        PlacedFeatureIndexer.IndexedFeatures indexedFeatures = indexedFeaturesList.get(step);
        int[] ids = step < selectedFeatureIds.length ? selectedFeatureIds[step] : C2ME$EMPTY_INT_ARRAY;
        for (int id : ids) {
            PlacedFeature feature = indexedFeatures.features().get(id);
            Supplier<String> name = () -> c2me$getRegistryName(placedFeatureRegistry, feature);
            random.setDecoratorSeed(populationSeed, id, step);
            world.setCurrentlyGeneratingStructureName(name);
            try {
                feature.generate(world, (ChunkGenerator) (Object) this, random, origin);
            } catch (Exception e) {
                throw c2me$createFeatureCrash(e, name);
            }
        }
    }

    @Unique
    private static BlockBox c2me$getBlockBoxForChunk(Chunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        int startX = chunkPos.getStartX();
        int startZ = chunkPos.getStartZ();
        return new BlockBox(startX, chunk.getBottomY() + 1, startZ, startX + 15, chunk.getTopY() - 1, startZ + 15);
    }

    @Unique
    private static <T> String c2me$getRegistryName(Registry<T> registry, T value) {
        return registry.getKey(value)
                .map(RegistryKey::getValue)
                .map(Object::toString)
                .orElseGet(value::toString);
    }

    @Unique
    private static CrashException c2me$createFeatureCrash(Exception exception, Supplier<String> description) {
        CrashReport crashReport = CrashReport.create(exception, "Feature placement");
        crashReport.addElement("Feature").add("Description", description::get);
        return new CrashException(crashReport);
    }

    @Unique
    private static final class FeatureSelectionKey {

        private final Object indexedFeaturesList;
        private final RegistryEntry<Biome>[] biomes;
        private final int hash;

        @SuppressWarnings("unchecked")
        private FeatureSelectionKey(Object indexedFeaturesList, Set<RegistryEntry<Biome>> biomes) {
            this.indexedFeaturesList = indexedFeaturesList;
            this.biomes = biomes.toArray(new RegistryEntry[0]);
            Arrays.sort(this.biomes, (a, b) -> {
                if (a == b) return 0;
                int byIdentity = Integer.compare(System.identityHashCode(a), System.identityHashCode(b));
                if (byIdentity != 0) return byIdentity;
                return a.getIdAsString().compareTo(b.getIdAsString());
            });
            int result = System.identityHashCode(indexedFeaturesList);
            for (RegistryEntry<Biome> biome : this.biomes) {
                result = 31 * result + System.identityHashCode(biome);
            }
            this.hash = result;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FeatureSelectionKey that)) return false;
            return this.indexedFeaturesList == that.indexedFeaturesList && c2me$identityArrayEquals(this.biomes, that.biomes);
        }

        @Override
        public int hashCode() {
            return this.hash;
        }

        private static boolean c2me$identityArrayEquals(Object[] a, Object[] b) {
            if (a.length != b.length) return false;
            for (int i = 0; i < a.length; ++i) {
                if (a[i] != b[i]) return false;
            }
            return true;
        }
    }

}
