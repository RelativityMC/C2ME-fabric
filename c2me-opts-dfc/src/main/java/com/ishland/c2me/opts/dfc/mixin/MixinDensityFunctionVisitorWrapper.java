/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * This file is a part of Concurrent Chunk Management Engine,
 * a mod designed to improve the chunk performance of Minecraft.
 *
 * Copyright (c) 2021-2026 ishland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ishland.c2me.opts.dfc.mixin;

import com.ishland.c2me.opts.dfc.common.ducks.IBlendingAwareVisitor;
import com.ishland.c2me.opts.dfc.common.ducks.ICompiledCachingAwareVisitor;
import com.ishland.c2me.opts.dfc.common.gen.jvm.CompiledEntry;
import com.ishland.c2me.opts.dfc.common.gen.jvm.internalapi.ArgumentVisitor;
import net.minecraft.world.gen.densityfunction.DensityFunction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "net/minecraft/world/gen/densityfunction/DensityFunction$Wrapper")
public class MixinDensityFunctionVisitorWrapper implements IBlendingAwareVisitor, ICompiledCachingAwareVisitor {

    @Shadow
    private DensityFunction.DensityFunctionVisitor field_1_6131;

    @Override
    public boolean c2me$isBlendingEnabled() {
        if (this.field_1_6131 instanceof IBlendingAwareVisitor blendingAwareVisitor) {
            return blendingAwareVisitor.c2me$isBlendingEnabled();
        }

        return false;
    }

    @Override
    public CompiledEntry c2me$visitIfAbsent(CompiledEntry entry, ArgumentVisitor visitor) {
        if (this.field_1_6131 instanceof ICompiledCachingAwareVisitor cachingAwareVisitor) {
            return cachingAwareVisitor.c2me$visitIfAbsent(entry, visitor);
        }

        return entry.newInstance(entry.getArgs(), visitor);
    }
}
