package com.ishland.c2me.tests.testmod.mixin.pregen;

import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.function.Supplier;

@Mixin(ServerWorld.class)
public abstract class MixinServerWorld extends World implements StructureWorldAccess {

    protected MixinServerWorld(MutableWorldProperties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> registryEntry, Supplier<Profiler> profiler, boolean isClient, boolean debugWorld, long seed, int i) {
        super(properties, registryRef, registryEntry, profiler, isClient, debugWorld, seed, i);
    }

    /**
     * @author ishland
     * @reason no ticking
     */
    @Overwrite
    private void tickFluid(BlockPos blockPos, Fluid fluid) {
        // nope
    }

    /**
     * @author ishland
     * @reason no ticking
     */
    @Overwrite
    private void tickBlock(BlockPos blockPos, Block fluid) {
        // nope
    }

}
