package com.ishland.c2me.base.common.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;

public class UrlUtil {

    private UrlUtil() {
    }

    public static URL asUrl(Path path) {
        try {
            return new URL(null, path.toUri().toString());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

}
