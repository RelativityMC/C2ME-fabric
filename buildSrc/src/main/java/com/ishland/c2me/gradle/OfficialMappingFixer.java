package com.ishland.c2me.gradle;

import net.fabricmc.mappingio.format.tiny.Tiny2FileReader;
import net.fabricmc.mappingio.format.tiny.Tiny2FileWriter;
import net.fabricmc.mappingio.tree.MappingTree;
import net.fabricmc.mappingio.tree.MemoryMappingTree;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class OfficialMappingFixer {

    /**
     * Cleans up mapping leakage from intermediary to official namespace caused by yarn
     */
    public static void apply(Path mergedMappingsPath) throws IOException {
        MemoryMappingTree mappingTree = new MemoryMappingTree();

        try (final BufferedReader reader = Files.newBufferedReader(mergedMappingsPath, StandardCharsets.UTF_8)) {
            Tiny2FileReader.read(reader, mappingTree);
        }

        boolean modified = false;

        for (MappingTree.ClassMapping classMapping : mappingTree.getClasses()) {
            loop0:
            while (true) {
                for (MappingTree.MethodMapping methodMapping : classMapping.getMethods()) {
                    String intermediary = methodMapping.getName("intermediary");
                    String official = methodMapping.getName("official");
                    if (intermediary != null && official != null && intermediary.startsWith("method_") && intermediary.equals(official)) {
                        classMapping.removeMethod(methodMapping.getSrcName(), methodMapping.getSrcDesc());
                        modified = true;
                        continue loop0;
                    }
                }
                break loop0;
            }
        }

        if (!modified) {
            return;
        }

        try (var writer = new Tiny2FileWriter(Files.newBufferedWriter(mergedMappingsPath, StandardCharsets.UTF_8, StandardOpenOption.CREATE), false)) {
            mappingTree.accept(writer);
        }
    }

}
