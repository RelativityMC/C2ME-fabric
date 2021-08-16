package com.ishland.curseforge.modpackresolver.dependency;

import net.fabricmc.loader.discovery.ModCandidate;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.SelfResolvingDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.dependencies.DefaultSelfResolvingDependency;
import org.gradle.api.internal.file.FileCollectionInternal;
import org.gradle.api.tasks.TaskDependency;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collections;
import java.util.Set;

public final class ModDependency extends DefaultSelfResolvingDependency implements SelfResolvingDependency {

    private final ModCandidate modCandidate;

    /**
     */
    public ModDependency(ModCandidate modCandidate, FileCollection files) {
        super((FileCollectionInternal) files);
        this.modCandidate = modCandidate;
    }

    @NotNull
    @Override
    public Set<File> resolve() {
        return resolve(true);
    }

    @Override
    public TaskDependency getBuildDependencies() {
        return task -> Collections.emptySet();
    }

    @Nullable
    @Override
    public String getGroup() {
        return "curseforge.modpackresolver.generated";
    }

    @Override
    public String getName() {
        return modCandidate.getInfo().getId();
    }

    @Nullable
    @Override
    public String getVersion() {
        return modCandidate.getInfo().getVersion().getFriendlyString();
    }

    @Override
    public boolean contentEquals(Dependency dependency) {
        if (dependency instanceof ModDependency) {
            return this.equals(dependency);
        }
        return false;
    }

    @Override
    public DefaultSelfResolvingDependency copy() {
        return new ModDependency(modCandidate, getFiles());
    }

}
