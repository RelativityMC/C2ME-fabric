package org.yatopiamc.c2me.common.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Supplier;

public class ConfigUtils {

    public static <T> T getValue(CommentedConfig config, String key, Supplier<T> def, String comment, CheckType... checks) {
        Preconditions.checkNotNull(config);
        Preconditions.checkNotNull(key);
        Preconditions.checkArgument(!key.isEmpty());
        Preconditions.checkNotNull(def);
        if (!config.contains(key) || (checks.length != 0 && Arrays.stream(checks).anyMatch(checkType -> !checkType.check(config.get(key))))) config.set(key, def.get());
        config.setComment(key, " " + comment);
        return Objects.requireNonNull(config.get(key));
    }

    public enum CheckType {
        THREAD_COUNT {
            @Override
            public <T> boolean check(T value) {
                return value instanceof Number && ((Number) value).intValue() >= 1 && ((Number) value).intValue() <= 0x7fff;
            }
        };

        public abstract <T> boolean check(T value);
    }

}
