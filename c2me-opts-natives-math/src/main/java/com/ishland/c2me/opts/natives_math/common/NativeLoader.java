package com.ishland.c2me.opts.natives_math.common;

import io.netty.util.internal.SystemPropertyUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.foreign.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;

public class NativeLoader {

    public static final String NORMALIZED_ARCH = normalizeArch(SystemPropertyUtil.get("os.arch", ""));
    public static final String NORMALIZED_OS = normalizeOs(SystemPropertyUtil.get("os.name", ""));

    private static final Arena arena = Arena.ofAuto();
    public static final SymbolLookup lookup;
    public static final Linker linker = Linker.nativeLinker();
    public static final ISATarget currentMachineTarget;

    static {
        String libName = String.format("%s-%s-%s", NORMALIZED_OS, NORMALIZED_ARCH, System.mapLibraryName("c2me-opts-natives-math"));
        lookup = load0(libName);
        if (lookup == null) {
            currentMachineTarget = null;
        } else {
            try {
                int level = (int) linker.downcallHandle(
                        lookup.find("c2me_natives_get_system_isa").get(),
                        FunctionDescriptor.of(ValueLayout.JAVA_INT)
                ).invokeExact();
                ISATarget target = (ISATarget) ISATarget.getInstance().getEnumConstants()[level];
                while (!target.isNativelySupported()) target = (ISATarget) ISATarget.getInstance().getEnumConstants()[target.ordinal() - 1];
                currentMachineTarget = target;
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

    private static String normalize(String value) {
        return value.toLowerCase(Locale.US).replaceAll("[^a-z0-9]+", "");
    }

    private static String normalizeArch(String value) {
        value = normalize(value);
        if (value.matches("^(x8664|amd64|ia32e|em64t|x64)$")) {
            return "x86_64";
        }
        if (value.matches("^(x8632|x86|i[3-6]86|ia32|x32)$")) {
            return "x86_32";
        }
        if (value.matches("^(ia64|itanium64)$")) {
            return "itanium_64";
        }
        if (value.matches("^(sparc|sparc32)$")) {
            return "sparc_32";
        }
        if (value.matches("^(sparcv9|sparc64)$")) {
            return "sparc_64";
        }
        if (value.matches("^(arm|arm32)$")) {
            return "arm_32";
        }
        if ("aarch64".equals(value)) {
            return "aarch_64";
        }
        if (value.matches("^(ppc|ppc32)$")) {
            return "ppc_32";
        }
        if ("ppc64".equals(value)) {
            return "ppc_64";
        }
        if ("ppc64le".equals(value)) {
            return "ppcle_64";
        }
        if ("s390".equals(value)) {
            return "s390_32";
        }
        if ("s390x".equals(value)) {
            return "s390_64";
        }
        if ("loongarch64".equals(value)) {
            return "loongarch_64";
        }

        return "unknown";
    }

    private static String normalizeOs(String value) {
        value = normalize(value);
        if (value.startsWith("aix")) {
            return "aix";
        }
        if (value.startsWith("hpux")) {
            return "hpux";
        }
        if (value.startsWith("os400")) {
            // Avoid the names such as os4000
            if (value.length() <= 5 || !Character.isDigit(value.charAt(5))) {
                return "os400";
            }
        }
        if (value.startsWith("linux")) {
            return "linux";
        }
        if (value.startsWith("macosx") || value.startsWith("osx") || value.startsWith("darwin")) {
            return "osx";
        }
        if (value.startsWith("freebsd")) {
            return "freebsd";
        }
        if (value.startsWith("openbsd")) {
            return "openbsd";
        }
        if (value.startsWith("netbsd")) {
            return "netbsd";
        }
        if (value.startsWith("solaris") || value.startsWith("sunos")) {
            return "sunos";
        }
        if (value.startsWith("windows")) {
            return "windows";
        }

        return "unknown";
    }

}
