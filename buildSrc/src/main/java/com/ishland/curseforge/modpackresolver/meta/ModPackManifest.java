package com.ishland.curseforge.modpackresolver.meta;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ModPackManifest {

    @SerializedName("minecraft")
    private Minecraft minecraft;

    @SerializedName("manifestVersion")
    private int manifestVersion;

    @SerializedName("author")
    private String author;

    @SerializedName("name")
    private String name;

    @SerializedName("manifestType")
    private String manifestType;

    @SerializedName("files")
    private List<FilesItem> files;

    @SerializedName("overrides")
    private String overrides;

    @SerializedName("version")
    private String version;

    public Minecraft getMinecraft() {
        return minecraft;
    }

    public int getManifestVersion() {
        return manifestVersion;
    }

    public String getAuthor() {
        return author;
    }

    public String getName() {
        return name;
    }

    public String getManifestType() {
        return manifestType;
    }

    public List<FilesItem> getFiles() {
        return files;
    }

    public String getOverrides() {
        return overrides;
    }

    public String getVersion() {
        return version;
    }

    @Override
    public String toString() {
        return
                "ModPackManifest{" +
                        "minecraft = '" + minecraft + '\'' +
                        ",manifestVersion = '" + manifestVersion + '\'' +
                        ",author = '" + author + '\'' +
                        ",name = '" + name + '\'' +
                        ",manifestType = '" + manifestType + '\'' +
                        ",files = '" + files + '\'' +
                        ",overrides = '" + overrides + '\'' +
                        ",version = '" + version + '\'' +
                        "}";
    }

    public static class FilesItem {

        @SerializedName("projectID")
        private int projectID;

        @SerializedName("required")
        private boolean required;

        @SerializedName("fileID")
        private int fileID;

        public int getProjectID() {
            return projectID;
        }

        public boolean isRequired() {
            return required;
        }

        public int getFileID() {
            return fileID;
        }

        @Override
        public String toString() {
            return
                    "FilesItem{" +
                            "projectID = '" + projectID + '\'' +
                            ",required = '" + required + '\'' +
                            ",fileID = '" + fileID + '\'' +
                            "}";
        }
    }

    public static class Minecraft {

        @SerializedName("version")
        private String version;

        @SerializedName("modLoaders")
        private List<ModLoadersItem> modLoaders;

        public String getVersion() {
            return version;
        }

        public List<ModLoadersItem> getModLoaders() {
            return modLoaders;
        }

        @Override
        public String toString() {
            return
                    "Minecraft{" +
                            "version = '" + version + '\'' +
                            ",modLoaders = '" + modLoaders + '\'' +
                            "}";
        }
    }

    public static class ModLoadersItem {

        @SerializedName("id")
        private String id;

        @SerializedName("primary")
        private boolean primary;

        public String getId() {
            return id;
        }

        public boolean isPrimary() {
            return primary;
        }

        @Override
        public String toString() {
            return
                    "ModLoadersItem{" +
                            "id = '" + id + '\'' +
                            ",primary = '" + primary + '\'' +
                            "}";
        }
    }
}