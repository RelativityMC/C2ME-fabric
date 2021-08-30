package com.ishland.curseforge.modpackresolver;

import com.ishland.curseforge.modpackresolver.provider.ModPackProvider;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class ModPackResolverPlugin implements Plugin<Project> {

    @Override
    public void apply(@NotNull Project project) {
        applyRepositories(project);

        System.setProperty("log4j2.disable.jmx", "true");
        System.setProperty("log4j.shutdownHookEnabled", "false");
        System.setProperty("log4j.skipJansi", "true");

        final NamedDomainObjectProvider<Configuration> curseforgeModpack = project.getConfigurations().register("curseforgeModpack", files -> files.setTransitive(false));
        project.afterEvaluate(__ -> {
            curseforgeModpack.configure(files -> {
                for (Dependency dependency : files.getDependencies()) {
                    try {
                        ModPackProvider.provide(dependency, project);
                    } catch (IOException e) {
                        throw new RuntimeException("Unable to configure modpack", e);
                    }
                }
            });
        });
    }

    private void applyRepositories(Project project) {
        project.getRepositories().maven(repo -> {
            repo.setUrl("https://www.cursemaven.com");
            repo.mavenContent(descriptor -> descriptor.includeGroup("curse.maven"));
        });
    }
}
