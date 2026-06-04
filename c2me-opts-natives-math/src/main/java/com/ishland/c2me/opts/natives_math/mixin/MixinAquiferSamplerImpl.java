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

package com.ishland.c2me.opts.natives_math.mixin;

import com.ishland.c2me.opts.natives_math.common.Bindings;
import net.minecraft.world.gen.chunk.AquiferSampler;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SegmentAllocator;
import java.lang.foreign.ValueLayout;

@Mixin(value = AquiferSampler.Impl.class, priority = 1101)
public class MixinAquiferSamplerImpl {

    @Dynamic
    @Shadow
    private int c2me$packed1;
    @Dynamic
    @Shadow
    private int c2me$packed2;
    @Dynamic
    @Shadow
    private int c2me$packed3;
    @Dynamic
    @Shadow
    private int c2me$packed4;

    @Dynamic
    @Shadow
    private short[] c2me$packedBlockPositions;

    @Shadow @Final private int startX;
    @Shadow @Final private int startY;
    @Shadow @Final private int startZ;
    @Shadow @Final private int sizeX;
    @Shadow @Final private int sizeZ;

    @Unique
    private MemorySegment c2me$dataSegment;
    @Unique
    private MemorySegment c2me$aquiferData;
    @Unique
    private long c2me$aquiferDataAddress;
    @Unique
    private MemorySegment c2me$packedArraySegment;
    @Unique
    private long c2me$packedArraySegmentAddress;
    @Unique
    private MemorySegment c2me$packedBlockPositionsSegment;
    @Unique
    private long c2me$packedBlockPositionsSegmentAddress;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        int length = this.c2me$packedBlockPositions.length;
        c2me$dataSegment = Arena.ofAuto().allocate((5 * 4) + (3 * 4) + (length * 2L), 64);
        SegmentAllocator allocator = SegmentAllocator.slicingAllocator(c2me$dataSegment);

        c2me$aquiferData = allocator.allocate(5 * 4);
        c2me$aquiferDataAddress = c2me$aquiferData.address();
        MemorySegment.copy(new int[] {startX, startY, startZ, sizeX, sizeZ}, 0, c2me$aquiferData, ValueLayout.JAVA_INT, 0, 5);

        c2me$packedArraySegment = allocator.allocate(4 * 4);
        c2me$packedArraySegmentAddress = c2me$packedArraySegment.address();

        c2me$packedBlockPositionsSegment = allocator.allocate(length * 2L);
        c2me$packedBlockPositionsSegmentAddress = c2me$packedBlockPositionsSegment.address();
        MemorySegment.copy(c2me$packedBlockPositions, 0, c2me$packedBlockPositionsSegment, ValueLayout.JAVA_SHORT, 0, length);
    }

    /**
     * @author ishland
     * @reason nativeaccel
     */
    @Dynamic
    @Overwrite
    private void aquiferExtracted$refreshDistPosIdx(int x, int y, int z) {
        Bindings.c2me_natives_aquifer_refreshDistPosIdx(this.c2me$packedBlockPositionsSegmentAddress, this.c2me$packedArraySegmentAddress, this.c2me$aquiferDataAddress, x, y, z);
        this.c2me$packed1 = c2me$packedArraySegment.get(ValueLayout.JAVA_INT, 0);
        this.c2me$packed2 = c2me$packedArraySegment.get(ValueLayout.JAVA_INT, 4);
        this.c2me$packed3 = c2me$packedArraySegment.get(ValueLayout.JAVA_INT, 8);
        this.c2me$packed4 = c2me$packedArraySegment.get(ValueLayout.JAVA_INT, 12);
    }

}
