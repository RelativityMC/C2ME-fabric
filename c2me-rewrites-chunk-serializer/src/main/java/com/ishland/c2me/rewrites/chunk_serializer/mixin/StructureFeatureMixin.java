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

//package com.ishland.c2me.opts.chunk_serializer.mixin;
//
//import com.ishland.c2me.common.optimization.chunkserialization.NbtWriter;
//import com.ishland.c2me.common.optimization.chunkserialization.StructureFeatureAccessor;
//import com.ishland.c2me.rewrites.chunk_serializer.common.chunk_serializer.NbtWriter;
//import com.ishland.c2me.opts.chunk_serializer.common.StructureFeatureAccessor;
//import net.minecraft.world.gen.feature.StructureFeature;
//import org.spongepowered.asm.mixin.Mixin;
//
//@Mixin(StructureFeature.class)
//public class StructureFeatureMixin implements StructureFeatureAccessor {
//    private byte[] nameBytes;
//
//    @Override
//    public byte[] getNameBytes() {
//        if (this.nameBytes == null) {
//            //noinspection SuspiciousMethodCalls
//            final String name = StructureFeature.STRUCTURES.inverse().get(this);
//            return this.nameBytes = NbtWriter.getStringBytes(name);
//        }
//        return this.nameBytes;
//    }
//}
