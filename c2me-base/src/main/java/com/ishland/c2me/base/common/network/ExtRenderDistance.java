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

package com.ishland.c2me.base.common.network;

import com.ishland.c2me.base.common.C2MEConstants;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ExtRenderDistance(int renderDistance) implements CustomPayload {

    public static final PacketCodec<PacketByteBuf, ExtRenderDistance> CODEC = PacketCodec.of(ExtRenderDistance::write, ExtRenderDistance::new);
    public static final Id<ExtRenderDistance> ID = new Id<>(Identifier.of(C2MEConstants.MODID, C2MEConstants.EXT_RENDER_DISTANCE_ID));

    static {
        PayloadTypeRegistry.serverboundConfiguration().register(ExtRenderDistance.ID, ExtRenderDistance.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ExtRenderDistance.ID, ExtRenderDistance.CODEC);
    }

    public static void init() {
    }

    public ExtRenderDistance(PacketByteBuf buf) {
        this(buf.readVarInt());
    }

    public void write(PacketByteBuf buf) {
        buf.writeVarInt(renderDistance);
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}
