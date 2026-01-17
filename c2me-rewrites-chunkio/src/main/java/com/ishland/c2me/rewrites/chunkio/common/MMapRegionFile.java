package com.ishland.c2me.rewrites.chunkio.common;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.storage.ChunkCompressionFormat;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class MMapRegionFile {

    private final MappedByteBuffer mmap;
    private final FileChannel channel;

    public MMapRegionFile(Path path) throws IOException {
        this.channel = FileChannel.open(path, StandardOpenOption.READ);
        this.mmap = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
        this.mmap.order(ByteOrder.BIG_ENDIAN);
    }

    public DataInputStream getChunkInputStream(ChunkPos pos) throws IOException {
        int x = pos.x & 31;
        int z = pos.z & 31;
        int headerOffset = (x + z * 32) * 4;
        
        if (headerOffset + 4 > mmap.limit()) return null;
        int location = mmap.getInt(headerOffset);
        if (location == 0) return null;

        int offset = (location >> 8) * 4096;
        int sectors = location & 0xFF;
        
        if (offset + 4 > mmap.limit()) return null;
        int length = mmap.getInt(offset);
        if (length <= 0 || length > sectors * 4096) return null;

        if (offset + 4 + length > mmap.limit()) return null;
        byte compressionTypeId = mmap.get(offset + 4);
        int compressionId = compressionTypeId & 0xFF;
        ChunkCompressionFormat compressionFormat;
        if (compressionId == ChunkCompressionFormat.GZIP.getId()) compressionFormat = ChunkCompressionFormat.GZIP;
        else if (compressionId == ChunkCompressionFormat.DEFLATE.getId()) compressionFormat = ChunkCompressionFormat.DEFLATE;
        else if (compressionId == ChunkCompressionFormat.UNCOMPRESSED.getId()) compressionFormat = ChunkCompressionFormat.UNCOMPRESSED;
        else compressionFormat = ChunkCompressionFormat.getCurrentFormat();
        
        byte[] data = new byte[length - 1];
        ByteBuffer buffer = mmap.duplicate();
        buffer.position(offset + 5);
        buffer.get(data);
        
        InputStream inputStream = compressionFormat.wrap(new ByteArrayInputStream(data));
        return new DataInputStream(inputStream);
    }

    public void close() throws IOException {
        channel.close();
    }
}
