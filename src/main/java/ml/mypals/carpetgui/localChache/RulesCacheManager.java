package ml.mypals.carpetgui.localChache;

import com.google.gson.*;
import ml.mypals.carpetgui.network.RuleData;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

import static ml.mypals.carpetgui.network.RuleData.getRuleType;

public class RulesCacheManager {

    private static final Logger LOGGER = LoggerFactory.getLogger("carpetgui-cache");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Path CACHE_DIR = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("carpetgui")
            .resolve("cache");
    private static final Path KNOWN_MANAGERS_FILE = CACHE_DIR.resolve("known_managers.json");
    private static final Path RULES_FILE = CACHE_DIR.resolve("base_cache.json");
    public static void saveCache(List<RuleData> newRules,
                                 String defaults,
                                 String currentLanguage) {
        try {
            Files.createDirectories(CACHE_DIR);

            Map<String, JsonObject> oldLocalNames   = new HashMap<>();
            Map<String, JsonObject> oldLocalDescs   = new HashMap<>();
            Map<String, JsonObject> oldCategoryValues = new LinkedHashMap<>();
            List<RuleData> oldRules    = List.of();
            String mergedDefaults      = defaults;

            Path file = RULES_FILE;
            if (Files.exists(file)) {
                try (Reader r = new InputStreamReader(
                        Files.newInputStream(file), StandardCharsets.UTF_8)) {

                    JsonObject oldRoot = GSON.fromJson(r, JsonObject.class);

                    if ((defaults == null || defaults.isBlank()) && oldRoot.has("defaults")) {
                        mergedDefaults = oldRoot.get("defaults").getAsString();
                    }

                    JsonArray oldCatsArr = oldRoot.getAsJsonArray("categories");
                    if (oldCatsArr != null) {
                        for (JsonElement el : oldCatsArr) {
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
                        for (JsonElement el : oldRulesArr) {
                            JsonObject obj = el.getAsJsonObject();
                            String name = obj.get("name").getAsString();
                            extractLocaleMap(obj, "localName",        name, oldLocalNames);
                            extractLocaleMap(obj, "localDescription",  name, oldLocalDescs);
                        }
                        oldRules = deserializeRules(oldRulesArr, oldCategoryValues, currentLanguage);
                    }

                } catch (Exception ex) {
                    LOGGER.warn("Could not read old cache for incremental merge: {}", ex.getMessage());
                }
            }

            Map<String, RuleData> ruleMap = new LinkedHashMap<>();
            for (RuleData old : oldRules) {
                if (old.name != null && !old.name.isBlank()) ruleMap.put(old.name, old);
            }
            for (RuleData neu : newRules) {
                if (neu.name != null && !neu.name.isBlank()) ruleMap.put(neu.name, neu);
            }
            List<RuleData> mergedRules = new ArrayList<>(ruleMap.values());

            for (RuleData rule : newRules) {
                for (Map.Entry<String, String> entry : rule.categories) {
                    String catKey   = entry.getKey();
                    String localVal = entry.getValue();
                    JsonObject langMap = oldCategoryValues.computeIfAbsent(catKey, k -> new JsonObject());
                    langMap.addProperty(currentLanguage, localVal);
                }
            }

            JsonObject root = new JsonObject();
            root.addProperty("defaults",      mergedDefaults);
            root.add("rules",      serializeRules(mergedRules, oldLocalNames, oldLocalDescs, currentLanguage));
            root.add("categories", serializeTopLevelCategories(oldCategoryValues));

            Path target = RULES_FILE;
            try (Writer w = new OutputStreamWriter(
                    Files.newOutputStream(target, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING),
                    StandardCharsets.UTF_8)) {
                GSON.toJson(root, w);
            }
            Set<String> managers = mergedRules.stream()
                    .map(r -> r.manager)
                    .filter(m -> m != null && !m.isBlank())
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            saveKnownManagers(managers);

        } catch (Exception ex) {
            LOGGER.error("Failed to save cache !", ex);
        }
    }

    public static Optional<CacheResult> loadCache(String currentLanguage) {
        Path file = RULES_FILE;
        if (!Files.exists(file)) return Optional.empty();

        try (Reader r = new InputStreamReader(Files.newInputStream(file), StandardCharsets.UTF_8)) {

            JsonObject root     = GSON.fromJson(r, JsonObject.class);
            String     defaults = root.has("defaults") ? root.get("defaults").getAsString() : "";

            Map<String, JsonObject> categoryValues = new LinkedHashMap<>();
            JsonArray catsArr = root.getAsJsonArray("categories");
            if (catsArr != null) {
                for (JsonElement el : catsArr) {
                    JsonObject obj = el.getAsJsonObject();
                    String     key = obj.get("key").getAsString();
                    JsonElement val = obj.get("value");
                    if (val != null && val.isJsonObject()) {
                        categoryValues.put(key, val.getAsJsonObject());
                    }
                }
            }

            List<RuleData> rules = deserializeRules(
                    root.getAsJsonArray("rules"), categoryValues, currentLanguage);
            return Optional.of(new CacheResult(rules, defaults));

        } catch (Exception ex) {
            LOGGER.error("Failed to load cache !", ex);
            return Optional.empty();
        }
    }
    public static void saveKnownManagers(Set<String> newManagers) {
        try {
            Files.createDirectories(CACHE_DIR);

            Set<String> merged = new LinkedHashSet<>(loadKnownManagers());
            merged.addAll(newManagers);

            JsonArray arr = new JsonArray();
            for (String m : merged) arr.add(m);

            JsonObject root = new JsonObject();
            root.add("managers", arr);

            try (Writer w = new OutputStreamWriter(
                    Files.newOutputStream(KNOWN_MANAGERS_FILE,
                            StandardOpenOption.CREATE,
                            StandardOpenOption.TRUNCATE_EXISTING),
                    StandardCharsets.UTF_8)) {
                GSON.toJson(root, w);
            }
        } catch (Exception ex) {
            LOGGER.error("Failed to save known_managers.json", ex);
        }
    }
    public static List<String> loadKnownManagers() {
        if (!Files.exists(KNOWN_MANAGERS_FILE)) return List.of("carpet");

        try (Reader r = new InputStreamReader(
                Files.newInputStream(KNOWN_MANAGERS_FILE), StandardCharsets.UTF_8)) {

            JsonObject root = GSON.fromJson(r, JsonObject.class);
            if (root == null || !root.has("managers")) return List.of("carpet");

            List<String> result = new ArrayList<>();
            for (JsonElement el : root.getAsJsonArray("managers")) {
                String val = el.getAsString();
                if (val != null && !val.isBlank()) result.add(val);
            }
            return result;

        } catch (Exception ex) {
            LOGGER.warn("Failed to load known_managers.json: {}", ex.getMessage());
            return List.of("carpet");
        }
    }
    private static JsonArray serializeRules(List<RuleData> rules,
                                            Map<String, JsonObject> oldLocalNames,
                                            Map<String, JsonObject> oldLocalDescs,
                                            String currentLanguage) {
        JsonArray arr = new JsonArray();
        for (RuleData r : rules) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name",         r.name);
            obj.addProperty("type",         r.type.getName());
            obj.addProperty("description",  r.description);
            obj.addProperty("defaultValue", r.defaultValue);
            obj.addProperty("isGamerule",   r.isGamerule);
            obj.addProperty("manager",      r.manager);

            obj.add("localName",
                    mergeLocaleMap(oldLocalNames.get(r.name), currentLanguage, r.localName));
            obj.add("localDescription",
                    mergeLocaleMap(oldLocalDescs.get(r.name), currentLanguage, r.localDescription));


            JsonArray suggs = new JsonArray();
            r.suggestions.forEach(suggs::add);
            obj.add("suggestions", suggs);


            JsonArray cats = new JsonArray();
            for (Map.Entry<String, String> e : r.categories) {
                cats.add(e.getKey());
            }
            obj.add("categories", cats);

            arr.add(obj);
        }
        return arr;
    }

    private static JsonArray serializeTopLevelCategories(Map<String, JsonObject> categoryValues) {
        JsonArray arr = new JsonArray();
        for (Map.Entry<String, JsonObject> entry : categoryValues.entrySet()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("key", entry.getKey());
            obj.add("value", entry.getValue());
            arr.add(obj);
        }
        return arr;
    }

    private static List<RuleData> deserializeRules(JsonArray arr,
                                                   Map<String, JsonObject> categoryValues,
                                                   String currentLanguage) {
        if (arr == null) return List.of();
        List<RuleData> list = new ArrayList<>();
        for (JsonElement el : arr) {
            JsonObject obj = el.getAsJsonObject();

            String name        = obj.get("name").getAsString();
            String description = obj.get("description").getAsString();

            String localName        = resolveLocale(obj.get("localName"),        currentLanguage, name);
            String localDescription = resolveLocale(obj.get("localDescription"), currentLanguage, description);

            RuleData rd = new RuleData(
                    obj.get("manager").getAsString(),
                    name,
                    localName,
                    getRuleType(obj.get("type").getAsString()),
                    obj.get("defaultValue").getAsString(),
                    obj.get("defaultValue").getAsString(),
                    description,
                    localDescription,
                    List.of(),
                    List.of()
            );

            rd.isGamerule = obj.get("isGamerule").getAsBoolean();

            List<String> suggs = new ArrayList<>();
            obj.getAsJsonArray("suggestions").forEach(s -> suggs.add(s.getAsString()));
            rd.suggestions = suggs;

            List<Map.Entry<String, String>> cats = new ArrayList<>();
            JsonArray catKeys = obj.getAsJsonArray("categories");
            if (catKeys != null) {
                catKeys.forEach(c -> {
                    String key     = c.getAsString();
                    JsonObject lm  = categoryValues.get(key);
                    String localVal = (lm != null)
                            ? resolveLocale(lm, currentLanguage, key)
                            : key;
                    cats.add(Map.entry(key, localVal));
                });
            }
            rd.categories = cats;

            list.add(rd);
        }
        return list;
    }

   private static JsonObject mergeLocaleMap(JsonObject oldMap,
                                             String lang,
                                             String newValue) {
        JsonObject merged = new JsonObject();
        if (oldMap != null) {
             for (Map.Entry<String, JsonElement> e : oldMap.entrySet()) {
                merged.add(e.getKey(), e.getValue());
            }
        }
        if (newValue != null) {
            merged.addProperty(lang, newValue);
        }
        return merged;
    }
   private static String resolveLocale(JsonElement localeEl, String language, String fallback) {
        if (localeEl == null) return fallback;
        if (localeEl.isJsonPrimitive()) return localeEl.getAsString();
        if (!localeEl.isJsonObject())   return fallback;

        JsonObject map = localeEl.getAsJsonObject();
        if (map.has(language)) return map.get(language).getAsString();
        if (!map.entrySet().isEmpty()) {
            return map.entrySet().iterator().next().getValue().getAsString();
        }
        return fallback;
    }

    private static void extractLocaleMap(JsonObject ruleObj,
                                         String field,
                                         String ruleName,
                                         Map<String, JsonObject> target) {
        JsonElement el = ruleObj.get(field);
        if (el == null) return;
        if (el.isJsonObject()) {
            target.put(ruleName, el.getAsJsonObject().deepCopy());
        }
    }

    private static String sanitize(String address) {
        return address.replaceAll("[^a-zA-Z0-9._\\-]", "_");
    }

    private static Path cacheFile(String serverAddress) {
        return CACHE_DIR.resolve(sanitize(serverAddress) + ".json");
    }

    public record CacheResult(List<RuleData> rules, String defaults) {}
}