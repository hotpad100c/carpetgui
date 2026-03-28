package ml.mypals.carpetgui.translate;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.utils.TranslationKeys;
import carpet.utils.Translations;
import com.google.gson.JsonObject;
import ml.mypals.carpetgui.CarpetGUI;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.fabricmc.fabric.impl.resource.loader.ModResourcePackUtil.GSON;

public class TranslationHelper {
    private static final Map<String, Map<String, String>> TRANSLATION_CACHE = new HashMap<>();

    public static String getCategoryTranslation(String lang,String manager, @NotNull String originalName) {
        String key = TranslationKeys.CATEGORY_PATTERN.formatted(manager, originalName);
        String baseName = resolveBaseName(key, originalName);
        try {
            Map<String, String> translations = getTranslations(lang);
            String translation = translations.get(key);
            if (translation != null && !translation.equals(baseName)) {
                return translation;
            }
        } catch (Exception ignored) {}

        return baseName;
    }
    public static String getNameTranslation(String lang, String manager, @NotNull String originalName) {
        String key = TranslationKeys.RULE_NAME_PATTERN.formatted(manager, originalName);
        String baseName = resolveBaseName(key, originalName);
        try {
            Map<String, String> translations = getTranslations(lang);
            String translation = translations.get(key);
            if (translation != null && !translation.equals(baseName)) {
                return translation;
            }
        } catch (Exception ignored) {}

        return baseName;
    }

    public static String getDescTranslation(String lang, String manager, @NotNull String originalName) {
        String key = TranslationKeys.RULE_DESC_PATTERN.formatted(manager, originalName);
        String extraKey = TranslationKeys.RULE_EXTRA_PREFIX_PATTERN.formatted(manager, originalName);
        try {
            Map<String, String> translations = getTranslations(lang);
            String translation = translations.get(key);
            String extras = collectExtras(translations, extraKey);

            String full = translation == null ? "" : translation;
            if (!extras.isEmpty()) full = full.isEmpty() ? extras : full + "\n" + extras;
            return full;

        } catch (Exception ignored) {}

        return "";
    }
    private static String collectExtras(Map<String, String> translations, String prefix) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; ; i++) {
            String val = translations.get(prefix + i);
            if (val == null) {
                break;
            }
            if (!sb.isEmpty()) ;
            sb.append(" > ").append(val).append("\n");
        }
        return sb.toString();
    }
    public static void clearCache() {
        TRANSLATION_CACHE.clear();
    }

    private static String resolveBaseName(String key, @Nullable String originalName) {
        if (originalName != null) return originalName;
        try {
            String enTranslation = getTranslations("en_us").get(key);
            if (enTranslation != null) return enTranslation;
        } catch (Exception ignored) {}
        return key.replace("rule.", "").replace(".name", "").replace(".desc", "");
    }
    private static Map<String, String> getTranslations(String lang) {
        String cacheKey = lang;
        if (TRANSLATION_CACHE.containsKey(cacheKey)) {
            return TRANSLATION_CACHE.get(cacheKey);
        }

        Map<String, String> translations = new HashMap<>();

        String carpetPath = String.format("assets/carpet/lang/%s.json", lang);
        loadTranslationsFromPath("carpet", carpetPath, translations);

        for (CarpetExtension ext : CarpetServer.extensions) {
            Map<String, String> extMappings = ext.canHasTranslations(lang);
            if (extMappings == null) {
                continue;
            }

            boolean warned = false;
            for (Map.Entry<String, String> entry : extMappings.entrySet()) {
                String key = entry.getKey();
                if (!key.startsWith("carpet.")) {
                    if (key.startsWith("rule.")) {
                        key = "carpet.rule." + key.substring(5);
                    } else if (key.startsWith("category.")) {
                        key = "carpet.category." + key.substring(9);
                    }
                    if (!warned && !key.equals(entry.getKey())) {
                        warned = true;
                    }
                }
                translations.putIfAbsent(key, entry.getValue());
            }
        }
        translations.keySet().removeIf(key -> {
            if (key.startsWith("//")) {
                return true;
            }
            return false;
        });

        TRANSLATION_CACHE.put(cacheKey, translations);
        return translations;
    }

    private static void loadTranslationsFromPath(String namespace, String path, Map<String, String> translations) {
        try {
            Path resourcePath = FabricLoader.getInstance()
                    .getModContainer(namespace)
                    .orElse(null)
                    .findPath(path)
                    .orElse(null);

            if (resourcePath != null && Files.exists(resourcePath)) {
                String jsonContent = Files.readString(resourcePath, StandardCharsets.UTF_8);
                JsonObject jsonObject = GSON.fromJson(jsonContent, JsonObject.class);
                jsonObject.entrySet().forEach(entry -> {
                    if (!entry.getKey().startsWith("//")) {
                        translations.putIfAbsent(entry.getKey(), GsonHelper.getAsString(jsonObject, entry.getKey(), entry.getKey()));
                    }
                });
            } else {
                try (InputStream inputStream = CarpetGUI.class.getClassLoader().getResourceAsStream(path)) {
                    if (inputStream != null) {
                        String jsonContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                        JsonObject jsonObject = GSON.fromJson(jsonContent, JsonObject.class);
                        jsonObject.entrySet().forEach(entry -> {
                            if (!entry.getKey().startsWith("//")) {
                                translations.putIfAbsent(entry.getKey(), GsonHelper.getAsString(jsonObject, entry.getKey(), entry.getKey()));
                            }
                        });
                    }
                }
            }
        } catch (IOException e) {
            CarpetGUI.LOGGER.warn("Failed to load language file: {} for namespace: {}, error: {}", path, namespace, e.getMessage());
        }
    }
}
