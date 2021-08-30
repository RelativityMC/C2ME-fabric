package com.ishland.curseforge.modpackresolver.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Iterator;

public class FilesUtil {

    public static void deleteDir(Path path) throws IOException {
        final Iterator<Path> iterator = Files.walk(path).sorted(Comparator.reverseOrder()).iterator();
        while (iterator.hasNext()) {
            final Path child = iterator.next();
            Files.delete(child);
        }
    }

}
