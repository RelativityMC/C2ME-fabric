package com.ishland.c2me.base.common.network;

import com.ishland.c2me.base.common.C2MEConstants;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ExtRenderDistance(int renderDistance) implements CustomPayload {

    public static final PacketCodec<PacketByteBuf, ExtRenderDistance> CODEC = PacketCodec.of(ExtRenderDistance::write, ExtRenderDistance::new);
    public static final Id<ExtRenderDistance> ID = new Id<>(new Identifier(C2MEConstants.MODID, C2MEConstants.EXT_RENDER_DISTANCE_ID));

    static {
        PayloadTypeRegistry.configurationC2S().register(ExtRenderDistance.ID, ExtRenderDistance.CODEC);
        PayloadTypeRegistry.playC2S().register(ExtRenderDistance.ID, ExtRenderDistance.CODEC);
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
