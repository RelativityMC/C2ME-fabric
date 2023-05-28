package com.ishland.c2me.rewrites.chunk_serializer.common.utils;

import java.security.AccessController;
import java.security.PrivilegedAction;

public class AccessControllerUtils {
    public static <T> T runWithAccessController(PrivilegedAction<T> action) throws IllegalAccessException{
        return AccessController.doPrivileged(action);
    }
}