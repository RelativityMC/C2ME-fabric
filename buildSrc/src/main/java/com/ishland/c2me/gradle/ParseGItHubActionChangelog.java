package com.ishland.c2me.gradle;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.nio.file.Files;
import java.nio.file.Path;

public class ParseGItHubActionChangelog {

    public static String getChangelog() throws Throwable {
        final String path = System.getenv("GITHUB_EVENT_RAW_PATH");
        if (path == null || path.isBlank()) return "No changelog was specified. ";
        final JsonObject jsonObject = new Gson().fromJson(Files.readString(Path.of(path)), JsonObject.class);

        StringBuilder builder = new StringBuilder();
        builder.append("This version is uploaded automatically by GitHub Actions.  \n\n")
                .append("Changelog:  \n");
        final JsonArray commits = jsonObject.getAsJsonArray("commits");
        if (commits.isEmpty()) {
            builder.append("No changes detected. \n");
        } else {
            for (JsonElement commit : commits) {
                JsonObject object = commit.getAsJsonObject();
                builder.append("- ");
                builder.append('[').append(object.get("id").getAsString(), 0, 8).append(']')
                        .append('(').append(object.get("url").getAsString()).append(')');
                builder.append(' ');
                builder.append(object.get("message").getAsString().split("\n")[0]);
                builder.append(" — ");
                builder.append(object.get("author").getAsJsonObject().get("name").getAsString());
                builder.append("  ").append('\n');
            }
        }
        return builder.toString();
    }

}
