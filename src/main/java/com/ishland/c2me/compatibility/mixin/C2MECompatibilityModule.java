package com.ishland.c2me.compatibility.mixin;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.ishland.c2me.common.config.ConfigUtils;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.util.version.SemanticVersionImpl;
import net.fabricmc.loader.util.version.SemanticVersionPredicateParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;

public class C2MECompatibilityModule implements IMixinConfigPlugin {
    private static final Logger LOGGER = LogManager.getLogger("C2ME Compatibility Module");
    private static final String mixinPackage = "com.ishland.c2me.compatibility.mixin.";
    private static final HashSet<String> enabledSubPackages = new HashSet<>();
    private static final HashSet<ModContainer> enabledMods = new HashSet<>();
    private static final AtomicBoolean initialized = new AtomicBoolean(false);

    public static Set<ModContainer> getEnabledMods() {
        return Collections.unmodifiableSet(enabledMods);
    }

    @Override
    public void onLoad(String mixinPackage) {
        if (!initialized.compareAndSet(false, true)) throw new IllegalStateException("Already initialized");
        LOGGER.info("Initializing C2ME Compatibility Module");
        CommentedFileConfig config = CommentedFileConfig.builder(FabricLoader.getInstance().getConfigDir().resolve("c2me-compat.toml"))
                .autosave()
                .preserveInsertionOrder()
                .sync()
                .build();
        config.load();
        final ConfigUtils.ConfigScope configScope = new ConfigUtils.ConfigScope(config);
//        addMixins("terra", ">=5.0.0", "terra", configScope);
//        addMixins("betterend", ">=0.10.2-pre", "betterend", configScope);
        configScope.removeUnusedKeys();
        config.save();
        config.close();
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.startsWith(mixinPackage)) {
            LOGGER.warn("Attempted to call shouldApplyMixin for foreign mixin {}", mixinClassName);
            return false;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        final ArrayList<String> extraMixins = new ArrayList<>();
        for (String subPackage : enabledSubPackages) {
            try (final InputStream in = C2MECompatibilityModule.class.getClassLoader().getResourceAsStream("c2me-compat." + subPackage + ".mixins.json");
                 final InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(in, "Cannot find specified module mixins"))) {
                final JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
                for (JsonElement element : jsonObject.getAsJsonArray("mixins")) {
                    extraMixins.add(subPackage + "." + element.getAsString());
                }

            } catch (Throwable t) {
                LOGGER.warn("Unable to apply compatibility module " + subPackage, t);
            }
        }
        LOGGER.info("Adding mixins: {}", extraMixins.isEmpty() ? "[None]" : "");
        for (String s : extraMixins) {
            LOGGER.info("- {}", s);
        }

        return extraMixins;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    private void addMixins(String modid, String versionRange, String subPackage, ConfigUtils.ConfigScope configScope) {
        FabricLoader.getInstance().getModContainer(modid).ifPresent(modContainer -> {
            try {
                final SemanticVersionImpl modVersion = new SemanticVersionImpl(modContainer.getMetadata().getVersion().getFriendlyString(), false);
                final Predicate<SemanticVersionImpl> versionPredicate = SemanticVersionPredicateParser.create(versionRange);
                if (versionPredicate.test(modVersion)) {
                    if (ConfigUtils.getValue(
                            configScope,
                            subPackage,
                            () -> true,
                            String.format("Compatibility module for %s@%s(%s)", modid, modVersion.getFriendlyString(), versionRange),
                            List.of(),
                            false)) {
                        LOGGER.info("Adding compatibility module for {}@{}({})", modid, modVersion.getFriendlyString(), versionRange);
                        enabledSubPackages.add(subPackage);
                        enabledMods.add(modContainer);
                    } else {
                        LOGGER.info("Not adding compatibility module for {}@{}({}) (disabled in config)", modid, modVersion.getFriendlyString(), versionRange);
                    }
                }
            } catch (Throwable throwable) {
                LOGGER.warn("Cannot add compatibility module for {}({})", modid, versionRange);
            }
        });
    }

}
