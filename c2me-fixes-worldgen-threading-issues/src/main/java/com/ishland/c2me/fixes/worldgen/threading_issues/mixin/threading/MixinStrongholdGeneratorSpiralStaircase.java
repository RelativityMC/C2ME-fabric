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

import com.ishland.c2me.fixes.worldgen.threading_issues.common.IStrongholdGenerator;
import net.minecraft.structure.StrongholdGenerator;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(StrongholdGenerator.SpiralStaircase.class)
public class MixinStrongholdGeneratorSpiralStaircase {

    @Redirect(method = "fillOpenings", at = @At(value = "FIELD", target = "Lnet/minecraft/structure/StrongholdGenerator;activePieceType:Ljava/lang/Class;", opcode = Opcodes.PUTSTATIC))
    private void redirectGetActivePieceType(Class<? extends StrongholdGenerator.Piece> value) {
        IStrongholdGenerator.Holder.INSTANCE.getActivePieceTypeThreadLocal().set(value);
    }

}
