package com.ishland.c2me.mixin.optimization.worldgen.vanilla_optimization.aquifer;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.DoublePerlinNoiseSampler;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.chunk.AquiferSampler;
import net.minecraft.world.gen.chunk.ChunkNoiseSampler;
import net.minecraft.world.gen.random.AbstractRandom;
import net.minecraft.world.gen.random.RandomDeriver;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AquiferSampler.Impl.class)
public class MixinAquiferSamplerImpl {

    @Shadow @Final private AquiferSampler.FluidLevelSampler fluidLevelSampler;

    @Shadow @Final private int startX;

    @Shadow @Final private int startY;

    @Shadow @Final private int startZ;

    @Shadow @Final private int sizeZ;

    @Shadow @Final private int sizeX;

    @Shadow @Final private long[] blockPositions;

    @Shadow @Final private RandomDeriver randomDeriver;

    @Shadow @Final private AquiferSampler.FluidLevel[] waterLevels;

    @Shadow @Final private static int[][] field_34581;

    @Shadow @Final private ChunkNoiseSampler chunkNoiseSampler;

    @Shadow @Final private DoublePerlinNoiseSampler fluidLevelFloodednessNoise;

    @Shadow @Final private DoublePerlinNoiseSampler fluidLevelSpreadNoise;

    @Shadow @Final private DoublePerlinNoiseSampler fluidTypeNoise;

    @Shadow @Final private static double field_36221;

    @Shadow private boolean needsFluidTick;

    @Shadow @Final private DoublePerlinNoiseSampler barrierNoise;

    /**
     * @author ishland
     * @reason optimize
     */
    @Overwrite
    private AquiferSampler.FluidLevel getWaterLevel(long pos) {
        // TODO [VanillaCopy] reordered
        final int var0 = 64 - BlockPos.BIT_SHIFT_X;
        final int var1 = 64 - BlockPos.SIZE_BITS_X;
        final int var2 = 64 - BlockPos.SIZE_BITS_Y;
        final int var3 = 64 - BlockPos.BIT_SHIFT_Z;
        final int var4 = 64 - BlockPos.SIZE_BITS_Z;
        final int var5 = var0 - BlockPos.SIZE_BITS_X;
        final int var6 = var3 - BlockPos.SIZE_BITS_Z;
        int i = (int) ((pos << var5) >> var1);
        int j = (int) ((pos << var2) >> var2);
        int k = (int) ((pos << var6) >> var4);
        int l = Math.floorDiv(i, 16);
        int m = Math.floorDiv(j, 12);
        int n = Math.floorDiv(k, 16);
        int o = ((((((m - this.startY) * this.sizeZ) + n) - this.startZ) * this.sizeX) + l) - this.startX;
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
    public AquiferSampler.FluidLevel getFluidLevel(int x, int y, int z) {
        // TODO [VanillaCopy]
        AquiferSampler.FluidLevel fluidLevel = this.fluidLevelSampler.getFluidLevel(x, y, z);
        int i = Integer.MAX_VALUE;
        int j = y + 12;
        int k = y - 12;
        boolean bl = false;

        for(int[] is : field_34581) {
            int l = x + (is[0] << 4); // C2ME - inline
            int m = z + (is[1] << 4); // C2ME - inline
            int n = this.chunkNoiseSampler.method_39900(l, m);
            int o = n + 8;
            boolean bl2 = is[0] == 0 && is[1] == 0;
            if (bl2 && k > o) {
                return fluidLevel;
            }

            boolean bl3 = j > o;
            if (bl2 || bl3) {  // C2ME - flipped
                AquiferSampler.FluidLevel fluidLevel2 = this.fluidLevelSampler.getFluidLevel(l, o, m);
                if (!fluidLevel2.getBlockState(o).isAir()) {
                    if (bl2) {
                        bl = true;
                    }

                    if (bl3) {
                        return fluidLevel2;
                    }
                }
            }

            i = Math.min(i, n);
        }

        int p = i + 8 - y;
        double d = bl ? clampedLerpFromProgressInlined(p) : 0.0; // C2ME - inline values
        double bl2 = MathHelper.lerpFromProgress(d, 1.0, 0.0, -0.3, 0.8);
        double n = MathHelper.clamp(this.fluidLevelFloodednessNoise.sample(x, y * 0.67, z), -1.0, 1.0);
        if (n > bl2) {
            return fluidLevel;
        } else {
            double fluidLevel2 = MathHelper.lerpFromProgress(d, 1.0, 0.0, -0.8, 0.4);
            if (n <= fluidLevel2) {
                return new AquiferSampler.FluidLevel(DimensionType.field_35479, fluidLevel.state);
            } else {
                int t = Math.floorDiv(x, 16);
                int u = Math.floorDiv(y, 40);
                int v = Math.floorDiv(z, 16);
                int w = u * 40 + 20;
                double e = this.fluidLevelSpreadNoise.sample(t, u / 1.4, v) * 10.0;
                int ab = (int) (Math.floor(e / 3D) * 3); // C2ME - inline
                int ac = w + ab;
                int ad = Math.min(i, ac);
                BlockState blockState = this.method_38993(x, y, z, fluidLevel, ac);
                return new AquiferSampler.FluidLevel(ad, blockState);
            }
        }
    }

    /**
     * @author ishland
     * @reason optimize
     */
    @Overwrite
    private BlockState method_38993(int i, int j, int k, AquiferSampler.FluidLevel fluidLevel, int l) {
        // TODO [VanillaCopy]
        if (l <= -10) {
            int o = Math.floorDiv(i, 64);
            int p = Math.floorDiv(j, 40);
            int q = Math.floorDiv(k, 64);
            double d = this.fluidTypeNoise.sample(o, p, q);
            if (Math.abs(d) > 0.3) {
                return Blocks.LAVA.getDefaultState();
            }
        }

        return fluidLevel.state;
    }

    /**
     * @author ishland
     * @reason optimize
     */
    @Nullable
    @Overwrite
    public BlockState apply(int x, int y, int z, double d, double e) {
        // TODO [VanillaCopy]
        if (d <= -64.0) {
            return this.fluidLevelSampler.getFluidLevel(x, y, z).getBlockState(y);
        } else {
            if (e <= 0.0) {
                AquiferSampler.FluidLevel fluidLevel = this.fluidLevelSampler.getFluidLevel(x, y, z);
                double f = 0.0;
                BlockState blockState;
                boolean bl;
                if (fluidLevel.getBlockState(y).isOf(Blocks.LAVA)) {
                    blockState = Blocks.LAVA.getDefaultState();
                    bl = false;
                } else {
                    int i = Math.floorDiv(x - 5, 16);
                    int j = Math.floorDiv(y + 1, 12);
                    int k = Math.floorDiv(z - 5, 16);
                    int l = Integer.MAX_VALUE;
                    int m = Integer.MAX_VALUE;
                    int n = Integer.MAX_VALUE;
                    long o = 0L;
                    long p = 0L;
                    long q = 0L;

                    for(int r = 0; r <= 1; ++r) {
                        for(int s = -1; s <= 1; ++s) {
                            for(int t = 0; t <= 1; ++t) {
                                int u = i + r;
                                int v = j + s;
                                int w = k + t;
                                int aa = ((v - this.startY) * this.sizeZ + (w - this.startZ)) * this.sizeX + u - this.startX; // C2ME - inline modified
                                long ab = this.blockPositions[aa];
                                long ac;
                                if (ab != Long.MAX_VALUE) {
                                    ac = ab;
                                } else {
                                    AbstractRandom abstractRandom = this.randomDeriver.createRandom(u, v, w);
                                    long l1 = 0L;
                                    // C2ME - inlined reordered
                                    final int rnd0 = abstractRandom.nextInt(10);
                                    final int rnd1 = abstractRandom.nextInt(9);
                                    final int rnd2 = abstractRandom.nextInt(10);
                                    l1 |= ((u * 16L + rnd0) & BlockPos.BITS_X) << BlockPos.BIT_SHIFT_X;
                                    l1 |= ((v * 12L + rnd1) & BlockPos.BITS_Y);
                                    ac = l1 | ((w * 16L + rnd2) & BlockPos.BITS_Z) << BlockPos.BIT_SHIFT_Z;
                                    this.blockPositions[aa] = ac;
                                }

                                int abstractRandom = (int) (ac << 64 - BlockPos.BIT_SHIFT_X - BlockPos.SIZE_BITS_X >> 64 - BlockPos.SIZE_BITS_X) - x; // C2ME - inline
                                int ad = (int) (ac << 64 - BlockPos.SIZE_BITS_Y >> 64 - BlockPos.SIZE_BITS_Y) - y; // C2ME - inline
                                int ae = (int) (ac << 64 - BlockPos.BIT_SHIFT_Z - BlockPos.SIZE_BITS_Z >> 64 - BlockPos.SIZE_BITS_Z) - z; // C2ME - inline
                                int af = abstractRandom * abstractRandom + ad * ad + ae * ae;
                                if (l >= af) {
                                    q = p;
                                    p = o;
                                    o = ac;
                                    n = m;
                                    m = l;
                                    l = af;
                                } else if (m >= af) {
                                    q = p;
                                    p = ac;
                                    n = m;
                                    m = af;
                                } else if (n >= af) {
                                    q = ac;
                                    n = af;
                                }
                            }
                        }
                    }

                    AquiferSampler.FluidLevel r = this.getWaterLevel(o);
                    AquiferSampler.FluidLevel s = this.getWaterLevel(p);
                    AquiferSampler.FluidLevel t = this.getWaterLevel(q);
                    double u = 1.0 - (double) Math.abs(m - l) / 25.0; // C2ME - inline
                    double w = 1.0 - (double) Math.abs(n - l) / 25.0; // C2ME - inline
                    double ac = 1.0 - (double) Math.abs(n - m) / 25.0; // C2ME - inline
                    bl = u >= field_36221;
                    final BlockState rBlockStateY = r.getBlockState(y);
                    final boolean rBlockStateYIsWater = rBlockStateY.isOf(Blocks.WATER);
                    if (rBlockStateYIsWater && this.fluidLevelSampler.getFluidLevel(x, y - 1, z).getBlockState(y - 1).isOf(Blocks.LAVA)) {
                        f = 1.0;
                    } else if (u > -1.0) {
                        MutableDouble ab = new MutableDouble(Double.NaN);
                        // C2ME - reduce branching & isOf calls
                        final boolean rBlockStateYNotLava = !rBlockStateY.isOf(Blocks.LAVA);
                        final BlockState sBlockStateY = s.getBlockState(y);
                        final boolean sBlockStateYNotWater = !sBlockStateY.isOf(Blocks.WATER);
                        final boolean sBlockStateYNotLava = !sBlockStateY.isOf(Blocks.LAVA);
                        double g = 1.0;
                        if ((rBlockStateYNotLava || sBlockStateYNotWater) && (!rBlockStateYIsWater || sBlockStateYNotLava)) {
                            g = calculateDensitySimplfied(r, s, y, ab, x, z);
                        }
                        double ad = 1.0;
                        final BlockState tBlockStateY = t.getBlockState(y);
                        final boolean tBlockStateYNotWater = !tBlockStateY.isOf(Blocks.WATER);
                        final boolean tBlockStateYNotLava = !tBlockStateY.isOf(Blocks.LAVA);
                        if ((rBlockStateYNotLava || tBlockStateYNotWater) && (!rBlockStateYIsWater || tBlockStateYNotLava)) {
                            ad = calculateDensitySimplfied(r, t, y, ab, x, z);
                        }
                        double af = 1.0;
                        if ((sBlockStateYNotLava || tBlockStateYNotWater) && (sBlockStateYNotWater || tBlockStateYNotLava)) {
                            af = calculateDensitySimplfied(s, t, y, ab, x, z);
                        }
                        double h = Math.max(0.0, u);
                        double ag = Math.max(0.0, w);
                        double ah = Math.max(0.0, ac);
                        double ai = 2.0 * h * Math.max(g, Math.max(ad * ag, af * ah));
                        f = Math.max(0.0, ai);
                    }

                    blockState = rBlockStateY;
                }

                if (e + f <= 0.0) {
                    this.needsFluidTick = bl;
                    return blockState;
                }
            }

            this.needsFluidTick = false;
            return null;
        }
    }

    private double calculateDensitySimplfied(AquiferSampler.FluidLevel r, AquiferSampler.FluidLevel s, int y, MutableDouble ab, int x, int z) {
        double g = 0.0;
        int l3 = Math.abs(r.y - s.y);
        if (l3 != 0) {
            double f3 = l3 / 2.0;
            double d3 = 0.5 * (double)(r.y + s.y);
            double e3 = y + 0.5 - d3;
            double q3 = f3 - Math.abs(e3);
            double s3;
            if (e3 > 0.0) {
                double r3 = 0.0 + q3;
                if (r3 > 0.0) {
                    s3 = r3 / 1.5;
                } else {
                    s3 = r3 / 2.5;
                }
            } else {
                double r3 = 3.0 + q3;
                if (r3 > 0.0) {
                    s3 = r3 / 3.0;
                } else {
                    s3 = r3 / 10.0;
                }
            }

            if (!(s3 < -2.0) && !(s3 > 2.0)) {
                double r3 = ab.getValue();
                if (Double.isNaN(r3)) {
                    double u3 = this.barrierNoise.sample(x, y * 0.5, z);
                    ab.setValue(u3);
                    g = u3 + s3;
                } else {
                    g = r3 + s3;
                }
            } else {
                g = s3;
            }
        }
        return g;
    }

    private static double clampedLerpFromProgressInlined(double lerpValue) {
        final double delta = lerpValue / 64.0;
        if (delta < 0.0) {
            return 1.0;
        } else {
            return (delta > 1.0) ? 0.0 : (1.0 - delta);
        }
    }

    private static double lerpFromProgress(double lerpValue, double lerpStart, double lerpEnd, double start, double end) {
        final double var0 = lerpValue - lerpStart;
        final double var1 = lerpEnd - lerpStart;
        final double delta = var0 / var1;
        return start + delta * (end - start);
    }

}
