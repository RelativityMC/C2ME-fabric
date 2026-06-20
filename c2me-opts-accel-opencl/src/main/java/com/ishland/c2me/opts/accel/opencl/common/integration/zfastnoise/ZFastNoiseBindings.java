package com.ishland.c2me.opts.accel.opencl.common.integration.zfastnoise;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.BlockState;
import net.minecraft.util.collection.BoundedRegionArray;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.ProtoChunk;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.ByteBuffer;

public class ZFastNoiseBindings {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZFastNoiseBindings.class);

    public static final MethodHandle MH_FastCopyBufferDataIntoChunks$copyData;

    static {
        if (FabricLoader.getInstance().isModLoaded("zfastnoise")) {
            MethodHandle mh_FastCopyBufferDataIntoChunks$copyData;

            try {
                Class<?> clazz = Class.forName("org.codeberg.zenxarch.fastnoise.ocl.FastCopyBufferDataIntoChunks");
                mh_FastCopyBufferDataIntoChunks$copyData = MethodHandles.lookup().findStatic(
                        clazz,
                        "copyData",
                        MethodType.methodType(
                                void.class,
                                BoundedRegionArray.class, // chunks
                                BlockState[].class, // clBlockStateMappings
                                int.class, // verticalSize
                                ChunkGeneratorSettings.class, // settings
                                int.class, // horizontalSize
                                ByteBuffer.class, // blockOutBufferData
                                ChunkPos.class, // startingPos
                                int.class // batchSize
                        )
                );
                LOGGER.info("Binding to FastCopyBufferDataIntoChunks.copyData");
            } catch (Throwable t) {
                LOGGER.warn("Unable to find compatible FastCopyBufferDataIntoChunks.copyData", t);
                mh_FastCopyBufferDataIntoChunks$copyData = null;
            }

            MH_FastCopyBufferDataIntoChunks$copyData = mh_FastCopyBufferDataIntoChunks$copyData;
        } else {
            MH_FastCopyBufferDataIntoChunks$copyData = null;
        }
    }

    public static void call_FastCopyBufferDataIntoChunks$copyData(BoundedRegionArray<ProtoChunk> chunks,
                                                                  BlockState[] clBlockStateMappings,
                                                                  int verticalSize,
                                                                  ChunkGeneratorSettings settings,
                                                                  int horizontalSize,
                                                                  ByteBuffer blockOutBufferData,
                                                                  ChunkPos startingPos,
                                                                  int batchSize) {
        try {
            MH_FastCopyBufferDataIntoChunks$copyData.invokeExact(
                    chunks,
                    clBlockStateMappings,
                    verticalSize,
                    settings,
                    horizontalSize,
                    blockOutBufferData,
                    startingPos,
                    batchSize
            );
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
