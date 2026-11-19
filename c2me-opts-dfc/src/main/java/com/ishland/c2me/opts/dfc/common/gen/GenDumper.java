/*
 * SPDX-License-Identifier: LGPL-3.0-only
 *
 * This file is a part of Concurrent Chunk Management Engine,
 * a mod designed to improve the chunk performance of Minecraft.
 *
 * Copyright (c) 2021-2026 ishland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ishland.c2me.opts.dfc.common.gen;

import com.ishland.c2me.opts.dfc.common.ast.opto.OptoPasses;
import com.ishland.c2me.opts.dfc.common.gen.dot.DotGen;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Function;

public class GenDumper {

    public static final File exportDir = new File("./cache/c2me-dfc");

    static {
        try {
            org.spongepowered.asm.util.Files.deleteRecursively(exportDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Path dumpClass(String className, byte[] bytes) {
        File outputFile = new File(exportDir, "classes/" + className + ".class");
        outputFile.getParentFile().mkdirs();
        try {
            com.google.common.io.Files.write(bytes, outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputFile.getAbsoluteFile().toPath();
    }

    public static Path dumpCL(String name, byte[] bytes) {
        File outputFile = new File(exportDir, "cl/" + name + ".cl");
        outputFile.getParentFile().mkdirs();
        try {
            com.google.common.io.Files.write(bytes, outputFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return outputFile.getAbsoluteFile().toPath();
    }

    public static void dumpDot(String name, Path primary, Map<String, OptoPasses.AstPair> roots, Function<Object, String> auxNameProvider) {
        // unoptimized
        {
            DotGen.Context ctx = new DotGen.Context(null);
            DotGen.Context.Builder rootNode = ctx.createExtraBuilder();
            rootNode.boxShape().label("Start");
            for (Map.Entry<String, OptoPasses.AstPair> entry : roots.entrySet()) {
                DotGen.Context.Builder entryBuilder = ctx.createExtraBuilder();
                int entryNode = entryBuilder
                        .boxShape().label(entry.getKey())
                        .edge(ctx.generate(entry.getValue().tryUnoptimized())).label("delegate").finish()
                        .build();
                rootNode.edge(entryNode).label(entry.getKey()).finish();
            }
            int rootId = rootNode.build();
            String content = ctx.write(name + "_unoptimized", rootId);
            try {
                com.google.common.io.Files.write(content.getBytes(StandardCharsets.UTF_8), primary.getParent().resolve(primary.getFileName().toString() + ".unoptimized.gv").toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // optimized
        {
            DotGen.Context ctx = new DotGen.Context(auxNameProvider);
            DotGen.Context.Builder rootNode = ctx.createExtraBuilder();
            rootNode.boxShape().label("Start");
            for (Map.Entry<String, OptoPasses.AstPair> entry : roots.entrySet()) {
                DotGen.Context.Builder entryBuilder = ctx.createExtraBuilder();
                int entryNode = entryBuilder
                        .boxShape().label(entry.getKey())
                        .edge(ctx.generate(entry.getValue().optimized())).label("delegate").finish()
                        .build();
                rootNode.edge(entryNode).label(entry.getKey()).finish();
            }
            int rootId = rootNode.build();
            String content = ctx.write(name, rootId);
            try {
                com.google.common.io.Files.write(content.getBytes(StandardCharsets.UTF_8), primary.getParent().resolve(primary.getFileName().toString() + ".gv").toFile());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
