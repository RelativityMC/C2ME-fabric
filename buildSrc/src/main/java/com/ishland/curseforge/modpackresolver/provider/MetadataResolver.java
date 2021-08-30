package com.ishland.curseforge.modpackresolver.provider;

import com.google.gson.Gson;
import com.ishland.curseforge.modpackresolver.Constants;
import com.ishland.curseforge.modpackresolver.meta.CurseMeta;
import com.ishland.curseforge.modpackresolver.meta.ModPackManifest;
import org.gradle.api.Project;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class MetadataResolver {

    public static CurseMeta getCurseMeta(String projectId, String fileId, Project project) throws IOException {
        project.getLogger().lifecycle("Resolving cursemeta: projectId={}, fileId={}", projectId, fileId);
        URL modpackURL = new URL(String.format("%s/%s/%s.json", Constants.CURSEMETA, projectId, fileId));
        try (final InputStream in = modpackURL.openStream(); final Reader reader = new InputStreamReader(in)) {
            return new Gson().fromJson(reader, CurseMeta.class);
        }
    }

    public static ModPackManifest parseManifest(Path path) throws IOException {
        try (final BufferedReader reader = Files.newBufferedReader(path)) {
            return new Gson().fromJson(reader, ModPackManifest.class);
        }
    }

}
