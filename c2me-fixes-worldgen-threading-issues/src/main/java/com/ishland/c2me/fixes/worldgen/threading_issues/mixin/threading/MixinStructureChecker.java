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

package com.ishland.c2me.fixes.worldgen.threading_issues.mixin.threading;

import it.unimi.dsi.fastutil.longs.Long2BooleanMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.world.StructureLocator;
import net.minecraft.world.gen.structure.Structure;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;

@Mixin(StructureLocator.class)
public class MixinStructureChecker {

    @Mutable
    @Shadow @Final private Long2ObjectMap<Object2IntMap<Structure>> cachedStructuresByChunkPos;

    @Mutable
    @Shadow @Final private Map<Structure, Long2BooleanMap> generationPossibilityByStructure;

    @Unique
    private Object mapMutex = new Object();

    @Inject(method = "<init>", at = @At(value = "RETURN"))
    private void onInit(CallbackInfo info) {
        this.mapMutex = new Object();
        this.cachedStructuresByChunkPos = Long2ObjectMaps.synchronize(this.cachedStructuresByChunkPos);
        this.generationPossibilityByStructure = Object2ObjectMaps.synchronize(new Object2ObjectOpenHashMap<>(), this.mapMutex);
    }

    @Redirect(method = "cache(JLit/unimi/dsi/fastutil/objects/Object2IntMap;)V", at = @At(value = "INVOKE", target = "Ljava/util/Collection;forEach(Ljava/util/function/Consumer;)V"))
    private void redirectForEach(Collection<Long2BooleanMap> instance, Consumer<Long2BooleanMap> consumer) {
        //noinspection SynchronizeOnNonFinalField
        synchronized (this.mapMutex) {
            final Iterator<Long2BooleanMap> iterator = instance.iterator();
            while (iterator.hasNext()) {
                final Long2BooleanMap next = iterator.next();
                consumer.accept(next);
                if (next.isEmpty()) iterator.remove();
            }
        }
    }

}
