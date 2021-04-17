package org.yatopiamc.c2me.common.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ConfigUtils {

    public static <T> T getValue(ConfigScope config, String key, Supplier<T> deff, String comment, CheckType... checks) {
        Preconditions.checkNotNull(config);
        Preconditions.checkNotNull(key);
        Preconditions.checkArgument(!key.isEmpty());
        Preconditions.checkNotNull(deff);
        Supplier<T> def = Suppliers.memoize(deff);
        config.processedKeys.add(key);
        if (!config.config.contains(key) || (checks.length != 0 && Arrays.stream(checks).anyMatch(checkType -> !checkType.check(config.config.get(key)))))
            config.config.set(key, def.get());
        if (def.get() instanceof Config) config.config.setComment(key, String.format(" %s", comment));
        else config.config.setComment(key, String.format(" (Default: %s) %s", def.get(), comment));
        return Objects.requireNonNull(config.config.get(key));
    }

    static class ConfigScope {
        final CommentedConfig config;
        final Set<String> processedKeys;

        ConfigScope(CommentedConfig config) {
            this.config = config;
            this.processedKeys = new HashSet<>();
        }

        void removeUnusedKeys() {
            config.entrySet().removeIf(entry -> !processedKeys.contains(entry.getKey()));
        }
    }

    public enum CheckType {
        THREAD_COUNT() {
            @Override
            public <T> boolean check(T value) {
                return value instanceof Number && ((Number) value).intValue() >= 1 && ((Number) value).intValue() <= 0x7fff;
            }
        };

        public abstract <T> boolean check(T value);
    }

}
