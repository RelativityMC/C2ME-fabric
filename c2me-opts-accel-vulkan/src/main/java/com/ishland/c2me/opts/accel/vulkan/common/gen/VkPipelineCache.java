package com.ishland.c2me.opts.accel.vulkan.common.gen;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.LongBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.CRC32C;

public class VkPipelineCache {

    private static final Logger LOGGER = LogManager.getLogger("C2ME Vulkan Pipeline Cache");

    private static final int HEADER_LENGTH = 32;
    private static final int HEADER_VERSION_ONE = 1;

    private static final int MAGIC = 0x504D3243;
    private static final int FORMAT_VERSION = 1;
    private static final int PREFIX_LENGTH = 16;

    private final VkDevice device;
    private final Path file;
    private final long handle;

    private final boolean seeded;
    private final int loadedChecksum;
    private final int loadedLength;

    private VkPipelineCache(VkDevice device, Path file, long handle, Loaded loaded) {
        this.device = device;
        this.file = file;
        this.handle = handle;
        this.seeded = loaded != null;
        this.loadedChecksum = loaded != null ? loaded.checksum() : 0;
        this.loadedLength = loaded != null ? loaded.length() : -1;
    }

    private static int checksum(byte[] data, int offset, int length) {
        CRC32C crc = new CRC32C();
        crc.update(data, offset, length);
        return (int) crc.getValue();
    }

    public static VkPipelineCache open(VkDevice device, VkPhysicalDevice physicalDevice, Path file) {
        Loaded existing = readIfUsable(physicalDevice, file);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPipelineCacheCreateInfo info = VkPipelineCacheCreateInfo.calloc(stack).sType$Default();

            ByteBuffer seed = null;
            try {
                if (existing != null) {
                    seed = MemoryUtil.memAlloc(existing.length());
                    seed.put(existing.stored(), PREFIX_LENGTH, existing.length()).flip();
                    info.pInitialData(seed);
                }

                LongBuffer pCache = stack.mallocLong(1);
                VkUtil.check(VK10.vkCreatePipelineCache(device, info, null, pCache));
                return new VkPipelineCache(device, file, pCache.get(0), existing);
            } finally {
                if (seed != null) MemoryUtil.memFree(seed);
            }
        }
    }

    private static Loaded readIfUsable(VkPhysicalDevice physicalDevice, Path file) {
        byte[] stored;
        try {
            if (!Files.isRegularFile(file)) return null;
            stored = Files.readAllBytes(file);
        } catch (IOException e) {
            LOGGER.warn("Could not read pipeline cache {}", file, e);
            return null;
        }
        if (stored.length < PREFIX_LENGTH + HEADER_LENGTH) return null;

        ByteBuffer prefix = ByteBuffer.wrap(stored).order(ByteOrder.LITTLE_ENDIAN);
        if (prefix.getInt(0) != MAGIC || prefix.getInt(4) != FORMAT_VERSION) return null;

        int length = prefix.getInt(12);
        if (length < HEADER_LENGTH || length != stored.length - PREFIX_LENGTH) {
            LOGGER.info("Discarding pipeline cache {}: truncated", file);
            return null;
        }

        int checksum = checksum(stored, PREFIX_LENGTH, length);
        if (checksum != prefix.getInt(8)) {
            LOGGER.info("Discarding pipeline cache {}: checksum mismatch", file);
            return null;
        }

        ByteBuffer header = ByteBuffer.wrap(stored, PREFIX_LENGTH, length).order(ByteOrder.LITTLE_ENDIAN)
                .slice().order(ByteOrder.LITTLE_ENDIAN);
        if (header.getInt(0) < HEADER_LENGTH || header.getInt(4) != HEADER_VERSION_ONE) return null;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkPhysicalDeviceProperties properties = VkPhysicalDeviceProperties.calloc(stack);
            VK10.vkGetPhysicalDeviceProperties(physicalDevice, properties);

            if (header.getInt(8) != properties.vendorID() || header.getInt(12) != properties.deviceID()) {
                LOGGER.info("Discarding pipeline cache {}: written by a different device", file);
                return null;
            }
            ByteBuffer uuid = properties.pipelineCacheUUID();
            for (int i = 0; i < VK10.VK_UUID_SIZE; i++) {
                if (stored[PREFIX_LENGTH + 16 + i] != uuid.get(i)) {
                    LOGGER.info("Discarding pipeline cache {}: written by a different driver version", file);
                    return null;
                }
            }
        }
        return new Loaded(stored, length, checksum);
    }

    public void saveIfChanged() {
        ByteBuffer data = null;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pSize = stack.mallocPointer(1);
            VkUtil.check(VK10.vkGetPipelineCacheData(this.device, this.handle, pSize, null));

            int size = (int) pSize.get(0);
            if (size <= HEADER_LENGTH) return;

            data = MemoryUtil.memAlloc(size);
            VkUtil.check(VK10.vkGetPipelineCacheData(this.device, this.handle, pSize, data));

            byte[] bytes = new byte[size];
            data.get(bytes);

            int checksum = checksum(bytes, 0, size);
            if (size == this.loadedLength && checksum == this.loadedChecksum) return;

            byte[] stored = new byte[PREFIX_LENGTH + size];
            ByteBuffer.wrap(stored).order(ByteOrder.LITTLE_ENDIAN)
                    .putInt(0, MAGIC)
                    .putInt(4, FORMAT_VERSION)
                    .putInt(8, checksum)
                    .putInt(12, size);
            System.arraycopy(bytes, 0, stored, PREFIX_LENGTH, size);

            Files.createDirectories(this.file.getParent());
            Path temporary = this.file.resolveSibling(this.file.getFileName() + ".tmp");
            Files.write(temporary, stored);
            Files.move(temporary, this.file, StandardCopyOption.REPLACE_EXISTING);

            LOGGER.debug("Wrote {} KiB of pipeline cache data to {}", size / 1024, this.file);
        } catch (IOException e) {
            LOGGER.warn("Could not write pipeline cache {}", this.file, e);
        } finally {
            if (data != null) MemoryUtil.memFree(data);
        }
    }

    public boolean wasSeeded() {
        return this.seeded;
    }

    public long getHandle() {
        return this.handle;
    }

    public void close() {
        VK10.vkDestroyPipelineCache(this.device, this.handle, null);
    }

    private record Loaded(byte[] stored, int length, int checksum) {
    }

}
