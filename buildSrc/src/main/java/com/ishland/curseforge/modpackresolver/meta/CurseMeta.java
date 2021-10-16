package com.ishland.curseforge.modpackresolver.meta;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class CurseMeta {

    @SerializedName("ReleaseType")
    private int releaseType;

    @SerializedName("GameVersionDateReleased")
    private String gameVersionDateReleased;

    @SerializedName("_Project")
    private Project project;

    @SerializedName("FileStatus")
    private int fileStatus;

    @SerializedName("ServerPackFileId")
    private int serverPackFileId;

    @SerializedName("__comment")
    private String comment;

    @SerializedName("Modules")
    private List<ModulesItem> modules;

    @SerializedName("FileName")
    private String fileName;

    @SerializedName("InstallMetadata")
    private Object installMetadata;

    @SerializedName("AlternateFileId")
    private int alternateFileId;

    @SerializedName("IsAlternate")
    private boolean isAlternate;

    @SerializedName("FileLength")
    private int fileLength;

    @SerializedName("GameVersion")
    private List<String> gameVersion;

    @SerializedName("FileDate")
    private String fileDate;

    @SerializedName("PackageFingerprint")
    private long packageFingerprint;

    @SerializedName("FileNameOnDisk")
    private String fileNameOnDisk;

    @SerializedName("HasInstallScript")
    private boolean hasInstallScript;

    @SerializedName("GameVersionFlavor")
    private Object gameVersionFlavor;

    @SerializedName("IsAvailable")
    private boolean isAvailable;

    @SerializedName("DisplayName")
    private String displayName;

    @SerializedName("Dependencies")
    private List<Object> dependencies;

    @SerializedName("Id")
    private int id;

    @SerializedName("DownloadURL")
    private String downloadURL;

    public int getReleaseType() {
        return releaseType;
    }

    public String getGameVersionDateReleased() {
        return gameVersionDateReleased;
    }

    public Project getProject() {
        return project;
    }

    public int getFileStatus() {
        return fileStatus;
    }

    public int getServerPackFileId() {
        return serverPackFileId;
    }

    public String getComment() {
        return comment;
    }

    public List<ModulesItem> getModules() {
        return modules;
    }

    public String getFileName() {
        return fileName;
    }

    public Object getInstallMetadata() {
        return installMetadata;
    }

    public int getAlternateFileId() {
        return alternateFileId;
    }

    public boolean isIsAlternate() {
        return isAlternate;
    }

    public int getFileLength() {
        return fileLength;
    }

    public List<String> getGameVersion() {
        return gameVersion;
    }

    public String getFileDate() {
        return fileDate;
    }

    public long getPackageFingerprint() {
        return packageFingerprint;
    }

    public String getFileNameOnDisk() {
        return fileNameOnDisk;
    }

    public boolean isHasInstallScript() {
        return hasInstallScript;
    }

    public Object getGameVersionFlavor() {
        return gameVersionFlavor;
    }

    public boolean isIsAvailable() {
        return isAvailable;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<Object> getDependencies() {
        return dependencies;
    }

    public int getId() {
        return id;
    }

    public String getDownloadURL() {
        return downloadURL;
    }

    @Override
    public String toString() {
        return
                "CurseMeta{" +
                        "releaseType = '" + releaseType + '\'' +
                        ",gameVersionDateReleased = '" + gameVersionDateReleased + '\'' +
                        ",_Project = '" + project + '\'' +
                        ",fileStatus = '" + fileStatus + '\'' +
                        ",serverPackFileId = '" + serverPackFileId + '\'' +
                        ",__comment = '" + comment + '\'' +
                        ",modules = '" + modules + '\'' +
                        ",fileName = '" + fileName + '\'' +
                        ",installMetadata = '" + installMetadata + '\'' +
                        ",alternateFileId = '" + alternateFileId + '\'' +
                        ",isAlternate = '" + isAlternate + '\'' +
                        ",fileLength = '" + fileLength + '\'' +
                        ",gameVersion = '" + gameVersion + '\'' +
                        ",fileDate = '" + fileDate + '\'' +
                        ",packageFingerprint = '" + packageFingerprint + '\'' +
                        ",fileNameOnDisk = '" + fileNameOnDisk + '\'' +
                        ",hasInstallScript = '" + hasInstallScript + '\'' +
                        ",gameVersionFlavor = '" + gameVersionFlavor + '\'' +
                        ",isAvailable = '" + isAvailable + '\'' +
                        ",displayName = '" + displayName + '\'' +
                        ",dependencies = '" + dependencies + '\'' +
                        ",id = '" + id + '\'' +
                        ",downloadURL = '" + downloadURL + '\'' +
                        "}";
    }

    public static class ModulesItem {

        @SerializedName("Fingerprint")
        private long fingerprint;

        @SerializedName("Foldername")
        private String foldername;

        public long getFingerprint() {
            return fingerprint;
        }

        public String getFoldername() {
            return foldername;
        }

        @Override
        public String toString() {
            return
                    "ModulesItem{" +
                            "fingerprint = '" + fingerprint + '\'' +
                            ",foldername = '" + foldername + '\'' +
                            "}";
        }
    }

    public static class Project {

        @SerializedName("Path")
        private String path;

        @SerializedName("PackageType")
        private String packageType;

        public String getPath() {
            return path;
        }

        public String getPackageType() {
            return packageType;
        }

        @Override
        public String toString() {
            return
                    "Project{" +
                            "path = '" + path + '\'' +
                            ",packageType = '" + packageType + '\'' +
                            "}";
        }
    }
}