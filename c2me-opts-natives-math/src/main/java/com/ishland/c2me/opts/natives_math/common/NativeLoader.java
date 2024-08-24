package com.ishland.c2me.opts.natives_math.common;

import io.netty.util.internal.PlatformDependent;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class NativeLoader {

    private static final Arena arena = Arena.ofShared();
    public static final SymbolLookup lookup;
    public static final Linker linker = Linker.nativeLinker();
    public static final ISATarget currentMachineTarget;

    static {
        String libName = String.format("%s-%s-%s", PlatformDependent.normalizedOs(), PlatformDependent.normalizedArch(), System.mapLibraryName("c2me-opts-natives-math"));
        lookup = load0(libName);
        if (lookup == null) {
            currentMachineTarget = null;
        } else {
            try {
                int level = (int) linker.downcallHandle(
                        lookup.find("c2me_natives_get_system_isa").get(),
                        FunctionDescriptor.of(ValueLayout.JAVA_INT)
                ).invokeExact();
                currentMachineTarget = (ISATarget) ISATarget.getInstance().getEnumConstants()[level];
                System.out.println(String.format("Detected maximum supported ISA target: %s", currentMachineTarget));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static SymbolLookup load0(String libName) {
        // load from resources
        try (final InputStream in = NativeLoader.class.getClassLoader().getResourceAsStream(libName)) {
            if (in == null) {
                throw new IOException("Cannot find native library " + libName);
            }
            final Path tempFile;
            if (Boolean.getBoolean("vectorizedgen.preserveNative")) {
                tempFile = Path.of(".", libName);
            } else {
                tempFile = Files.createTempFile(null, libName);
                tempFile.toFile().deleteOnExit();
            }
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            return SymbolLookup.libraryLookup(tempFile, arena);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

}
