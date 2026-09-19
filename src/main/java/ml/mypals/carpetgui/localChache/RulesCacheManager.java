package ml.mypals.carpetgui.localChache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import ml.mypals.carpetgui.network.RuleData;
import ml.mypals.carpetgui.settings.CarpetGUIConfigManager;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RulesCacheManager {
   private static final Logger LOGGER = LoggerFactory.getLogger("carpetgui-cache");
   private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().create();
   private static final Path CACHE_DIR;
   private static final Path KNOWN_MANAGERS_FILE;
   private static final Path RULES_FILE;

   public static void saveCache(List<RuleData> newRules, String defaults, String currentLanguage) {
      try {
         Files.createDirectories(CACHE_DIR);
         Map<String, JsonObject> oldLocalNames = new HashMap();
         Map<String, JsonObject> oldLocalDescs = new HashMap();
         Map<String, JsonObject> oldCategoryValues = new LinkedHashMap();
         List<RuleData> oldRules = List.of();
         String mergedDefaults = defaults;
         Path file = RULES_FILE;
         if (Files.exists(file, new LinkOption[0])) {
            try {
               Reader r = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8);

               try {
                  JsonObject oldRoot = (JsonObject)GSON.fromJson(r, JsonObject.class);
                  if ((defaults == null || defaults.isBlank()) && oldRoot.has("defaults")) {
                     mergedDefaults = oldRoot.get("defaults").getAsString();
                  }

                  JsonArray oldCatsArr = oldRoot.getAsJsonArray("categories");
                  if (oldCatsArr != null) {
                     for(JsonElement el : oldCatsArr) {
                        JsonObject obj = el.getAsJsonObject();
                        String key = obj.get("key").getAsString();
                        JsonElement val = obj.get("value");
                        if (val != null && val.isJsonObject()) {
                           oldCategoryValues.put(key, val.getAsJsonObject().deepCopy());
                        }
                     }
                  }

                  JsonArray oldRulesArr = oldRoot.getAsJsonArray("rules");
                  if (oldRulesArr != null) {
                     for(JsonElement el : oldRulesArr) {
                        JsonObject obj = el.getAsJsonObject();
                        String name = obj.get("name").getAsString();
                        extractLocaleMap(obj, "localName", name, oldLocalNames);
                        extractLocaleMap(obj, "localDescription", name, oldLocalDescs);
                     }

                     oldRules = deserializeRules(oldRulesArr, oldCategoryValues, currentLanguage);
                  }
               } catch (Throwable var21) {
                  try {
                     r.close();
                  } catch (Throwable var20) {
                     var21.addSuppressed(var20);
                  }

                  throw var21;
               }

               r.close();
            } catch (Exception ex) {
               LOGGER.warn("Could not read old cache for incremental merge: {}", ex.getMessage());
            }
         }

         Map<String, RuleData> ruleMap = new LinkedHashMap();

         for(RuleData old : oldRules) {
            if (old.name != null && !old.name.isBlank()) {
               ruleMap.put(old.name, old);
            }
         }

         for(RuleData neu : newRules) {
            if (neu.name != null && !neu.name.isBlank()) {
               ruleMap.put(neu.name, neu);
            }
         }

         List<RuleData> mergedRules = new ArrayList(ruleMap.values());

         for(RuleData rule : newRules) {
            for(Map.Entry<String, String> entry : rule.categories) {
               String catKey = (String)entry.getKey();
               String localVal = (String)entry.getValue();
               JsonObject langMap = (JsonObject)oldCategoryValues.computeIfAbsent(catKey, (k) -> new JsonObject());
               langMap.addProperty(currentLanguage, localVal);
            }
         }

         JsonObject root = new JsonObject();
         root.addProperty("defaults", mergedDefaults);
         root.add("rules", serializeRules(mergedRules, oldLocalNames, oldLocalDescs, currentLanguage));
         root.add("categories", serializeTopLevelCategories(oldCategoryValues));
         Writer w = new OutputStreamWriter(Files.newOutputStream(RULES_FILE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), StandardCharsets.UTF_8);

         try {
            GSON.toJson(root, w);
         } catch (Throwable var19) {
            try {
               w.close();
            } catch (Throwable var18) {
               var19.addSuppressed(var18);
            }

            throw var19;
         }

         w.close();
         Set<String> managers = (Set)mergedRules.stream().map((rx) -> rx.manager).filter((m) -> m != null && !m.isBlank()).collect(Collectors.toCollection(LinkedHashSet::new));
         saveKnownManagers(managers);
      } catch (Exception ex) {
         LOGGER.error("Failed to save cache !", ex);
      }

   }

   public static Optional<CacheResult> loadCache(String currentLanguage) {
      Path file = RULES_FILE;
      if (!Files.exists(file, new LinkOption[0])) {
         return Optional.empty();
      } else {
         try {
            Reader r = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8);

            Optional var16;
            try {
               JsonObject root = (JsonObject)GSON.fromJson(r, JsonObject.class);
               String defaults = root.has("defaults") ? root.get("defaults").getAsString() : "";
               Map<String, JsonObject> categoryValues = new LinkedHashMap();
               JsonArray catsArr = root.getAsJsonArray("categories");
               if (catsArr != null) {
                  for(JsonElement el : catsArr) {
                     JsonObject obj = el.getAsJsonObject();
                     String key = obj.get("key").getAsString();
                     JsonElement val = obj.get("value");
                     if (val != null && val.isJsonObject()) {
                        categoryValues.put(key, val.getAsJsonObject());
                     }
                  }
               }

               List<RuleData> rules = deserializeRules(root.getAsJsonArray("rules"), categoryValues, currentLanguage);
               var16 = Optional.of(new CacheResult(rules, defaults));
            } catch (Throwable var13) {
               try {
                  r.close();
               } catch (Throwable var12) {
                  var13.addSuppressed(var12);
               }

               throw var13;
            }

            r.close();
            return var16;
         } catch (Exception ex) {
            LOGGER.error("Failed to load cache !", ex);
            return Optional.empty();
         }
      }
   }

   public static void saveRawCache(RawCacheData rawCache, Collection<CachedRuleEntry> mergedRules, String defaults) {
      try {
         Files.createDirectories(CACHE_DIR);
         JsonObject root = new JsonObject();
         root.addProperty("defaults", defaults == null ? "" : defaults);
         JsonArray rulesArr = new JsonArray();

         for(CachedRuleEntry entry : mergedRules) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", entry.name);
            obj.addProperty("type", entry.type);
            obj.addProperty("description", entry.description);
            obj.addProperty("defaultValue", entry.defaultValue);
            obj.addProperty("isGamerule", entry.isGamerule);
            obj.addProperty("manager", entry.manager);
            JsonObject localNameObj = new JsonObject();
            entry.localName.forEach((k, v) -> localNameObj.addProperty((String) k, (String) v));
            obj.add("localName", localNameObj);
            JsonObject localDescObj = new JsonObject();
            entry.localDescription.forEach((k, v) -> localDescObj.addProperty((String) k, (String) v));
            obj.add("localDescription", localDescObj);
            JsonArray suggs = new JsonArray();
            entry.suggestions.forEach(s -> suggs.add((String) s));
            obj.add("suggestions", suggs);
            JsonArray cats = new JsonArray();
            entry.categories.forEach(c -> cats.add((String) c));
            obj.add("categories", cats);
            rulesArr.add(obj);
         }

         root.add("rules", rulesArr);
         JsonArray catsArr = new JsonArray();

         for(CachedCategoryEntry catEntry : rawCache.categories) {
            JsonObject obj = new JsonObject();
            obj.addProperty("key", catEntry.key);
            JsonObject valObj = new JsonObject();
            catEntry.value.forEach((k, v) -> valObj.addProperty((String) k, (String) v));
            obj.add("value", valObj);
            catsArr.add(obj);
         }

         root.add("categories", catsArr);
         Writer w = new OutputStreamWriter(Files.newOutputStream(RULES_FILE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), StandardCharsets.UTF_8);

         try {
            GSON.toJson(root, w);
         } catch (Throwable var13) {
            try {
               w.close();
            } catch (Throwable var12) {
               var13.addSuppressed(var12);
            }

            throw var13;
         }

         w.close();
         Set<String> managers = (Set)mergedRules.stream().map((e) -> e.manager).filter((m) -> m != null && !m.isBlank()).collect(Collectors.toCollection(LinkedHashSet::new));
         saveKnownManagers(managers);
      } catch (Exception ex) {
         LOGGER.error("Failed to save raw cache!", ex);
      }

   }

   public static RawCacheData loadRawCache() {
      try {
         if (!Files.exists(RULES_FILE, new LinkOption[0])) {
            Files.createDirectories(CACHE_DIR);
            Files.createFile(RULES_FILE);
         }

         Reader r = new InputStreamReader(Files.newInputStream(RULES_FILE), StandardCharsets.UTF_8);
         JsonObject root = (JsonObject)GSON.fromJson(r, JsonObject.class);
         RawCacheData data = new RawCacheData();
         data.defaults = root.has("defaults") ? root.get("defaults").getAsString() : "";
         data.categories = new ArrayList();
         JsonArray catsArr = root.getAsJsonArray("categories");
         if (catsArr != null) {
            for(JsonElement el : catsArr) {
               JsonObject obj = el.getAsJsonObject();
               CachedCategoryEntry entry = new CachedCategoryEntry();
               entry.key = obj.get("key").getAsString();
               entry.value = new HashMap();
               JsonObject valObj = obj.getAsJsonObject("value");
               if (valObj != null) {
                  for(Map.Entry<String, JsonElement> e : valObj.entrySet()) {
                     entry.value.put((String)e.getKey(), ((JsonElement)e.getValue()).getAsString());
                  }
               }

               data.categories.add(entry);
            }
         }

         data.rules = new ArrayList();
         JsonArray rulesArr = root.getAsJsonArray("rules");
         if (rulesArr != null) {
            for(JsonElement el : rulesArr) {
               JsonObject obj = el.getAsJsonObject();
               CachedRuleEntry entry = new CachedRuleEntry();
               entry.name = obj.get("name").getAsString();
               entry.type = obj.get("type").getAsString();
               entry.description = obj.get("description").getAsString();
               entry.defaultValue = obj.get("defaultValue").getAsString();
               entry.isGamerule = obj.get("isGamerule").getAsBoolean();
               entry.manager = obj.get("manager").getAsString();
               entry.suggestions = new ArrayList();
               obj.getAsJsonArray("suggestions").forEach((s) -> entry.suggestions.add(s.getAsString()));
               entry.categories = new ArrayList();
               obj.getAsJsonArray("categories").forEach((c) -> entry.categories.add(c.getAsString()));
               entry.localName = new HashMap();
               JsonElement localNameEl = obj.get("localName");
               if (localNameEl != null && localNameEl.isJsonObject()) {
                  for(Map.Entry<String, JsonElement> e : localNameEl.getAsJsonObject().entrySet()) {
                     entry.localName.put((String)e.getKey(), ((JsonElement)e.getValue()).getAsString());
                  }
               }

               entry.localDescription = new HashMap();
               JsonElement localDescEl = obj.get("localDescription");
               if (localDescEl != null && localDescEl.isJsonObject()) {
                  for(Map.Entry<String, JsonElement> e : localDescEl.getAsJsonObject().entrySet()) {
                     entry.localDescription.put((String)e.getKey(), ((JsonElement)e.getValue()).getAsString());
                  }
               }

               data.rules.add(entry);
            }
         }

         return data;
      } catch (Exception ex) {
         LOGGER.error("Failed to load raw cache!", ex);
         return null;
      }
   }

   public static void saveKnownManagers(Set<String> newManagers) {
      try {
         if (!Files.exists(KNOWN_MANAGERS_FILE, new LinkOption[0])) {
            Files.createDirectories(CACHE_DIR);
            Files.createFile(KNOWN_MANAGERS_FILE);
         }

         Set<String> merged = new LinkedHashSet(loadKnownManagers());
         merged.addAll(newManagers);
         JsonArray arr = new JsonArray();

         for(String m : merged) {
            arr.add(m);
         }

         JsonObject root = new JsonObject();
         root.add("managers", arr);
         Writer w = new OutputStreamWriter(Files.newOutputStream(KNOWN_MANAGERS_FILE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING), StandardCharsets.UTF_8);

         try {
            GSON.toJson(root, w);
         } catch (Throwable var8) {
            try {
               w.close();
            } catch (Throwable var7) {
               var8.addSuppressed(var7);
            }

            throw var8;
         }

         w.close();
      } catch (Exception ex) {
         LOGGER.error("Failed to save known_managers.json", ex);
      }

   }

   public static List<String> loadKnownManagers() {
      try {
         if (!Files.exists(KNOWN_MANAGERS_FILE, new LinkOption[0])) {
            Files.createDirectories(CACHE_DIR);
            Files.createFile(KNOWN_MANAGERS_FILE);
         }

         Reader r = new InputStreamReader(Files.newInputStream(KNOWN_MANAGERS_FILE), StandardCharsets.UTF_8);
         JsonObject root = (JsonObject)GSON.fromJson(r, JsonObject.class);
         if (root != null && root.has("managers")) {
            List<String> result = new ArrayList();

            for(JsonElement el : root.getAsJsonArray("managers")) {
               String val = el.getAsString();
               if (val != null && !val.isBlank()) {
                  result.add(val);
               }
            }

            return result;
         } else {
            return List.of("carpet");
         }
      } catch (Exception ex) {
         LOGGER.warn("Failed to load known_managers.json: {}", ex.getMessage());
         return List.of("carpet");
      }
   }

   private static JsonArray serializeRules(List<RuleData> rules, Map<String, JsonObject> oldLocalNames, Map<String, JsonObject> oldLocalDescs, String currentLanguage) {
      JsonArray arr = new JsonArray();

      for(RuleData r : rules) {
         JsonObject obj = new JsonObject();
         obj.addProperty("name", r.name);
         obj.addProperty("type", r.type.getName());
         obj.addProperty("description", r.description);
         obj.addProperty("defaultValue", r.defaultValue);
         obj.addProperty("isGamerule", r.isGamerule);
         obj.addProperty("manager", r.manager);
         obj.add("localName", mergeLocaleMap((JsonObject)oldLocalNames.get(r.name), currentLanguage, r.localName));
         obj.add("localDescription", mergeLocaleMap((JsonObject)oldLocalDescs.get(r.name), currentLanguage, r.localDescription));
         JsonArray suggs = new JsonArray();
         r.suggestions.forEach(s -> suggs.add((String) s));
         obj.add("suggestions", suggs);
         JsonArray cats = new JsonArray();

         for(Map.Entry<String, String> e : r.categories) {
            cats.add((String)e.getKey());
         }

         obj.add("categories", cats);
         arr.add(obj);
      }

      return arr;
   }

   private static JsonArray serializeTopLevelCategories(Map<String, JsonObject> categoryValues) {
      JsonArray arr = new JsonArray();

      for(Map.Entry<String, JsonObject> entry : categoryValues.entrySet()) {
         JsonObject obj = new JsonObject();
         obj.addProperty("key", (String)entry.getKey());
         obj.add("value", (JsonElement)entry.getValue());
         arr.add(obj);
      }

      return arr;
   }

   private static List<RuleData> deserializeRules(JsonArray arr, Map<String, JsonObject> categoryValues, String currentLanguage) {
      if (arr == null) {
         return List.of();
      } else {
         List<RuleData> list = new ArrayList();

         for(JsonElement el : arr) {
            JsonObject obj = el.getAsJsonObject();
            String name = obj.get("name").getAsString();
            String description = obj.get("description").getAsString();
            String localName = resolveLocale(obj.get("localName"), currentLanguage, name);
            String localDescription = resolveLocale(obj.get("localDescription"), currentLanguage, description);
            RuleData rd = new RuleData(obj.get("manager").getAsString(), name, localName, RuleData.getRuleType(obj.get("type").getAsString()), obj.get("defaultValue").getAsString(), obj.get("defaultValue").getAsString(), description, localDescription, List.of(), List.of());
            rd.isGamerule = obj.get("isGamerule").getAsBoolean();
            List<String> suggs = new ArrayList();
            obj.getAsJsonArray("suggestions").forEach((s) -> suggs.add(s.getAsString()));
            rd.suggestions = suggs;
            List<Map.Entry<String, String>> cats = new ArrayList();
            JsonArray catKeys = obj.getAsJsonArray("categories");
            if (catKeys != null) {
               catKeys.forEach((c) -> {
                  String key = c.getAsString();
                  JsonObject lm = (JsonObject)categoryValues.get(key);
                  String localVal = lm != null ? resolveLocale(lm, currentLanguage, key) : key;
                  cats.add(Map.entry(key, localVal));
               });
            }

            rd.categories = cats;
            list.add(rd);
         }

         return list;
      }
   }

   private static JsonObject mergeLocaleMap(JsonObject oldMap, String lang, String newValue) {
      JsonObject merged = new JsonObject();
      if (oldMap != null) {
         for(Map.Entry<String, JsonElement> e : oldMap.entrySet()) {
            merged.add((String)e.getKey(), (JsonElement)e.getValue());
         }
      }

      if (newValue != null) {
         merged.addProperty(lang, newValue);
      }

      return merged;
   }

   private static String resolveLocale(JsonElement localeEl, String language, String fallback) {
      if (localeEl == null) {
         return fallback;
      } else if (localeEl.isJsonPrimitive()) {
         return localeEl.getAsString();
      } else if (!localeEl.isJsonObject()) {
         return fallback;
      } else {
         JsonObject map = localeEl.getAsJsonObject();
         if (map.has(language)) {
            return map.get(language).getAsString();
         } else {
            return !map.entrySet().isEmpty() ? ((JsonElement)((Map.Entry)map.entrySet().iterator().next()).getValue()).getAsString() : fallback;
         }
      }
   }

   private static void extractLocaleMap(JsonObject ruleObj, String field, String ruleName, Map<String, JsonObject> target) {
      JsonElement el = ruleObj.get(field);
      if (el != null) {
         if (el.isJsonObject()) {
            target.put(ruleName, el.getAsJsonObject().deepCopy());
         }

      }
   }

   private static String sanitize(String address) {
      return address.replaceAll("[^a-zA-Z0-9._\\-]", "_");
   }

   private static Path cacheFile(String serverAddress) {
      return CACHE_DIR.resolve(sanitize(serverAddress) + ".json");
   }

   static {
      CACHE_DIR = FabricLoader.getInstance().getConfigDir().resolve(CarpetGUIConfigManager.CarpetGUI_DIR).resolve("cache");
      KNOWN_MANAGERS_FILE = CACHE_DIR.resolve("known_managers.json");
      RULES_FILE = CACHE_DIR.resolve("base_cache.json");
   }

   public static record CacheResult(List<RuleData> rules, String defaults) {
   }

   public static class RawCacheData {
      public List<CachedRuleEntry> rules;
      public List<CachedCategoryEntry> categories;
      public String defaults;
   }

   public static class CachedRuleEntry {
      public String name;
      public String type;
      public String description;
      public String defaultValue;
      public boolean isGamerule;
      public String manager;
      public List<String> suggestions;
      public List<String> categories;
      public Map<String, String> localName;
      public Map<String, String> localDescription;

      public RuleData toRuleData(String lang, List<CachedCategoryEntry> knowCategories) {
         RuleData rule = new RuleData();
         rule.name = this.name;
         rule.manager = this.manager;
         rule.defaultValue = this.defaultValue;
         rule.isGamerule = this.isGamerule;
         rule.suggestions = this.suggestions;
         rule.type = RuleData.getRuleType(this.type);
         rule.description = this.description;
         rule.localName = (String)this.localName.getOrDefault(lang, this.name);
         rule.localDescription = (String)this.localDescription.getOrDefault(lang, this.description);
         new ArrayList();
         Map<String, String> catValueMap = (Map)knowCategories.stream().collect(Collectors.toMap((c) -> c.key, (c) -> (String)c.value.getOrDefault(lang, c.key)));
         rule.categories = (List)this.categories.stream().map((key) -> Map.entry(key, (String)catValueMap.getOrDefault(key, key))).collect(Collectors.toList());
         return rule;
      }
   }

   public static class CachedCategoryEntry {
      public String key;
      public Map<String, String> value;
   }
}
