package com.ishland.c2me.common.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;
import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ConfigUtils {

    private static final boolean IGNORE_INCOMPATIBILITY = Boolean.parseBoolean(System.getProperty("com.ishland.c2me.common.config.ignoreIncompatibility", "false"));

    public static <T> T getValue(ConfigScope config, String key, Supplier<T> def, String comment, List<String> incompatibleMods, T incompatibleDefault, boolean usePlaceholder, CheckType... checks) {
        return getValue0(config, key, Suppliers.memoize(def::get), comment, incompatibleMods, incompatibleDefault, usePlaceholder, checks);
    }

    private static <T> T getValue0(ConfigScope config, String key, Supplier<T> def, String comment, List<String> incompatibleMods, T incompatibleDefault, boolean usePlaceholder, CheckType... checks) {
        if (IGNORE_INCOMPATIBILITY) C2MEConfig.LOGGER.fatal("Ignoring incompatibility check. You will get NO support if you do this unless explicitly stated. ");
        Preconditions.checkNotNull(config);
        Preconditions.checkNotNull(key);
        Preconditions.checkArgument(!key.isEmpty());
        Preconditions.checkNotNull(def);
        Preconditions.checkNotNull(incompatibleMods);
        final Set<ModContainer> foundIncompatibleMods = IGNORE_INCOMPATIBILITY ? Collections.emptySet() : incompatibleMods.stream().flatMap(modId -> FabricLoader.getInstance().getModContainer(modId).stream()).collect(Collectors.toSet());
        if (!foundIncompatibleMods.isEmpty()) {
            comment = comment + " \n INCOMPATIBILITY: Set to " + incompatibleDefault + " forcefully by: " + String.join(", ", foundIncompatibleMods.stream().map(modContainer -> modContainer.getMetadata().getId()).collect(Collectors.toSet()));
        }
        config.processedKeys.add(key);
        Object originalValue = config.config.get(key);
        //noinspection SimplifiableConditionalExpression
        if (!config.config.contains(key) || ((usePlaceholder ? !originalValue.equals("default") : true) && checks.length != 0 && Arrays.stream(checks).anyMatch(checkType -> !checkType.check(originalValue))))
            config.config.set(key, def.get() instanceof Config || !usePlaceholder ? def.get() : "default");
        if (def.get() instanceof Config) config.config.setComment(key, String.format(" %s", comment));
        else config.config.setComment(key, String.format(" (Default: %s) %s", def.get(), comment));
        Object configuredValue = getConfiguredValue(config.config.get(key), def);
        if (!(configuredValue.getClass().isAssignableFrom(def.get().getClass()))) {
            C2MEConfig.LOGGER.warn("Configured value for {} is of type {} but expected type is {}", key, configuredValue.getClass(), def.get().getClass());
            boolean dataFixed = false;
            try {
                if (Boolean.class.isAssignableFrom(def.get().getClass())) {
                    Boolean bool = Boolean.parseBoolean(configuredValue.toString());
                    if (bool != null) {
                        configuredValue = bool;
                        dataFixed = true;
                    }
                } else if (Integer.class.isAssignableFrom(def.get().getClass())) {
                    Integer integer = Integer.parseInt(configuredValue.toString());
                    if (integer != null) {
                        configuredValue = integer;
                        dataFixed = true;
                    }
                }
            } catch (Exception ignored) {}

            if (!dataFixed) {
                C2MEConfig.LOGGER.warn("No fix could be applied to the configured value for {}. Resetting value to the default: {}", key, def.get());
                configuredValue = def.get();
                config.config.set(key, def.get() instanceof Config || !usePlaceholder ? def.get() : "default");
            } else {
                C2MEConfig.LOGGER.warn("Fixed the configured value for {} by converting it to type {}", key, configuredValue.getClass());
                if (configuredValue == def.get()) {
                    config.config.set(key, def.get() instanceof Config || !usePlaceholder ? def.get() : "default");
                } else {
                    config.config.set(key, configuredValue);
                }
            }
        }
        return foundIncompatibleMods.isEmpty() ? Objects.requireNonNull((configuredValue.equals("default")) ? def.get() : (T) configuredValue) : incompatibleDefault;
    }

    @SuppressWarnings("unchecked")
    private static <T> T getConfiguredValue(Object o, Supplier<T> def) {
        if (o.equals("default")) return def.get();
        try {
            return (T) o;
        } catch (ClassCastException e) {
            return def.get();
        }
    }

    public static CommentedConfig config() {
        return CommentedConfig.of(LinkedHashMap::new, InMemoryCommentedFormat.defaultInstance());
    }

    public static class ConfigScope {
        final CommentedConfig config;
        final Set<String> processedKeys;

        public ConfigScope(CommentedConfig config) {
            this.config = config;
            this.processedKeys = new HashSet<>();
        }

        public void removeUnusedKeys() {
            config.entrySet().removeIf(entry -> !processedKeys.contains(entry.getKey()));
        }
    }

    public enum CheckType {
        THREAD_COUNT() {
            @Override
            public <T> boolean check(T value) {
                return value instanceof Number && ((Number) value).intValue() >= 1 && ((Number) value).intValue() <= 0x7fff;
            }
        },
        NO_TICK_VIEW_DISTANCE() {
            @Override
            public <T> boolean check(T value) {
                return value instanceof Number && ((Number) value).intValue() >= 2 && ((Number) value).intValue() <= 248;
            }
        },
        POSITIVE_VALUE_ONLY() {
            @Override
            public <T> boolean check(T value) {
                return value instanceof Number && ((Number) value).intValue() >= 1;
            }
        };

        public abstract <T> boolean check(T value);
    }

}
