package com.ishland.c2me.base.common.network;

import com.ishland.c2me.base.common.C2MEConstants;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public record ExtRenderDistance(int renderDistance) implements FabricPacket {

    public static PacketType<ExtRenderDistance> TYPE = PacketType.create(
            new Identifier(C2MEConstants.MODID, C2MEConstants.EXT_RENDER_DISTANCE_ID),
            ExtRenderDistance::new
    );

    public ExtRenderDistance(PacketByteBuf buf) {
        this(buf.readVarInt());
    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(renderDistance);
    }

    @Override
    public PacketType<?> getType() {
        return TYPE;
    }
}
