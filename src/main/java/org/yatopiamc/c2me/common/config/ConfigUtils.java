package org.yatopiamc.c2me.common.config;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ConfigUtils {

    private static final boolean IGNORE_INCOMPATIBILITY = Boolean.parseBoolean(System.getProperty("org.yatopia.c2me.common.config.ignoreIncompatibility", "false"));

    public static <T> T getValue(ConfigScope config, String key, Supplier<T> deff, String comment, List<String> incompatibleMods, T incompatibleDefault, CheckType... checks) {
        if (IGNORE_INCOMPATIBILITY) C2MEConfig.LOGGER.fatal("Ignoring incompatibility check. You will get NO support if you do this unless explicitly stated. ");
        Preconditions.checkNotNull(config);
        Preconditions.checkNotNull(key);
        Preconditions.checkArgument(!key.isEmpty());
        Preconditions.checkNotNull(deff);
        Preconditions.checkNotNull(incompatibleMods);
        final Set<ModContainer> foundIncompatibleMods = IGNORE_INCOMPATIBILITY ? Collections.emptySet() : incompatibleMods.stream().flatMap(modId -> FabricLoader.getInstance().getModContainer(modId).stream()).collect(Collectors.toSet());
        Supplier<T> def = Suppliers.memoize(deff::get);
        if (!foundIncompatibleMods.isEmpty()) {
            comment = comment + " \n INCOMPATIBILITY: Set to " + incompatibleDefault + " forcefully by: " + String.join(", ", foundIncompatibleMods.stream().map(modContainer -> modContainer.getMetadata().getId()).collect(Collectors.toSet()));

        }
        config.processedKeys.add(key);
        if (!config.config.contains(key) || (checks.length != 0 && Arrays.stream(checks).anyMatch(checkType -> !checkType.check(config.config.get(key)))))
            config.config.set(key, def.get());
        if (def.get() instanceof Config) config.config.setComment(key, String.format(" %s", comment));
        else config.config.setComment(key, String.format(" (Default: %s) %s", def.get(), comment));
        return foundIncompatibleMods.isEmpty() ? Objects.requireNonNull(config.config.get(key)) : incompatibleDefault;
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
