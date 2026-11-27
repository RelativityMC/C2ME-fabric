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

package com.ishland.c2me.base.mixin.client.taskredir;

import com.ishland.c2me.base.common.GlobalExecutors;
import com.ishland.c2me.base.mixin.access.INameableExecutor;
import net.minecraft.client.render.chunk.ChunkBuilder;
import net.minecraft.util.thread.NameableExecutor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkBuilder.class)
public class MixinChunkBuilder {

    @Redirect(method = {"send"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/util/thread/NameableExecutor;execute(Ljava/lang/Runnable;)V"), require = 1)
    private void redirectExecutor(NameableExecutor instance, Runnable runnable) {
        // not time critical but still high priority, test when porting
        GlobalExecutors.prioritizedScheduler.schedule(INameableExecutor.invokeWrapForTracy(runnable), 1);
    }

}
