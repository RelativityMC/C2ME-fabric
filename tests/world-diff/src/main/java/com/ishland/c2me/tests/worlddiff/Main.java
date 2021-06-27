package com.ishland.c2me.tests.worlddiff;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class Main {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void main() {
        try {
            System.out.println("Starting WorldDiff");
            final String worldString = System.getProperty("com.ishland.c2me.tests.worlddiff.worlds", "");
            for (String instance : worldString.split(";")) {
                final String[] folders = instance.split(",");
                if (folders.length != 2) {
                    LOGGER.error("Failed to parse system property");
                    return;
                }
                File from = new File(folders[0]);
                File to = new File(folders[1]);
                if (!from.isDirectory()) {
                    LOGGER.error("World not present: {}", from);
                    return;
                }
                if (!to.isDirectory()) {
                    LOGGER.error("World not present: {}", to);
                    return;
                }
                new ComparisonSession(from, to);
            }
        } catch (Throwable t) {
            LOGGER.error("Unexpected exception thrown while testing", t);
        }
    }

}
