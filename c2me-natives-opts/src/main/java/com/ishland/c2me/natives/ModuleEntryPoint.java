package com.ishland.c2me.natives;

import com.ishland.c2me.natives.common.NativesInterface;
import io.netty.util.internal.PlatformDependent;
import org.jetbrains.annotations.VisibleForTesting;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class ModuleEntryPoint {

    private static final String nativeName = "libc2me-natives-opts";

    private static final boolean enabled;

    static {
        if (ModuleLayer.boot().findModule("jdk.incubator.foreign").isPresent()) {
            boolean success = false;
            try {
                if (loadNatives()) {
                    NativesInterface.init();
                    success = true;
                }
            } catch (Throwable t) {
                System.err.println("Failed to load libraries: %s".formatted(t.getMessage()));
                t.printStackTrace();
            }
            enabled = success;
        } else {
            enabled = false;
        }
    }

    @VisibleForTesting
    public static void init() {
    }

    private static boolean loadNatives() {
        try {
            final String suffix;
            if (PlatformDependent.isWindows()) {
                suffix = ".dll";
            } else if (PlatformDependent.isOsx()) {
                suffix = ".dylib";
            } else {
                suffix = ".so";
            }
            final InputStream resource = ModuleEntryPoint.class.getClassLoader().getResourceAsStream(nativeName + suffix);
            if (resource != null) {
                try {
                    final File tempFile = File.createTempFile(nativeName + "-", suffix);
                    Files.copy(resource, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
//                    System.out.println("Loading %s".formatted(tempFile.getAbsolutePath()));
                    System.load(tempFile.getAbsolutePath());
                } finally {
                    resource.close();
                }
                return true;
            } else {
                System.err.println("Failed to locate %s%s".formatted(nativeName, suffix));
                return false;
            }
        } catch (Throwable t) {
            System.err.println("Failed to load native libraries: %s".formatted(t.getMessage()));
            t.printStackTrace();
            return false;
        }
    }

}
