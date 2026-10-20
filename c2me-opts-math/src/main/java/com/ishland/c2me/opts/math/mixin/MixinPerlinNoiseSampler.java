/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021-2026 ishland
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.opts.math.mixin;

import com.ishland.c2me.base.common.util.NoisePacking;
import com.ishland.c2me.base.mixin.access.ISampleBuffer;
import com.ishland.c2me.opts.math.common.CachedArrays;
import com.ishland.c2me.opts.math.common.ducks.PerlinNoiseSamplerExtension;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.noise.LatticedNoiseSampler;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.noise.SamplingRegion;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.gen.sampler.SampleBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

import static com.ishland.c2me.base.common.util.NoisePacking.FLAT_SIMPLEX_GRAD_F32;

@Mixin(value = PerlinNoiseSampler.class, priority = 1090)
public abstract class MixinPerlinNoiseSampler extends LatticedNoiseSampler implements PerlinNoiseSamplerExtension {

    protected MixinPerlinNoiseSampler(Random random) {
        super(random);
    }

    @Unique
    private short[] c2me$packedPermutationsForJava;

    @Unique
    @Override
    public short[] c2me$initPackedPermutationsForJava() {
        short[] packedPermutations = this.c2me$packedPermutationsForJava;
        if (packedPermutations == null) {
            this.c2me$packedPermutationsForJava = packedPermutations = NoisePacking.packPermutation512bShort(this.permutation);
        }
        return packedPermutations;
    }

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        this.c2me$initPackedPermutationsForJava();
    }

//    protected float c2me$sampleVanillaSingle(
//            final int cellX, final int cellY, final int cellZ, final float cellLocalX, final float cellLocalY, final float cellLocalZ, final float originalCellLocalY
//    ) {
//        int x0 = this.permuteIndex(cellX);
//        int x1 = this.permuteIndex(cellX + 1);
//        int xy00 = this.permuteIndex(x0 + cellY);
//        int xy01 = this.permuteIndex(x0 + cellY + 1);
//        int xy10 = this.permuteIndex(x1 + cellY);
//        int xy11 = this.permuteIndex(x1 + cellY + 1);
//        float d000 = getLatticePointContribution(this.permuteIndex(xy00 + cellZ), cellLocalX, cellLocalY, cellLocalZ);
//        float d100 = getLatticePointContribution(this.permuteIndex(xy10 + cellZ), cellLocalX - 1.0F, cellLocalY, cellLocalZ);
//        float d010 = getLatticePointContribution(this.permuteIndex(xy01 + cellZ), cellLocalX, cellLocalY - 1.0F, cellLocalZ);
//        float d110 = getLatticePointContribution(this.permuteIndex(xy11 + cellZ), cellLocalX - 1.0F, cellLocalY - 1.0F, cellLocalZ);
//        float d001 = getLatticePointContribution(this.permuteIndex(xy00 + cellZ + 1), cellLocalX, cellLocalY, cellLocalZ - 1.0F);
//        float d101 = getLatticePointContribution(this.permuteIndex(xy10 + cellZ + 1), cellLocalX - 1.0F, cellLocalY, cellLocalZ - 1.0F);
//        float d011 = getLatticePointContribution(this.permuteIndex(xy01 + cellZ + 1), cellLocalX, cellLocalY - 1.0F, cellLocalZ - 1.0F);
//        float d111 = getLatticePointContribution(this.permuteIndex(xy11 + cellZ + 1), cellLocalX - 1.0F, cellLocalY - 1.0F, cellLocalZ - 1.0F);
//        float xAlpha = MathHelper.perlinFade(cellLocalX);
//        float yAlpha = MathHelper.perlinFade(originalCellLocalY);
//        float zAlpha = MathHelper.perlinFade(cellLocalZ);
//        return MathHelper.lerp3(xAlpha, yAlpha, zAlpha, d000, d100, d010, d110, d001, d101, d011, d111);
//    }

    /**
     * @author ishland
     * @reason optimize
     */
    @Overwrite
    public float sample(final double x, final double y, final double z) {
        double xx = wrapCoord(x) + this.originX;
        double yx = wrapCoord(y) + this.originY;
        double zx = wrapCoord(z) + this.originZ;
        double floorX = Math.floor(xx);
        double floorY = Math.floor(yx);
        double floorZ = Math.floor(zx);
        float relativeX = (float) (xx - floorX);
        float relativeY = (float) (yx - floorY);
        float relativeZ = (float) (zx - floorZ);
        return this.sample((int) floorX, (int) floorY, (int) floorZ, relativeX, relativeY, relativeZ, relativeY);
    }

    /**
     * @author ishland
     * @reason optimize
     */
    @Overwrite
    protected float sample(
            final int px0, final int py0, final int pz0, final float fx0, final float fy0, final float fz0, final float fadeLocalY
    ) {
        // using 256b perm is more stable in memory bound cases

        final byte[] permutations = this.permutation;

        if (permutations.length != 256) throw new AssertionError("permutations.length != 256");

        final int hash0__ = permutations[px0 & 0xff];
        final int hash1__ = permutations[(px0 + 1) & 0xff];
        final int hash00_ = permutations[(hash0__ + py0) & 0xff];
        final int hash01_ = permutations[(hash0__ + py0 + 1) & 0xff];
        final int hash10_ = permutations[(hash1__ + py0) & 0xff];
        final int hash11_ = permutations[(hash1__ + py0 + 1) & 0xff];
        final float fx1 = fx0 - 1.0f;
        final float fy1 = fy0 - 1.0f;
        final float fz1 = fz0 - 1.0f;
        final int hash000 = permutations[(hash00_ + pz0) & 0xff] & 0xf;
        final float f000 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash000 << 2) + 0] * fx0 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash000 << 2) + 1] * fy0 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash000 << 2) + 2] * fz0;
        final int hash001 = permutations[(hash00_ + pz0 + 1) & 0xff] & 0xf;
        final float f001 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash001 << 2) + 0] * fx0 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash001 << 2) + 1] * fy0 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash001 << 2) + 2] * fz1;
        final int hash010 = permutations[(hash01_ + pz0) & 0xff] & 0xf;
        final float f010 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash010 << 2) + 0] * fx0 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash010 << 2) + 1] * fy1 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash010 << 2) + 2] * fz0;
        final int hash011 = permutations[(hash01_ + pz0 + 1) & 0xff] & 0xf;
        final float f011 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash011 << 2) + 0] * fx0 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash011 << 2) + 1] * fy1 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash011 << 2) + 2] * fz1;
        final int hash100 = permutations[(hash10_ + pz0) & 0xff] & 0xf;
        final float f100 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash100 << 2) + 0] * fx1 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash100 << 2) + 1] * fy0 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash100 << 2) + 2] * fz0;
        final int hash101 = permutations[(hash10_ + pz0 + 1) & 0xff] & 0xf;
        final float f101 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash101 << 2) + 0] * fx1 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash101 << 2) + 1] * fy0 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash101 << 2) + 2] * fz1;
        final int hash110 = permutations[(hash11_ + pz0) & 0xff] & 0xf;
        final float f110 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash110 << 2) + 0] * fx1 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash110 << 2) + 1] * fy1 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash110 << 2) + 2] * fz0;
        final int hash111 = permutations[(hash11_ + pz0 + 1) & 0xff] & 0xf;
        final float f111 = NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash111 << 2) + 0] * fx1 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash111 << 2) + 1] * fy1 + NoisePacking.FLAT_SIMPLEX_GRAD_F32[(hash111 << 2) + 2] * fz1;

        final float dx = MathHelper.perlinFade(fx0);
        final float dy = MathHelper.perlinFade(fadeLocalY);
        final float dz = MathHelper.perlinFade(fz0);

        float lerpx00 = MathHelper.lerp(dx, f000, f100);
        float lerpx10 = MathHelper.lerp(dx, f010, f110);
        float lerpx01 = MathHelper.lerp(dx, f001, f101);
        float lerpx11 = MathHelper.lerp(dx, f011, f111);
        float lerpxx0 = MathHelper.lerp(dy, lerpx00, lerpx10);
        float lerpxx1 = MathHelper.lerp(dy, lerpx01, lerpx11);
        float lerpxxx = MathHelper.lerp(dz, lerpxx0, lerpxx1);

//        if (lerpxxx != this.c2me$sampleVanillaSingle(px0, py0, pz0, fx0, fy0, fz0, fadeLocalY)) {
//            throw new AssertionError();
//        }

        return lerpxxx;
    }

//    public void c2me$fillVanilla(final SampleBuffer buf, final SamplingRegion region, final double scaleXz, final double scaleY, final float outputScale) {
//        float d000xz = 0.0F;
//        float d100xz = 0.0F;
//        float d010xz = 0.0F;
//        float d110xz = 0.0F;
//        float d001xz = 0.0F;
//        float d101xz = 0.0F;
//        float d011xz = 0.0F;
//        float d111xz = 0.0F;
//        float g000y = 0.0F;
//        float g100y = 0.0F;
//        float g010y = 0.0F;
//        float g110y = 0.0F;
//        float g001y = 0.0F;
//        float g101y = 0.0F;
//        float g011y = 0.0F;
//        float g111y = 0.0F;
//        int index = 0;
//
//        for (int indexZ = 0; indexZ < region.sizeZ(); indexZ++) {
//            double z = wrapCoord(region.translateZ(indexZ) * scaleXz) + this.originZ;
//            int floorZ = MathHelper.floor(z);
//            float relativeZ = (float)(z - floorZ);
//            float alphaZ = MathHelper.perlinFade(relativeZ);
//
//            for (int indexX = 0; indexX < region.sizeX(); indexX++) {
//                double x = wrapCoord(region.translateX(indexX) * scaleXz) + this.originX;
//                int floorX = MathHelper.floor(x);
//                float relativeX = (float)(x - floorX);
//                int x0 = this.permuteIndex(floorX);
//                int x1 = this.permuteIndex(floorX + 1);
//                float alphaX = MathHelper.perlinFade(relativeX);
//                int lastFloorY = Integer.MIN_VALUE;
//
//                for (int indexY = 0; indexY < region.sizeY(); indexY++) {
//                    double y = wrapCoord(region.translateY(indexY) * scaleY) + this.originY;
//                    int floorY = MathHelper.floor(y);
//                    float relativeY = (float)(y - floorY);
//                    float alphaY = MathHelper.perlinFade(relativeY);
//                    if (lastFloorY != floorY) {
//                        int xy00 = this.permuteIndex(x0 + floorY);
//                        int xy01 = this.permuteIndex(x0 + floorY + 1);
//                        int xy10 = this.permuteIndex(x1 + floorY);
//                        int xy11 = this.permuteIndex(x1 + floorY + 1);
//                        LatticedNoiseSampler.LatticePointDirection g000 = this.getLatticePointDirection(xy00 + floorZ);
//                        d000xz = g000.dotXz(relativeX, relativeZ);
//                        g000y = g000.y();
//                        LatticedNoiseSampler.LatticePointDirection g100 = this.getLatticePointDirection(xy10 + floorZ);
//                        d100xz = g100.dotXz(relativeX - 1.0F, relativeZ);
//                        g100y = g100.y();
//                        LatticedNoiseSampler.LatticePointDirection g010 = this.getLatticePointDirection(xy01 + floorZ);
//                        d010xz = g010.dotXz(relativeX, relativeZ);
//                        g010y = g010.y();
//                        LatticedNoiseSampler.LatticePointDirection g110 = this.getLatticePointDirection(xy11 + floorZ);
//                        d110xz = g110.dotXz(relativeX - 1.0F, relativeZ);
//                        g110y = g110.y();
//                        LatticedNoiseSampler.LatticePointDirection g001 = this.getLatticePointDirection(xy00 + floorZ + 1);
//                        d001xz = g001.dotXz(relativeX, relativeZ - 1.0F);
//                        g001y = g001.y();
//                        LatticedNoiseSampler.LatticePointDirection g101 = this.getLatticePointDirection(xy10 + floorZ + 1);
//                        d101xz = g101.dotXz(relativeX - 1.0F, relativeZ - 1.0F);
//                        g101y = g101.y();
//                        LatticedNoiseSampler.LatticePointDirection g011 = this.getLatticePointDirection(xy01 + floorZ + 1);
//                        d011xz = g011.dotXz(relativeX, relativeZ - 1.0F);
//                        g011y = g011.y();
//                        LatticedNoiseSampler.LatticePointDirection g111 = this.getLatticePointDirection(xy11 + floorZ + 1);
//                        d111xz = g111.dotXz(relativeX - 1.0F, relativeZ - 1.0F);
//                        g111y = g111.y();
//                        lastFloorY = floorY;
//                    }
//
//                    buf.add(
//                            index,
//                            outputScale
//                                    * MathHelper.lerp3(
//                                    alphaX,
//                                    alphaY,
//                                    alphaZ,
//                                    d000xz + g000y * relativeY,
//                                    d100xz + g100y * relativeY,
//                                    d010xz + g010y * (relativeY - 1.0F),
//                                    d110xz + g110y * (relativeY - 1.0F),
//                                    d001xz + g001y * relativeY,
//                                    d101xz + g101y * relativeY,
//                                    d011xz + g011y * (relativeY - 1.0F),
//                                    d111xz + g111y * (relativeY - 1.0F)
//                            )
//                    );
//                    index++;
//                }
//            }
//        }
//    }

    /**
     * @author ishland
     * @reason optimize
     */
    @Overwrite
    public void fill(final SampleBuffer buf, final SamplingRegion region, final double scaleXz, final double scaleY, final float outputScale) {
        final short[] permutations = this.c2me$initPackedPermutationsForJava();

//        SampleBuffer vanillaBuffer = SampleBuffer.withCount(buf.count());
//        vanillaBuffer.copyFrom(buf);
//        this.c2me$fillVanilla(vanillaBuffer, region, scaleXz, scaleY, outputScale);

        CachedArrays cachedArrays = CachedArrays.INSTANCE.get();

        final int minBlockX = region.minBlockX();
        final int minBlockY = region.minBlockY();
        final int minBlockZ = region.minBlockZ();
        final int stepBlockX = region.stepBlockX();
        final int stepBlockY = region.stepBlockY();
        final int stepBlockZ = region.stepBlockZ();
        final int sizeX = region.sizeX();
        final int sizeY = region.sizeY();
        final int sizeZ = region.sizeZ();

        if (permutations.length != 256) throw new AssertionError();

        for (int yBase = 0; yBase < sizeY; yBase += CachedArrays.TILE_MAX_Y) {
            final int yCount = Math.min(CachedArrays.TILE_MAX_Y, sizeY - yBase);
            cachedArrays.configureYTile(yCount);

            for (int offY = 0; offY < yCount; offY ++) {
//            const int32_t block =
//                        region.minBlockY + (int32_t) (yBase + j) * (int32_t) region.stepBlockY;
//            const double y = (double) block * scaleY;
//            const double yy = math_octave_maintainPrecision(y) + originY;
//            const double fl = floor(yy), rel = yy - fl;
//            const double fy =
//                        legacy
//                                ? floor(((y >= 0.0 && y < rel) ? y : rel) / yScale + 1.0E-7) * yScale
//                                : 0.0;
//                py[j] = (uint32_t) (int32_t) fl & 255u;
//                yf0[j] = (float) (rel - fy);
//                yd[j] = math_perlinFade((float) rel);
                final int blockY = minBlockY + stepBlockY * (yBase + offY);
                final double y = (double) blockY * scaleY;
                final double yy = wrapCoord(y) + originY;
                final double fl = Math.floor(yy), rel = yy - fl;
                final double fy = 0.0;
//                final double fy = Math.floor(((y >= 0.0 && y < rel) ? y : rel) / offsetScale + 1.0E-7) * offsetScale;;
                cachedArrays.yp(offY, (byte) ((int) fl & 0xff));
                cachedArrays.relY(offY, (float) (rel - fy));
                cachedArrays.fadeY(offY, MathHelper.perlinFade((float) rel));
            }

            for (int zBase = 0; zBase < sizeZ; zBase += CachedArrays.TILE_MAX_Z) {
                final int zCount = Math.min(CachedArrays.TILE_MAX_Z, sizeZ - zBase);
                cachedArrays.configureZTile(zCount);

                for (int offZ = 0; offZ < zCount; offZ ++) {
//                const int32_t block = region.minBlockZ +
//                            (int32_t) (zBase + j) * (int32_t) region.stepBlockZ;
//                const double zz =
//                            math_octave_maintainPrecision((double) block * scaleXz) + originZ;
//                const double fl = floor(zz);
//                    zp[j] = (uint32_t) (int32_t) fl;
//                    zf0[j] = (float) (zz - fl);
//                    zf1[j] = zf0[j] - 1.0f;
//                    zd[j] = math_perlinFade(zf0[j]);
                    final int blockZ = minBlockZ + stepBlockZ * (zBase + offZ);
                    final double z = (double) blockZ * scaleXz;
                    final double zz = wrapCoord(z) + originZ;
                    final double fl = Math.floor(zz), rel = zz - fl;
                    cachedArrays.zp(offZ, (byte) ((int) fl & 0xff));
                    cachedArrays.relZ(offZ, (float) rel);
                    cachedArrays.fadeZ(offZ, MathHelper.perlinFade((float) rel));
                }

                int px0_prev = Integer.MAX_VALUE;
                int px0_perm = 0;
                int px1_perm = 0;

                int py0_prev = Integer.MAX_VALUE;
                int px0_py0_perm = 0;
                int px1_py0_perm = 0;
                int px0_py1_perm = 0;
                int px1_py1_perm = 0;

                int pz0_prev = Integer.MAX_VALUE;
                float a000_2 = 0f;
                float a100_2 = 0f;
                float a010_2 = 0f;
                float a110_2 = 0f;
                float a001_2 = 0f;
                float a101_2 = 0f;
                float a011_2 = 0f;
                float a111_2 = 0f;

                // note: ordering actually doesn't matter because there's only two non-zero values
                // also FMA is possible since multiplied are *exact* but not implemented here
                float xy000 = 0f, xy100 = 0f, xy010 = 0f, xy110 = 0f;
                float xy001 = 0f, xy101 = 0f, xy011 = 0f, xy111 = 0f;

                for (int offX = 0; offX < sizeX; offX++) {
                    final int blockX = minBlockX + offX * stepBlockX;
                    final double x = (double) blockX * scaleXz;
                    final double x1 = wrapCoord(x) + originX;
                    final double floorX = Math.floor(x1);
                    final double relX = x1 - floorX;
                    final float fx0 = (float) relX;
                    final int px0 = (int) floorX & 0xff;
                    final float fx1 = fx0 - 1.0f;
                    final float dx = MathHelper.perlinFade(fx0);

                    if (px0_prev != px0) {
                        final int pxr = permutations[px0 & 0xff];
                        px0_perm = pxr;
                        px1_perm = (pxr >>> 8);
                        px0_prev = px0;
                        py0_prev = Integer.MAX_VALUE;
                        pz0_prev = Integer.MAX_VALUE;
                    }

                    for (int offY = 0; offY < yCount; offY++) {
//                        final int blockY = region.minBlockY() + offY * region.stepBlockY();
//                        final double y = (double) blockY * scaleY;
//                        final double y1 = wrapCoord(y) + originY;
//                        final double floorY = Math.floor(y1);
//                        final double relY = y1 - floorY;
//                        final int py0 = (int) floorY;
//                        final float fy0 = (float) relY;
//                        final float fadeLocalY = (float) relY;
//                        final float fy1 = fy0 - 1.0f;
//                        final float dy = MathHelper.perlinFade(fadeLocalY);
                        final int py0 = cachedArrays.yp(offY);
                        final float fy0 = cachedArrays.relY(offY);
                        final float fy1 = fy0 - 1.0f;
                        final float dy = cachedArrays.fadeY(offY);

                        pz0_prev = Integer.MAX_VALUE; // always refresh

                        if (py0_prev != py0) {
                            final int px0r = permutations[(px0_perm + py0) & 0xff];
                            final int px1r = permutations[(px1_perm + py0) & 0xff];
                            px0_py0_perm = px0r;
                            px1_py0_perm = px1r;
                            px0_py1_perm = (px0r >>> 8);
                            px1_py1_perm = (px1r >>> 8);
                            py0_prev = py0;
                            pz0_prev = Integer.MAX_VALUE;
                        }

                        final int zStride = sizeX * sizeY;
                        int idx = (yBase + offY) + offX * sizeY + (zBase) * zStride;

                        for (int offZ = 0; offZ < zCount; offZ++) {
//                            final int blockZ = region.minBlockZ() + offZ * region.stepBlockZ();
//                            final double z = (double) blockZ * scaleXz;
//                            final double z1 = wrapCoord(z) + originZ;
//                            final double floorZ = Math.floor(z1);
//                            final double relZ = z1 - floorZ;
//                            final int pz0 = (int) floorZ;
//                            final float fz0 = (float) relZ;
//                            final float fz1 = fz0 - 1.0f;
//                            final float dz = MathHelper.perlinFade(fz0);
                            final int pz0 = cachedArrays.zp(offZ);
                            final float fz0 = cachedArrays.relZ(offZ);
                            final float fz1 = fz0 - 1.0f;
                            final float dz = cachedArrays.fadeZ(offZ);

                            if (pz0_prev != pz0) {
                                final int px00r = permutations[(px0_py0_perm + pz0) & 0xff];
                                final int px10r = permutations[(px1_py0_perm + pz0) & 0xff];
                                final int px01r = permutations[(px0_py1_perm + pz0) & 0xff];
                                final int px11r = permutations[(px1_py1_perm + pz0) & 0xff];

                                if (pz0_prev + 1 == pz0) {
                                    a000_2 = a001_2;
                                    a100_2 = a101_2;
                                    a010_2 = a011_2;
                                    a110_2 = a111_2;

                                    xy000 = xy001;
                                    xy100 = xy101;
                                    xy010 = xy011;
                                    xy110 = xy111;
                                } else {
                                    final int a000 = (px00r & 0xF) << 2;
                                    final float a000_0 = FLAT_SIMPLEX_GRAD_F32[a000];
                                    final float a000_1 = FLAT_SIMPLEX_GRAD_F32[a000 + 1];
                                    a000_2 = FLAT_SIMPLEX_GRAD_F32[a000 + 2];

                                    final int a100 = (px10r & 0xF) << 2;
                                    final float a100_0 = FLAT_SIMPLEX_GRAD_F32[a100];
                                    final float a100_1 = FLAT_SIMPLEX_GRAD_F32[a100 + 1];
                                    a100_2 = FLAT_SIMPLEX_GRAD_F32[a100 + 2];

                                    final int a010 = (px01r & 0xF) << 2;
                                    final float a010_0 = FLAT_SIMPLEX_GRAD_F32[a010];
                                    final float a010_1 = FLAT_SIMPLEX_GRAD_F32[a010 + 1];
                                    a010_2 = FLAT_SIMPLEX_GRAD_F32[a010 + 2];

                                    final int a110 = (px11r & 0xF) << 2;
                                    final float a110_0 = FLAT_SIMPLEX_GRAD_F32[a110];
                                    final float a110_1 = FLAT_SIMPLEX_GRAD_F32[a110 + 1];
                                    a110_2 = FLAT_SIMPLEX_GRAD_F32[a110 + 2];

                                    xy000 = a000_0 * fx0 + a000_1 * fy0;
                                    xy100 = a100_0 * fx1 + a100_1 * fy0;
                                    xy010 = a010_0 * fx0 + a010_1 * fy1;
                                    xy110 = a110_0 * fx1 + a110_1 * fy1;
                                }

                                final int a001 = ((px00r >>> 8) & 0xF) << 2;
                                final float a001_0 = FLAT_SIMPLEX_GRAD_F32[a001];
                                final float a001_1 = FLAT_SIMPLEX_GRAD_F32[a001 + 1];
                                a001_2 = FLAT_SIMPLEX_GRAD_F32[a001 + 2];

                                final int a101 = ((px10r >>> 8) & 0xF) << 2;
                                final float a101_0 = FLAT_SIMPLEX_GRAD_F32[a101];
                                final float a101_1 = FLAT_SIMPLEX_GRAD_F32[a101 + 1];
                                a101_2 = FLAT_SIMPLEX_GRAD_F32[a101 + 2];

                                final int a011 = ((px01r >>> 8) & 0xF) << 2;
                                final float a011_0 = FLAT_SIMPLEX_GRAD_F32[a011];
                                final float a011_1 = FLAT_SIMPLEX_GRAD_F32[a011 + 1];
                                a011_2 = FLAT_SIMPLEX_GRAD_F32[a011 + 2];

                                final int a111 = ((px11r >>> 8) & 0xF) << 2;
                                final float a111_0 = FLAT_SIMPLEX_GRAD_F32[a111];
                                final float a111_1 = FLAT_SIMPLEX_GRAD_F32[a111 + 1];
                                a111_2 = FLAT_SIMPLEX_GRAD_F32[a111 + 2];

                                xy001 = a001_0 * fx0 + a001_1 * fy0;
                                xy101 = a101_0 * fx1 + a101_1 * fy0;
                                xy011 = a011_0 * fx0 + a011_1 * fy1;
                                xy111 = a111_0 * fx1 + a111_1 * fy1;

                                pz0_prev = pz0;
                            }

                            final float f000 = xy000 + a000_2 * fz0;
                            final float f100 = xy100 + a100_2 * fz0;
                            final float f010 = xy010 + a010_2 * fz0;
                            final float f110 = xy110 + a110_2 * fz0;
                            final float f001 = xy001 + a001_2 * fz1;
                            final float f101 = xy101 + a101_2 * fz1;
                            final float f011 = xy011 + a011_2 * fz1;
                            final float f111 = xy111 + a111_2 * fz1;

                            float lerpx00 = MathHelper.lerp(dx, f000, f100);
                            float lerpx10 = MathHelper.lerp(dx, f010, f110);
                            float lerpx01 = MathHelper.lerp(dx, f001, f101);
                            float lerpx11 = MathHelper.lerp(dx, f011, f111);
                            float lerpxx0 = MathHelper.lerp(dy, lerpx00, lerpx10);
                            float lerpxx1 = MathHelper.lerp(dy, lerpx01, lerpx11);
                            float lerpxxx = MathHelper.lerp(dz, lerpxx0, lerpxx1);

                            buf.add(
                                    idx,
                                    lerpxxx * outputScale
                            );

                            idx += zStride;
                        }
                    }
                }

            }
        }

//        if (!Arrays.equals(
//                ((ISampleBuffer) vanillaBuffer).getElems(), 0, buf.count(),
//                ((ISampleBuffer) buf).getElems(), 0, buf.count()
//        )) {
//            throw new AssertionError();
//        }
    }

}
