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

package com.ishland.c2me.opts.math.common;

import com.ishland.flowsched.util.Assertions;

public class CachedArrays {

    public static final ThreadLocal<CachedArrays> INSTANCE = ThreadLocal.withInitial(CachedArrays::new);

    public static final int TILE_MAX_Y = 64;
    public static final int TILE_MAX_Z = 32;

    // arrays are watermarked
    public int sizeY, sizeZ;

    // py | pz
    public final byte[] p = new byte[TILE_MAX_Y + TILE_MAX_Z];

    // relY | fadeY | relZ | fadeZ
    public final float[] floats = new float[TILE_MAX_Y + TILE_MAX_Z + TILE_MAX_Y + TILE_MAX_Z];

    public void configureYTile(int sizeY) {
        Assertions.assertTrue(sizeY <= TILE_MAX_Y);
        this.sizeY = sizeY;
    }

    public void configureZTile(int sizeZ) {
        Assertions.assertTrue(sizeZ <= TILE_MAX_Z);
        this.sizeZ = sizeZ;
    }

    private void assertY(int idx) {
        if (idx >= sizeY) throw new AssertionError();
    }

    private void assertZ(int idx) {
        if (idx >= sizeZ) throw new AssertionError();
    }

    public int yp(int idx) {
        assertY(idx);
        return this.p[idx] & 0xff;
    }

    public void yp(int idx, byte val) {
        assertY(idx);
        this.p[idx] = val;
    }

    public int zp(int idx) {
        assertZ(idx);
        return this.p[sizeY + idx] & 0xff;
    }

    public void zp(int idx, byte val) {
        assertZ(idx);
        this.p[sizeY + idx] = val;
    }

    public float relY(int idx) {
        assertY(idx);
        return this.floats[idx];
    }

    public void relY(int idx, float val) {
        assertY(idx);
        this.floats[idx] = val;
    }

    public float fadeY(int idx) {
        assertY(idx);
        return this.floats[this.sizeY + idx];
    }

    public void fadeY(int idx, float val) {
        assertY(idx);
        this.floats[this.sizeY + idx] = val;
    }

    public float relZ(int idx) {
        assertZ(idx);
        return this.floats[(this.sizeY << 1) + idx];
    }

    public void relZ(int idx, float val) {
        assertZ(idx);
        this.floats[(this.sizeY << 1) + idx] = val;
    }

    public float fadeZ(int idx) {
        assertZ(idx);
        return this.floats[(this.sizeY << 1) + this.sizeZ + idx];
    }

    public void fadeZ(int idx, float val) {
        assertZ(idx);
        this.floats[(this.sizeY << 1) + this.sizeZ + idx] = val;
    }

}
