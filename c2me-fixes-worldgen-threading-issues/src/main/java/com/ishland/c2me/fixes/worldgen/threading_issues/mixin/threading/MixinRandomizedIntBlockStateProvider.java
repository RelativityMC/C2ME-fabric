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

package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import net.minecraft.block.BlockState;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.gen.stateprovider.BlockStateProvider;
import net.minecraft.world.gen.stateprovider.RandomizedIntBlockStateProvider;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RandomizedIntBlockStateProvider.class)
public abstract class MixinRandomizedIntBlockStateProvider {

    @Shadow @Nullable private IntProperty property;

    @Shadow @Final private BlockStateProvider source;

    @Shadow
    @Nullable
    private static IntProperty getIntPropertyByName(BlockState state, String propertyName) {
        throw new AbstractMethodError();
    }

    @Shadow @Final private String propertyName;

    @Shadow @Final private IntProvider values;

    /**
     * @author ishland
     * @reason ensure proper behavior
     */
    @Overwrite
    public BlockState get(final StructureWorldAccess level, final Random random, final BlockPos pos) {
        BlockState unmodifiedState = this.source.get(level, random, pos);
        IntProperty propertyLocal = this.property;
        if (propertyLocal == null || !unmodifiedState.contains(propertyLocal)) {
            IntProperty property = getIntPropertyByName(unmodifiedState, this.propertyName);
            if (property == null) {
                return unmodifiedState;
            }

            propertyLocal = this.property = property;
        }

        return unmodifiedState.with(propertyLocal, this.values.get(random));
    }

}
