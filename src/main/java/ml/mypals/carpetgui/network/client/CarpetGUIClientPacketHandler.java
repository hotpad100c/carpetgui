package ml.mypals.carpetgui.network.client;

import ml.mypals.carpetgui.CarpetGUIClient;
import ml.mypals.carpetgui.localChache.RulesCacheManager;
import ml.mypals.carpetgui.network.RuleData;
import ml.mypals.carpetgui.network.server.HelloPacketPayload;
import ml.mypals.carpetgui.network.server.RuleStackSyncPayload;
import ml.mypals.carpetgui.network.server.RulesPacketPayload;
import ml.mypals.carpetgui.screen.ruleGroup.RuleGroupScreen;
import ml.mypals.carpetgui.screen.ruleStack.RuleStackData;
import ml.mypals.carpetgui.screen.ruleStack.RuleStackScreen;
import ml.mypals.carpetgui.screen.rulesEditScreen.RulesEditScreen;
import ml.mypals.carpetgui.settings.CarpetGUIConfigManager;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.stream.Collectors;

import static ml.mypals.carpetgui.CarpetGUIClient.*;

public class CarpetGUIClientPacketHandler {
    public static void handleHelloPacket(HelloPacketPayload payload) {
        Minecraft.getInstance().execute(()-> CarpetGUIClient.hasModOnServer = true);
    }

    public static void handleRuleStackSync(RuleStackSyncPayload payload) {
        Minecraft.getInstance().execute(() -> {
            CarpetGUIClient.cachedRuleStackData = new RuleStackData(
                    payload.activePrefabName(),
                    payload.allPrefabNames(),
                    payload.layers(),
                    payload.pendingChanges(),
                    payload.futureLayers()
            );

            if (RuleStackScreen.INSTANCE != null) {
                RuleStackScreen.INSTANCE.onSync();
            }
        });
    }

    public static void handleRulesPacket(RulesPacketPayload payload) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.execute(() -> {
            Minecraft client = Minecraft.getInstance();
            boolean fromRuleGroupScreen = client.screen instanceof RuleGroupScreen rgs && rgs.requestingRulesForNewGroup;

            CarpetGUIClient.hasModOnServer = true;

            if (!payload.isPartial()) {
                CarpetGUIClient.cachedCompleteRules.clear();
                CarpetGUIClient.cachedCompleteRules.addAll(payload.rules());
                cachedCategories.clear();
                for (var entry : RulesEditScreen.DefaultCategory.values()) {
                    if (!entry.equals(RulesEditScreen.DefaultCategory.SEARCHING)) {
                        cachedCategories.add(entry.getName());
                    }
                }

                Set<String> categories = new LinkedHashSet<>();

                ListIterator<RuleData> it = cachedCompleteRules.listIterator();
                while (it.hasNext()) {
                    RuleData data = it.next();

                    for (Map.Entry<?, String> entry : data.categories) {
                        categories.add(entry.getValue());
                    }

                    if (fromRuleGroupScreen) {
                        data.value = data.defaultValue;
                        it.set(data);
                    }
                }

                cachedCategories.addAll(categories);

                defaultRules.clear();
                if (!fromRuleGroupScreen) {
                    defaultRules.addAll(Arrays.stream(payload.defaults().split(";")).toList());
                }

                favoriteRules.clear();
                favoriteRules.addAll(CarpetGUIConfigManager.readFavoriteRules());

                String lang = client.getLanguageManager().getSelected();

                new Thread(() -> {
                    RulesCacheManager.saveCache(cachedCompleteRules, payload.defaults(), lang);
                    cachedManagers = RulesCacheManager.loadKnownManagers();
                }, "carpetgui-cache-save").start();

            } else {
                String lang = client.getLanguageManager().getSelected();

                RulesCacheManager.RawCacheData rawCache = RulesCacheManager.loadRawCache();
                if (rawCache == null) {
                    ClientPlayNetworking.send(new RequestRulesPayload(lang, List.of()));
                    return;
                }

                Map<String, RulesCacheManager.CachedRuleEntry> mergedMap = new LinkedHashMap<>();
                for (RulesCacheManager.CachedRuleEntry entry : rawCache.rules) {
                    mergedMap.put(entry.name, entry);
                }

                for (RuleData newRule : payload.rules()) {
                    if (newRule.name == null) continue;

                    RulesCacheManager.CachedRuleEntry existing = mergedMap.get(newRule.name);
                    if (existing != null) {
                        existing.localName.put(lang, newRule.localName);
                        existing.localDescription.put(lang, newRule.localDescription);
                        for (Map.Entry<String, String> cat : newRule.categories) {
                            rawCache.categories.stream()
                                    .filter(c -> c.key.equals(cat.getKey()))
                                    .findFirst()
                                    .ifPresentOrElse(
                                            c -> c.value.put(lang, cat.getValue()),
                                            () -> {
                                                RulesCacheManager.CachedCategoryEntry newCat = new RulesCacheManager.CachedCategoryEntry();
                                                newCat.key = cat.getKey();
                                                newCat.value = new HashMap<>(Map.of(lang, cat.getValue()));
                                                rawCache.categories.add(newCat);
                                            }
                                    );
                        }
                    } else {
                        RulesCacheManager.CachedRuleEntry newEntry = new RulesCacheManager.CachedRuleEntry();
                        newEntry.name = newRule.name;
                        newEntry.type = newRule.type.getSimpleName();
                        newEntry.description = newRule.description;
                        newEntry.defaultValue = newRule.defaultValue;
                        newEntry.isGamerule = newRule.isGamerule;
                        newEntry.manager = newRule.manager;
                        newEntry.suggestions = newRule.suggestions;
                        newEntry.localName = new HashMap<>(Map.of(lang, newRule.localName));
                        newEntry.localDescription = new HashMap<>(Map.of(lang, newRule.localDescription));
                        newEntry.categories = newRule.categories.stream()
                                .map(Map.Entry::getKey).toList();
                        mergedMap.put(newEntry.name, newEntry);
                    }
                }

                if (incompleteRulesFromServer != null && !incompleteRulesFromServer.isEmpty()) {
                    Set<String> serverNames = incompleteRulesFromServer.stream()
                            .map(r -> r.name).filter(Objects::nonNull).collect(Collectors.toSet());

                    mergedMap.entrySet().removeIf(e -> {
                        RulesCacheManager.CachedRuleEntry entry = e.getValue();
                        return !entry.isGamerule && !serverNames.contains(e.getKey());
                    });

                    Map<String, String> serverValues = incompleteRulesFromServer.stream()
                            .filter(r -> r.name != null)
                            .collect(Collectors.toMap(r -> r.name, r -> r.value));
                    CarpetGUIClient.cachedCompleteRules.clear();
                    for (RulesCacheManager.CachedRuleEntry entry : mergedMap.values()) {
                        RuleData rule = entry.toRuleData(lang, rawCache.categories);
                        rule.value = serverValues.getOrDefault(rule.name, rule.defaultValue);
                        CarpetGUIClient.cachedCompleteRules.add(rule);
                    }
                }
                CarpetGUIClient.cachedCompleteRules.addAll(payload.rules());

                cachedCategories.clear();

                for (var entry : RulesEditScreen.DefaultCategory.values()) {
                    if (!entry.equals(RulesEditScreen.DefaultCategory.SEARCHING)) {
                        cachedCategories.add(entry.getName());
                    }
                }
                Set<String> categories = new HashSet<>();

                ListIterator<RuleData> it = cachedCompleteRules.listIterator();
                while (it.hasNext()) {
                    RuleData data = it.next();

                    for (Map.Entry<?, String> entry : data.categories) {
                        categories.add(entry.getValue());
                    }

                    if (fromRuleGroupScreen) {
                        data.value = data.defaultValue;
                        it.set(data);
                    }
                }

                cachedCategories.addAll(categories);

                defaultRules.clear();
                if (!fromRuleGroupScreen) {
                    defaultRules.addAll(Arrays.stream(payload.defaults().split(";")).toList());
                }

                favoriteRules.clear();
                favoriteRules.addAll(CarpetGUIConfigManager.readFavoriteRules());

                new Thread(() -> {
                    RulesCacheManager.saveRawCache(rawCache, mergedMap.values(), payload.defaults());
                    cachedManagers = RulesCacheManager.loadKnownManagers();
                }, "carpetgui-cache-save").start();
            }

            requesting = false;
            client.setScreen(new RulesEditScreen(!fromRuleGroupScreen));
        });
    }
    public static void openRuleEditScreen(boolean instantAffect) {
        Minecraft client = Minecraft.getInstance();
        if (hasModOnServer) {
            String lang = client.getLanguageManager().getSelected();
            List<String> knownRuleNames = new ArrayList<>();

            if (incompleteRulesFromServer != null && !incompleteRulesFromServer.isEmpty()) {

                RulesCacheManager.RawCacheData rawCache = RulesCacheManager.loadRawCache();

                if (rawCache != null) {
                    Map<String, RulesCacheManager.CachedRuleEntry> cacheMap = rawCache.rules.stream()
                            .collect(Collectors.toMap(r -> r.name, r -> r));

                    for (RuleData serverRule : incompleteRulesFromServer) {
                        if (serverRule.name == null) continue;

                        RulesCacheManager.CachedRuleEntry cached = cacheMap.get(serverRule.name);
                        if (cached == null) continue;
                        boolean hasLocalName = cached.localName.containsKey(lang);
                        boolean hasLocalDesc = cached.localDescription.containsKey(lang);

                        boolean hasAllCategories = rawCache.categories.stream()
                                .filter(cat -> cached.categories.contains(cat.key))
                                .allMatch(cat -> cat.value.containsKey(lang));

                        if (hasLocalName && hasLocalDesc && hasAllCategories) {
                            knownRuleNames.add(serverRule.name);
                        }
                    }
                }
            }

            ClientPlayNetworking.send(new RequestRulesPayload(lang, knownRuleNames));
            requesting = true;
        } else {
            openScreenFromCache(client, instantAffect);
        }
    }
    private static void openScreenFromCache(Minecraft client, boolean instantAffect) {
        String addr = getServerAddress(client);
        if (addr == null) return;

        String lang = client.getLanguageManager().getSelected();
        Optional<RulesCacheManager.CacheResult> cacheOpt = RulesCacheManager.loadCache(lang);

        cacheOpt.ifPresent(cache -> {
            client.execute(() -> {
                cachedCompleteRules.clear();
                cachedCompleteRules.addAll(cache.rules());

                cachedCategories.clear();
                for (var entry : RulesEditScreen.DefaultCategory.values()) {
                    if (!entry.equals(RulesEditScreen.DefaultCategory.SEARCHING)) {
                        cachedCategories.add(entry.getName());
                    }
                }
                cachedCategories.addAll(
                        cache.rules().stream()
                                .flatMap(r -> r.categories.stream())
                                .distinct()
                                .map(Map.Entry::getValue)
                                .toList()
                );

                defaultRules.clear();
                defaultRules.addAll(Arrays.stream(cache.defaults().split(";")).toList());

                favoriteRules.clear();
                favoriteRules.addAll(CarpetGUIConfigManager.readFavoriteRules());

                if (incompleteRulesFromServer != null && !incompleteRulesFromServer.isEmpty()) {
                    Map<String, RuleData> cachedMap = new HashMap<>();
                    for (RuleData rule : cachedCompleteRules) {
                        if (rule.name != null) {
                            cachedMap.put(rule.name, rule);
                        }
                    }

                    for (RuleData serverRule : incompleteRulesFromServer) {
                        if (serverRule.name == null || serverRule.name.isEmpty()) {
                            continue;
                        }

                        RuleData existing = cachedMap.get(serverRule.name);

                        if (existing != null) {
                            existing.value = serverRule.value;
                            if (serverRule.manager != null) {
                                existing.manager = serverRule.manager;
                            }
                        } else {
                            RuleData newRule = new RuleData();
                            newRule.manager = serverRule.manager;
                            newRule.name = serverRule.name;
                            newRule.value = serverRule.value;
                            String unknown = Component.translatable("gui.tip.unknown_rule").getString();
                            newRule.localName = serverRule.name;
                            newRule.defaultValue = serverRule.value;
                            newRule.description = unknown;
                            newRule.localDescription = unknown;
                            newRule.type = null;
                            newRule.suggestions = serverRule.value.equals("true") || serverRule.value.equals("false") ? List.of("true", "false") : List.of("");
                            newRule.categories = List.of(Map.entry("unkown", Component.translatable("gui.category.unknown").getString()));
                            newRule.isGamerule = false;

                            cachedCompleteRules.add(newRule);
                            cachedMap.put(newRule.name, newRule);
                        }
                    }

                    Iterator<RuleData> iterator = cachedCompleteRules.iterator();
                    while (iterator.hasNext()) {
                        RuleData cachedRule = iterator.next();
                        if (cachedRule.name == null) continue;

                        boolean existsOnServer = incompleteRulesFromServer.stream()
                                .anyMatch(sr -> sr.name != null && sr.name.equals(cachedRule.name));

                        if (!existsOnServer && !cachedRule.isGamerule) {
                            iterator.remove();
                            cachedMap.remove(cachedRule.name);
                        }
                    }
                }
                client.setScreen(new RulesEditScreen(instantAffect));
            });
        });
    }
}
