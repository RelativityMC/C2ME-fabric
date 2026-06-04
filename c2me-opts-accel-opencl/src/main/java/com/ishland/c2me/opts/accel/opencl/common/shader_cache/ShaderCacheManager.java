/*
 * All Rights Reserved
 *
 * Copyright (c) 2025-2026 ishland
 *
 * All rights reserved. Do not redistribute.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.ishland.c2me.opts.accel.opencl.common.shader_cache;

import com.google.common.base.Stopwatch;
import com.ishland.c2me.opts.accel.opencl.common.zstd.ZstdInputStreamNoFinalizer;
import com.ishland.c2me.opts.accel.opencl.common.enumeration.OpenCLDeviceMetadata;
import com.ishland.c2me.opts.accel.opencl.common.util.CLUtil;
import com.ishland.c2me.opts.accel.opencl.common.compiler.OpenCLCGen;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.lwjgl.PointerBuffer;
import org.lwjgl.opencl.CL10;
import org.lwjgl.opencl.CL12;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ShaderCacheManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ShaderCacheManager.class);

    private final Map<String, Path> cacheIndex;

    public ShaderCacheManager() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            this.cacheIndex = Collections.unmodifiableMap(scan());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stopwatch.stop();
        LOGGER.info("Indexed {} shader cache entries in {}", this.cacheIndex.size(), stopwatch);
    }

    private static Map<String, Path> scan() throws IOException {
        Path baseDir = Path.of(".", "config", "c2me-shader-delivery");

        if (!Files.isDirectory(baseDir)) {
            return new Object2ObjectOpenHashMap<>();
        }

        Object2ObjectOpenHashMap<String, Path> index = new Object2ObjectOpenHashMap<>();

        for (Path path : Files.list(baseDir).sorted().toList()) {
            path = path.normalize();
            if (path.getFileName().toString().endsWith(".tar.zst") && Files.isRegularFile(path)) {
                Stopwatch stopwatch = Stopwatch.createStarted();
                try (var in = new TarArchiveInputStream(new BufferedInputStream(new ZstdInputStreamNoFinalizer(Files.newInputStream(path)), 1048576))) {
                    TarArchiveEntry entry;
                    while ((entry = in.getNextEntry()) != null) {
                        String name = entry.getName();
                        if (name.endsWith("/") || !entry.isFile()) {
                            continue;
                        }
                        if (index.containsKey(name)) {
                            throw new IllegalStateException(String.format("Duplicate entry in (%s) and (%s): (%s)", path, index.get(name), name));
                        }
                        index.put(name, path);
                    }
                } catch (IOException e) {
                    LOGGER.error("Failed to read {}", path, e);
                }
                stopwatch.stop();
                LOGGER.info("Read {} fully in {}", path, stopwatch);
            }
        }

        return index;
    }

    /**
     * @apiNote path must not include trailing slashes
     */
    public void tryCacheDirs(long context, OpenCLDeviceMetadata metadata, EnumMap<OpenCLCGen.ProgramType, Long> map, String... paths) {
        Map<Path, Set<OpenCLCGen.ProgramType>> path2Programs =
                Arrays.stream(OpenCLCGen.ProgramType.values())
                        .flatMap(type -> Arrays.stream(paths).map(path -> Pair.of(type, path)))
                        .map(pair -> Pair.of(pair.left(), this.cacheIndex.get(String.format("%s/%s.bin", pair.right(), pair.left().name()))))
                        .filter(pair -> pair.value() != null)
                        .collect(Collectors.groupingBy(
                                Pair::value,
                                Collectors.mapping(Pair::key, Collectors.toSet())
                        ));

        if (path2Programs.isEmpty()) {
            LOGGER.info("Cache miss fully for {}", Arrays.toString(paths));
        }

        for (Map.Entry<Path, Set<OpenCLCGen.ProgramType>> pathEntry : path2Programs.entrySet()) {
            Path archivePath = pathEntry.getKey();
            Set<OpenCLCGen.ProgramType> types = new HashSet<>(pathEntry.getValue());
            tryRead(paths, context, metadata, map, types, archivePath);
        }
    }

    private static void tryRead(String[] paths, long context, OpenCLDeviceMetadata metadata, EnumMap<OpenCLCGen.ProgramType, Long> map, Set<OpenCLCGen.ProgramType> types, Path archivePath) {
        LOGGER.info("Loading programs {} from archive {}", Arrays.toString(types.toArray(OpenCLCGen.ProgramType[]::new)), archivePath);
        Stopwatch stopwatch = Stopwatch.createStarted();
        try (var in = new TarArchiveInputStream(new BufferedInputStream(new ZstdInputStreamNoFinalizer(Files.newInputStream(archivePath)), 1048576))) {
            TarArchiveEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.endsWith("/") || !entry.isFile() || !name.endsWith(".bin")) {
                    continue;
                }
                if (Arrays.stream(paths).noneMatch(name::startsWith)) {
                    continue;
                }
                if (!in.canReadEntryData(entry)) {
                    LOGGER.error("Unable to read {} from archive {}, something is wrong", name, archivePath);
                    continue;
                }
                OpenCLCGen.ProgramType programType;
                try {
                    programType = OpenCLCGen.ProgramType.valueOf(name.substring(name.lastIndexOf('/') + 1, name.length() - ".bin".length()));
                } catch (IllegalArgumentException e) {
                    LOGGER.warn("Illegal filename: {}", name);
                    continue;
                }
                if (!types.contains(programType)) {
                    LOGGER.warn("Ignoring {} from archive {} because it is not in the index", name, archivePath);
                    continue;
                }
                LOGGER.info("Attempting to load cache for {}: {}!{}", programType, archivePath, name);
                long program = tryLoadBinary(context, metadata, in.readAllBytes());
                if (program != 0L) {
                    LOGGER.info("Cache hit for {}: {}!{}", programType, archivePath, name);
                    map.put(programType, program);
                    types.remove(programType);
                }
                if (types.isEmpty()) {
                    break;
                }
            }
        } catch (IOException e) {
            LOGGER.error("Failed to read {}", archivePath, e);
        }
        stopwatch.stop();
        LOGGER.info("Read {} in {}", archivePath, stopwatch);
    }

    public byte[] tryCache(String path) {
        Path archivePath = this.cacheIndex.get(path);
        if (archivePath == null) return null;

        try (var in = new TarArchiveInputStream(new BufferedInputStream(new ZstdInputStreamNoFinalizer(Files.newInputStream(archivePath)), 1048576))) {
            TarArchiveEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.endsWith("/") || !entry.isFile() || !name.equals(path)) {
                    continue;
                }
                if (!in.canReadEntryData(entry)) {
                    LOGGER.error("Unable to read {} from archive {}, something is wrong", path, archivePath);
                    continue;
                }
                return in.readAllBytes();
            }
            return null;
        } catch (IOException e) {
            LOGGER.error("Failed to read {}", archivePath, e);
            return null;
        }
    }

    private static long tryLoadBinary(long context, OpenCLDeviceMetadata metadata, byte[] binary) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer errorCodeRet = stack.callocInt(1);

            ByteBuffer byteBuffer = MemoryUtil.memAlloc(binary.length);
            byteBuffer.put(0, binary).rewind();

            PointerBuffer devicePtrBuffer = stack.mallocPointer(1).put(0, metadata.devicePtr).rewind();
            IntBuffer binErrorCodeRet = stack.callocInt(1);

            long program = CL12.clCreateProgramWithBinary(
                    context,
                    devicePtrBuffer,
                    byteBuffer,
                    binErrorCodeRet,
                    errorCodeRet
            );
            MemoryUtil.memFree(byteBuffer);
            if (errorCodeRet.get(0) == CL10.CL_INVALID_BINARY) {
                LOGGER.info("Shader binary loading failed: CL_INVALID_BINARY");
                return 0L;
            }
            CLUtil.checkCLError(errorCodeRet);

            try {
                CLUtil.checkCLError(binErrorCodeRet);

                int errcode = CL12.nclBuildProgram(program, 1, MemoryUtil.memAddress(devicePtrBuffer), 0L, 0L, 0L);
                if (errcode == CL10.CL_BUILD_PROGRAM_FAILURE) {
                    LOGGER.info("Shader binary loading failed: CL_BUILD_PROGRAM_FAILURE");
                    CLUtil.checkCLError(CL12.clReleaseProgram(program));
                    return 0L;
                }
                CLUtil.checkCLError(errcode);
            } catch (Throwable t) {
                CLUtil.checkCLError(CL12.clReleaseProgram(program));
                throw t;
            }

            return program;
        } catch (Throwable t) {
            LOGGER.error("Failed to load compilation cache", t);
            return 0L;
        }
    }

}
