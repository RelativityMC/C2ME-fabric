package com.ishland.c2me.opts.worldgen.vanilla.mixin.aquifer;

import com.ishland.c2me.opts.worldgen.general.common.random_instances.RandomUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.math.random.RandomSplitter;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.minecraft.util.math.BlockPos.SIZE_BITS_X;

@Mixin(AquiferSampler.Impl.class)
public abstract class MixinAquiferSamplerImpl {

    @Unique
    private static final int WATER_LEVEL_MAGIC_1 = 64 - BlockPos.BIT_SHIFT_X - SIZE_BITS_X;
    @Unique
    private static final int WATER_LEVEL_MAGIC_2 = 64 - SIZE_BITS_X;
    @Unique
    private static final int WATER_LEVEL_MAGIC_3 = 64 - BlockPos.SIZE_BITS_Y;
    @Unique
    private static final int WATER_LEVEL_MAGIC_4 = 64 - BlockPos.SIZE_BITS_Y;
    @Unique
    private static final int WATER_LEVEL_MAGIC_5 = 64 - BlockPos.BIT_SHIFT_Z - BlockPos.SIZE_BITS_Z;
    @Unique
    private static final int WATER_LEVEL_MAGIC_6 = 64 - BlockPos.SIZE_BITS_Z;


    @Shadow
    @Final
    private int startX;

    @Shadow
    @Final
    private int startY;

    @Shadow
    @Final
    private int startZ;

    @Shadow
    @Final
    private int sizeZ;

    @Shadow @Final private int sizeX;

    @Shadow @Final private long[] blockPositions;

    @Shadow @Final private RandomSplitter randomDeriver;

    @Shadow
    @Final
    private AquiferSampler.FluidLevel[] waterLevels;

    @Shadow
    @Final
    private static int[][] CHUNK_POS_OFFSETS;

    @Shadow
    @Final
    private ChunkNoiseSampler chunkNoiseSampler;

    @Shadow
    @Final
    private DensityFunction barrierNoise;

    @Shadow
    @Final
    private DensityFunction fluidLevelFloodednessNoise;

    @Shadow
    @Final
    private DensityFunction fluidLevelSpreadNoise;

    @Shadow
    @Final
    private DensityFunction fluidTypeNoise;

    @Shadow
    @Final
    private static double NEEDS_FLUID_TICK_DISTANCE_THRESHOLD;

    @Shadow
    private boolean needsFluidTick;

    @Shadow
    @Final
    private AquiferSampler.FluidLevelSampler fluidLevelSampler;

    @Shadow protected abstract int index(int x, int y, int z);

    @Shadow
    protected static double maxDistance(int i, int a) {
        throw new AbstractMethodError();
    }

    @Shadow protected abstract double calculateDensity(DensityFunction.NoisePos pos, MutableDouble mutableDouble, AquiferSampler.FluidLevel fluidLevel, AquiferSampler.FluidLevel fluidLevel2);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo info) {
        // preload position cache
        if (this.blockPositions.length % (this.sizeX * this.sizeZ) != 0) {
            throw new AssertionError("Array length");
        }
        int sizeY = this.blockPositions.length / (this.sizeX * this.sizeZ);
        final Random random = RandomUtils.getRandom(this.randomDeriver);
        // index: z, x, y
        for (int z = 0; z < this.sizeZ; z++) {
            for (int x = 0; x < this.sizeX; x++) {
                for (int y = 0; y < sizeY; y++) {
                    final int x1 = x + this.startX;
                    final int y1 = y + this.startY;
                    final int z1 = z + this.startZ;
                    RandomUtils.derive(this.randomDeriver, random, x1, y1, z1);
                    this.blockPositions[this.index(x1, y1, z1)] = BlockPos.asLong(x1 * 16 + random.nextInt(10), y1 * 12 + random.nextInt(9), z1 * 16 + random.nextInt(10));
                }
            }
        }
        for (long blockPosition : this.blockPositions) {
            if (blockPosition == Long.MAX_VALUE) {
                throw new AssertionError("Array initialization");
            }
        }
    }

    /**
     * @author ishland
     * @reason make C2 happier by splitting method into two
     */
    @Overwrite
    public BlockState apply(DensityFunction.NoisePos pos, double density) {
        int i = pos.blockX();
        int j = pos.blockY();
        int k = pos.blockZ();
        if (density > 0.0) {
            this.needsFluidTick = false;
            return null;
        } else {
            AquiferSampler.FluidLevel fluidLevel = this.fluidLevelSampler.getFluidLevel(i, j, k);
            if (fluidLevel.getBlockState(j).isOf(Blocks.LAVA)) {
                this.needsFluidTick = false;
                return Blocks.LAVA.getDefaultState();
            } else {
                final Result1 result = getResult1(i, j, k);

                AquiferSampler.FluidLevel fluidLevel2 = this.getWaterLevel(result.r());
                double d = maxDistance(result.o(), result.p());
                BlockState blockState = fluidLevel2.getBlockState(j);
                if (d <= 0.0) {
                    this.needsFluidTick = d >= NEEDS_FLUID_TICK_DISTANCE_THRESHOLD;
                    return blockState;
                } else if (blockState.isOf(Blocks.WATER) && this.fluidLevelSampler.getFluidLevel(i, j - 1, k).getBlockState(j - 1).isOf(Blocks.LAVA)) {
                    this.needsFluidTick = true;
                    return blockState;
                } else {
                    MutableDouble mutableDouble = new MutableDouble(Double.NaN);
                    AquiferSampler.FluidLevel fluidLevel3 = this.getWaterLevel(result.s());
                    double e = d * this.calculateDensity(pos, mutableDouble, fluidLevel2, fluidLevel3);
                    if (density + e > 0.0) {
                        this.needsFluidTick = false;
                        return null;
                    } else {
                        AquiferSampler.FluidLevel fluidLevel4 = this.getWaterLevel(result.t());
                        double f = maxDistance(result.o(), result.q());
                        if (f > 0.0) {
                            double g = d * f * this.calculateDensity(pos, mutableDouble, fluidLevel2, fluidLevel4);
                            if (density + g > 0.0) {
                                this.needsFluidTick = false;
                                return null;
                            }
                        }

                        double g = maxDistance(result.p(), result.q());
                        if (g > 0.0) {
                            double h = d * g * this.calculateDensity(pos, mutableDouble, fluidLevel3, fluidLevel4);
                            if (density + h > 0.0) {
                                this.needsFluidTick = false;
                                return null;
                            }
                        }

                        this.needsFluidTick = true;
                        return blockState;
                    }
                }
            }
        }
    }

    @NotNull
    private Result1 getResult1(int i, int j, int k) {
        int l = Math.floorDiv(i - 5, 16);
        int m = Math.floorDiv(j + 1, 12);
        int n = Math.floorDiv(k - 5, 16);
        int o = Integer.MAX_VALUE;
        int p = Integer.MAX_VALUE;
        int q = Integer.MAX_VALUE;
        long r = 0L;
        long s = 0L;
        long t = 0L;

        for(int u = 0; u <= 1; ++u) {
            for(int v = -1; v <= 1; ++v) {
                for(int w = 0; w <= 1; ++w) {
                    int x = l + u;
                    int y = m + v;
                    int z = n + w;
                    int aa = this.index(x, y, z);
                    long ab = this.blockPositions[aa];
                    long ac = ab; // cache preloaded

                    int ad = BlockPos.unpackLongX(ac) - i;
                    int ae = BlockPos.unpackLongY(ac) - j;
                    int af = BlockPos.unpackLongZ(ac) - k;
                    int ag = ad * ad + ae * ae + af * af;
                    if (o >= ag) {
                        t = s;
                        s = r;
                        r = ac;
                        q = p;
                        p = o;
                        o = ag;
                    } else if (p >= ag) {
                        t = s;
                        s = ac;
                        q = p;
                        p = ag;
                    } else if (q >= ag) {
                        t = ac;
                        q = ag;
                    }
                }
            }
        }
        Result1 result = new Result1(o, p, q, r, s, t);
        return result;
    }

    @SuppressWarnings("MixinInnerClass")
    private record Result1(int o, int p, int q, long r, long s, long t) {
    }


    /**
     * @author ishland
     * @reason optimize
     */
    @Overwrite
    private AquiferSampler.FluidLevel getWaterLevel(long pos) {
        int i = (int) ((pos << WATER_LEVEL_MAGIC_1) >> WATER_LEVEL_MAGIC_2); // C2ME - inline
        int j = (int) ((pos << WATER_LEVEL_MAGIC_3) >> WATER_LEVEL_MAGIC_4); // C2ME - inline
        int k = (int) ((pos << WATER_LEVEL_MAGIC_5) >> WATER_LEVEL_MAGIC_6); // C2ME - inline
        int l = Math.floorDiv(i, 16); // C2ME - inline
        int m = Math.floorDiv(j, 12); // C2ME - inline
        int n = Math.floorDiv(k, 16); // C2ME - inline
        int o = ((m - this.startY) * this.sizeZ + n - this.startZ) * this.sizeX + l - this.startX;
        AquiferSampler.FluidLevel fluidLevel = this.waterLevels[o];
        if (fluidLevel != null) {
            return fluidLevel;
        } else {
            AquiferSampler.FluidLevel fluidLevel2 = this.getFluidLevel(i, j, k);
            this.waterLevels[o] = fluidLevel2;
            return fluidLevel2;
        }
    }

    /**
     * @author ishland
     * @reason optimize
     */
    @Overwrite
    private AquiferSampler.FluidLevel getFluidLevel(int i, int j, int k) {
        AquiferSampler.FluidLevel fluidLevel = this.fluidLevelSampler.getFluidLevel(i, j, k);
        int l = Integer.MAX_VALUE;
        int m = j + 12;
        int n = j - 12;
        boolean bl = false;

        for (int[] is : CHUNK_POS_OFFSETS) {
            int o = i + (is[0] << 4); // C2ME - inline
            int p = k + (is[1] << 4); // C2ME - inline
            int q = this.chunkNoiseSampler.estimateSurfaceHeight(o, p);
            int r = q + 8;
            boolean bl2 = is[0] == 0 && is[1] == 0;
            if (bl2 && n > r) {
                return fluidLevel;
            }

            boolean bl3 = m > r;
            if (bl2 || bl3) {
                AquiferSampler.FluidLevel fluidLevel2 = this.fluidLevelSampler.getFluidLevel(o, r, p);
                if (!fluidLevel2.getBlockState(r).isAir()) {
                    if (bl2) {
                        bl = true;
                    }

                    if (bl3) {
                        return fluidLevel2;
                    }
                }
            }

            l = Math.min(l, q);
        }

        int s = l + 8 - j;
        double d = bl ? clampedLerpFromProgressInlined(s) : 0.0;
        double e = MathHelper.clamp(this.fluidLevelFloodednessNoise.sample(new DensityFunction.UnblendedNoisePos(i, j, k)), -1.0, 1.0);
        double f = lerpFromProgressInlined(d, -0.3, 0.8);
        if (e > f) {
            return fluidLevel;
        } else {
            double g = lerpFromProgressInlined(d, -0.8, 0.4);
            if (e <= g) {
                return new AquiferSampler.FluidLevel(DimensionType.field_35479, fluidLevel.state);
            } else {
                int w = Math.floorDiv(i, 16);
                int x = Math.floorDiv(j, 40);
                int y = Math.floorDiv(k, 16);
                int z = x * 40 + 20;
                double h = this.fluidLevelSpreadNoise.sample(new DensityFunction.UnblendedNoisePos(w, x, y)) * 10.0;
                int ab = MathHelper.roundDownToMultiple(h, 3);
                int ac = z + ab;
                int ad = Math.min(l, ac);
                if (ac <= -10) {
                    int ag = Math.floorDiv(i, 64);
                    int ah = Math.floorDiv(j, 40);
                    int ai = Math.floorDiv(k, 64);
                    double aj = this.fluidTypeNoise.sample(new DensityFunction.UnblendedNoisePos(ag, ah, ai));
                    if (Math.abs(aj) > 0.3) {
                        return new AquiferSampler.FluidLevel(ad, Blocks.LAVA.getDefaultState());
                    }
                }

                return new AquiferSampler.FluidLevel(ad, fluidLevel.state);
            }
        }
    }

    @Unique
    private static double clampedLerpFromProgressInlined(double lerpValue) {
        final double delta = lerpValue / 64.0;
        if (delta < 0.0) {
            return 1.0;
        } else {
            return delta > 1.0 ? 0.0 : 1.0 - delta;
        }
    }

    @Unique
    private static double lerpFromProgressInlined(double lerpValue, double start, double end) {
        return start - (lerpValue - 1.0) * (end - start);
    }

}
