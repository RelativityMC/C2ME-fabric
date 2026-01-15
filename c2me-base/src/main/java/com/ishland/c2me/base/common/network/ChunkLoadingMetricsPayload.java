package com.ishland.c2me.base.common.network;

import com.ishland.c2me.base.common.metrics.ChunkLoadingSnapshot;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ChunkLoadingMetricsPayload(ChunkLoadingSnapshot snapshot) implements CustomPayload {

    public static final PacketCodec<PacketByteBuf, ChunkLoadingMetricsPayload> CODEC = PacketCodec.of(
        ChunkLoadingMetricsPayload::write,
        ChunkLoadingMetricsPayload::new
    );

    public static final Id<ChunkLoadingMetricsPayload> ID = new Id<>(
        Identifier.of("c2me", "chunk_loading_metrics")
    );

    public ChunkLoadingMetricsPayload(PacketByteBuf buf) {
        this(new ChunkLoadingSnapshot(
            buf.readDouble(), // avgChunkLoadTimeMs
            buf.readDouble(), // minChunkLoadTimeMs
            buf.readDouble(), // maxChunkLoadTimeMs
            buf.readVarInt(), // currentChunkQueueSize

            buf.readDouble(), // avgWorldgenTimeMs
            buf.readDouble(), // avgWorldgenLoadTimeMs

            buf.readDouble(), // avgIoReadTimeMs
            buf.readDouble(), // avgIoWriteTimeMs
            buf.readVarInt(), // currentIoBacklogSize
            buf.readVarInt(), // currentIoPendingWrites

            buf.readVarInt(), // currentSchedulerQueueSize
            buf.readVarInt()  // currentSchedulerActiveThreads
        ));
    }

    public void write(PacketByteBuf buf) {
        buf.writeDouble(snapshot.avgChunkLoadTimeMs());
        buf.writeDouble(snapshot.minChunkLoadTimeMs());
        buf.writeDouble(snapshot.maxChunkLoadTimeMs());
        buf.writeVarInt(snapshot.currentChunkQueueSize());

        buf.writeDouble(snapshot.avgWorldgenTimeMs());
        buf.writeDouble(snapshot.avgWorldgenLoadTimeMs());

        buf.writeDouble(snapshot.avgIoReadTimeMs());
        buf.writeDouble(snapshot.avgIoWriteTimeMs());
        buf.writeVarInt(snapshot.currentIoBacklogSize());
        buf.writeVarInt(snapshot.currentIoPendingWrites());

        buf.writeVarInt(snapshot.currentSchedulerQueueSize());
        buf.writeVarInt(snapshot.currentSchedulerActiveThreads());
    }

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

}