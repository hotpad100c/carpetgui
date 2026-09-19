package ml.mypals.carpetgui.translate;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import ml.mypals.carpetgui.CarpetGUI;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.util.GsonHelper;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TranslationHelper {
   public static final String BASE_RULE_NAMESPACE = "%s.rule.";
   public static final String BASE_RULE_PATTERN = "%s.rule.%s.";
   public static final String RULE_NAME_PATTERN = "%s.rule.%s.name";
   public static final String RULE_DESC_PATTERN = "%s.rule.%s.desc";
   public static final String RULE_EXTRA_PREFIX_PATTERN = "%s.rule.%s.extra.";
   public static final String CATEGORY_PATTERN = "%s.category.%s";
   private static final Map<String, Map<String, String>> TRANSLATION_CACHE = new HashMap();

   public static String getCategoryTranslation(String lang, String manager, @NotNull String originalName) {
      String key = "%s.category.%s".formatted(manager, originalName);
      String baseName = resolveBaseName(key, originalName);

      try {
         Map<String, String> translations = getTranslations(lang);
         String translation = (String)translations.get(key);
         if (translation != null && !translation.equals(baseName)) {
            return translation;
         }
      } catch (Exception var7) {
      }

      return baseName;
   }

   public static String getNameTranslation(String lang, String manager, @NotNull String originalName) {
      String key = "%s.rule.%s.name".formatted(manager, originalName);
      String baseName = resolveBaseName(key, originalName);

      try {
         Map<String, String> translations = getTranslations(lang);
         String translation = (String)translations.get(key);
         if (translation != null && !translation.equals(baseName)) {
            return translation;
         }
      } catch (Exception var7) {
      }

      return baseName;
   }

   public static String getDescTranslation(String lang, String manager, @NotNull String originalName) {
      String key = "%s.rule.%s.desc".formatted(manager, originalName);
      String extraKey = "%s.rule.%s.extra.".formatted(manager, originalName);

      try {
         Map<String, String> translations = getTranslations(lang);
         String translation = (String)translations.get(key);
         String extras = collectExtras(translations, extraKey);
         String full = translation == null ? "" : translation;
         if (!extras.isEmpty()) {
            full = full.isEmpty() ? extras : full + "\n" + extras;
         }

         return full;
      } catch (Exception var9) {
         return "";
      }
   }

   private static String collectExtras(Map<String, String> translations, String prefix) {
      StringBuilder sb = new StringBuilder();
      int i = 0;

      while(true) {
         String val = (String)translations.get(prefix + i);
         if (val == null) {
            return sb.toString();
         }

         if (!sb.isEmpty()) {
         }

         sb.append(" > ").append(val).append("\n");
         ++i;
      }
   }

   public static void clearCache() {
      TRANSLATION_CACHE.clear();
   }

   private static String resolveBaseName(String key, @Nullable String originalName) {
      if (originalName != null) {
         return originalName;
      } else {
         try {
            String enTranslation = (String)getTranslations("en_us").get(key);
            if (enTranslation != null) {
               return enTranslation;
            }
         } catch (Exception var3) {
         }

         return key.replace("rule.", "").replace(".name", "").replace(".desc", "");
      }
   }

   private static Map<String, String> getTranslations(String lang) {
      if (TRANSLATION_CACHE.containsKey(lang)) {
         return (Map)TRANSLATION_CACHE.get(lang);
      } else {
         Map<String, String> translations = new HashMap();
         String carpetPath = String.format("assets/carpet/lang/%s.json", lang);
         loadTranslationsFromPath("carpet", carpetPath, translations);

         for(CarpetExtension ext : CarpetServer.extensions) {
            Map<String, String> extMappings = ext.canHasTranslations(lang);
            if (extMappings != null) {
               boolean warned = false;

               for(Map.Entry<String, String> entry : extMappings.entrySet()) {
                  String key = (String)entry.getKey();
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

                  translations.putIfAbsent(key, (String)entry.getValue());
               }
            }
         }

         translations.keySet().removeIf((keyx) -> keyx.startsWith("//"));
         TRANSLATION_CACHE.put(lang, translations);
         return translations;
      }
   }

   private static void loadTranslationsFromPath(String namespace, String path, Map<String, String> translations) {
      try {
         Path resourcePath = Objects.requireNonNull(FabricLoader.getInstance().getModContainer(namespace).orElse(null)).findPath(path).orElse(null);
         if (resourcePath != null && Files.exists(resourcePath, new LinkOption[0])) {
            String jsonContent = Files.readString(resourcePath, StandardCharsets.UTF_8);
            JsonObject jsonObject = (JsonObject)CarpetGUI.GSON.fromJson(jsonContent, JsonObject.class);
            jsonObject.entrySet().forEach((entry) -> {
               if (!((String)entry.getKey()).startsWith("//")) {
                  translations.putIfAbsent((String)entry.getKey(), GsonHelper.getAsString(jsonObject, (String)entry.getKey(), (String)entry.getKey()));
               }

            });
         } else {
            InputStream inputStream = CarpetGUI.class.getClassLoader().getResourceAsStream(path);

            try {
               if (inputStream != null) {
                  String jsonContent = IOUtils.toString(inputStream, StandardCharsets.UTF_8);
                  JsonObject jsonObject = (JsonObject)CarpetGUI.GSON.fromJson(jsonContent, JsonObject.class);
                  jsonObject.entrySet().forEach((entry) -> {
                     if (!((String)entry.getKey()).startsWith("//")) {
                        translations.putIfAbsent((String)entry.getKey(), GsonHelper.getAsString(jsonObject, (String)entry.getKey(), (String)entry.getKey()));
                     }

                  });
               }
            } catch (Throwable var8) {
               if (inputStream != null) {
                  try {
                     inputStream.close();
                  } catch (Throwable var7) {
                     var8.addSuppressed(var7);
                  }
               }

               throw var8;
            }

            if (inputStream != null) {
               inputStream.close();
            }
         }
      } catch (IOException e) {
         CarpetGUI.LOGGER.warn("Failed to load language file: {} for namespace: {}, error: {}", new Object[]{path, namespace, e.getMessage()});
      }

   }
}
