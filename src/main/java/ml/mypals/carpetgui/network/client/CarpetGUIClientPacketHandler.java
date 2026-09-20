package ml.mypals.carpetgui.network.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
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
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class CarpetGUIClientPacketHandler {
   private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

   public static void handleHelloPacket(HelloPacketPayload payload) {
      Minecraft.getInstance().execute(() -> CarpetGUIClient.hasModOnServer = true);
   }

   public static void handleRuleStackSync(RuleStackSyncPayload payload) {
      Minecraft.getInstance().execute(() -> {
         CarpetGUIClient.cachedRuleStackData = new RuleStackData(payload.activePrefabName(), payload.allPrefabNames(), payload.layers(), payload.pendingChanges(), payload.futureLayers());
         if (RuleStackScreen.INSTANCE != null) {
            RuleStackScreen.INSTANCE.onSync();
         }

      });
   }

   public static void handleRulesPacket(RulesPacketPayload payload) {
      Minecraft.getInstance().execute(() -> {
         Minecraft client;
         boolean var10000;
         label22: {
            client = Minecraft.getInstance();
            //? if <26.2 {
            /*Screen patt0$temp = client.screen;
            *///?} else {
            Screen patt0$temp = client.gui.screen();
            //?}
            if (patt0$temp instanceof RuleGroupScreen rgs) {
               if (rgs.requestingRulesForNewGroup) {
                  var10000 = true;
                  break label22;
               }
            }

            var10000 = false;
         }

         boolean fromRuleGroupScreen = var10000;
         CarpetGUIClient.hasModOnServer = true;
         if (!payload.isPartial()) {
            handleCompletePacket(payload, fromRuleGroupScreen, client);
         } else {
            handlePartialPacket(payload, fromRuleGroupScreen, client);
         }

         CarpetGUIClient.requesting = false;
         client.setScreenAndShow(new RulesEditScreen(!fromRuleGroupScreen));
      });
   }

   private static void handleCompletePacket(RulesPacketPayload payload, boolean fromRuleGroupScreen, Minecraft client) {
      CarpetGUIClient.cachedCompleteRules.clear();

      for(RuleData rule : payload.rules()) {
         if (fromRuleGroupScreen) {
            rule.value = rule.defaultValue;
         }

         CarpetGUIClient.cachedCompleteRules.put(rule.name, rule);
      }

      refreshCachedCategories(CarpetGUIClient.cachedCompleteRules.values());
      refreshDefaultAndFavoriteRules(payload.defaults(), fromRuleGroupScreen);
      String lang = client.getLanguageManager().getSelected();
      EXECUTOR.execute(() -> {
         RulesCacheManager.saveCache(new ArrayList(CarpetGUIClient.cachedCompleteRules.values()), payload.defaults(), lang);
         CarpetGUIClient.cachedManagers = RulesCacheManager.loadKnownManagers();
      });
   }

   private static void handlePartialPacket(RulesPacketPayload payload, boolean fromRuleGroupScreen, Minecraft client) {
      String lang = client.getLanguageManager().getSelected();
      RulesCacheManager.RawCacheData rawCache = RulesCacheManager.loadRawCache();
      if (rawCache == null) {
         ClientPlayNetworking.send(new RequestRulesPayload(lang, List.of()));
      } else {
         Map<String, RulesCacheManager.CachedRuleEntry> mergedMap = buildMergedMap(rawCache, payload.rules(), lang);
         reconcileWithServerRules(mergedMap, rawCache, lang, fromRuleGroupScreen);

         for(RuleData rule : payload.rules()) {
            CarpetGUIClient.cachedCompleteRules.put(rule.name, rule);
         }

         refreshCachedCategories(CarpetGUIClient.cachedCompleteRules.values());
         refreshDefaultAndFavoriteRules(payload.defaults(), fromRuleGroupScreen);
         (new Thread(() -> {
            RulesCacheManager.saveRawCache(rawCache, mergedMap.values(), payload.defaults());
            CarpetGUIClient.cachedManagers = RulesCacheManager.loadKnownManagers();
         }, "carpetgui-cache-save")).start();
      }
   }

   private static Map<String, RulesCacheManager.CachedRuleEntry> buildMergedMap(RulesCacheManager.RawCacheData rawCache, List<RuleData> incomingRules, String lang) {
      Map<String, RulesCacheManager.CachedRuleEntry> mergedMap = new LinkedHashMap();

      for(RulesCacheManager.CachedRuleEntry entry : rawCache.rules) {
         mergedMap.put(entry.name, entry);
      }

      for(RuleData newRule : incomingRules) {
         if (newRule.name != null) {
            RulesCacheManager.CachedRuleEntry existing = (RulesCacheManager.CachedRuleEntry)mergedMap.get(newRule.name);
            if (existing != null) {
               updateExistingCacheEntry(existing, newRule, rawCache, lang);
            } else {
               mergedMap.put(newRule.name, createCacheEntry(newRule, lang));
            }
         }
      }

      return mergedMap;
   }

   private static void updateExistingCacheEntry(RulesCacheManager.CachedRuleEntry existing, RuleData newRule, RulesCacheManager.RawCacheData rawCache, String lang) {
      existing.localName.put(lang, newRule.localName);
      existing.localDescription.put(lang, newRule.localDescription);

      for(Map.Entry<String, String> cat : newRule.categories) {
         rawCache.categories.stream().filter((c) -> c.key.equals(cat.getKey())).findFirst().ifPresentOrElse((c) -> c.value.put(lang, (String)cat.getValue()), () -> {
            RulesCacheManager.CachedCategoryEntry newCat = new RulesCacheManager.CachedCategoryEntry();
            newCat.key = (String)cat.getKey();
            newCat.value = new HashMap(Map.of(lang, (String)cat.getValue()));
            rawCache.categories.add(newCat);
         });
      }

   }

   private static RulesCacheManager.CachedRuleEntry createCacheEntry(RuleData rule, String lang) {
      RulesCacheManager.CachedRuleEntry entry = new RulesCacheManager.CachedRuleEntry();
      entry.name = rule.name;
      entry.type = rule.type.getSimpleName();
      entry.description = rule.description;
      entry.defaultValue = rule.defaultValue;
      entry.isGamerule = rule.isGamerule;
      entry.manager = rule.manager;
      entry.suggestions = rule.suggestions;
      entry.localName = new HashMap(Map.of(lang, rule.localName));
      entry.localDescription = new HashMap(Map.of(lang, rule.localDescription));
      entry.categories = rule.categories.stream().map(Map.Entry::getKey).toList();
      return entry;
   }

   private static void reconcileWithServerRules(Map<String, RulesCacheManager.CachedRuleEntry> mergedMap, RulesCacheManager.RawCacheData rawCache, String lang, boolean fromRuleGroupScreen) {
      if (CarpetGUIClient.incompleteRulesFromServer != null && !CarpetGUIClient.incompleteRulesFromServer.isEmpty()) {
         Set<String> serverNames = (Set)CarpetGUIClient.incompleteRulesFromServer.stream().map((r) -> r.name).filter(Objects::nonNull).collect(Collectors.toSet());
         mergedMap.entrySet().removeIf((e) -> !((RulesCacheManager.CachedRuleEntry)e.getValue()).isGamerule && !serverNames.contains(e.getKey()));
         Map<String, String> serverValues = (Map)CarpetGUIClient.incompleteRulesFromServer.stream().filter((r) -> r.name != null).collect(Collectors.toMap((r) -> r.name, (r) -> r.value));
         CarpetGUIClient.cachedCompleteRules.clear();

         for(RulesCacheManager.CachedRuleEntry entry : mergedMap.values()) {
            RuleData rule = entry.toRuleData(lang, rawCache.categories);
            rule.value = (String)serverValues.getOrDefault(rule.name, rule.defaultValue);
            if (fromRuleGroupScreen) {
               rule.value = rule.defaultValue;
            }

            CarpetGUIClient.cachedCompleteRules.put(rule.name, rule);
         }

      }
   }

   private static void refreshCachedCategories(Collection<RuleData> rules) {
      CarpetGUIClient.cachedCategories.clear();

      for(RulesEditScreen.DefaultCategory entry : RulesEditScreen.DefaultCategory.values()) {
         if (!entry.equals(RulesEditScreen.DefaultCategory.SEARCHING)) {
            CarpetGUIClient.cachedCategories.add(entry.getName());
         }
      }

      for(RuleData data : rules) {
         for(Map.Entry<?, String> entry : data.categories) {
            CarpetGUIClient.cachedCategories.add((String)entry.getValue());
         }
      }

   }

   private static void refreshDefaultAndFavoriteRules(String defaults, boolean fromRuleGroupScreen) {
      CarpetGUIClient.defaultRules.clear();
      if (!fromRuleGroupScreen) {
         Collections.addAll(CarpetGUIClient.defaultRules, defaults.split(";"));
      }

      CarpetGUIClient.favoriteRules.clear();
      CarpetGUIClient.favoriteRules.addAll(CarpetGUIConfigManager.readFavoriteRules());
   }

   public static void openRuleEditScreen(boolean instantAffect) {
      Minecraft client = Minecraft.getInstance();
      if (CarpetGUIClient.hasModOnServer) {
         String lang = client.getLanguageManager().getSelected();
         List<String> knownRuleNames = new ArrayList();
         if (CarpetGUIClient.incompleteRulesFromServer != null && !CarpetGUIClient.incompleteRulesFromServer.isEmpty()) {
            RulesCacheManager.RawCacheData rawCache = RulesCacheManager.loadRawCache();
            if (rawCache != null) {
               Map<String, RulesCacheManager.CachedRuleEntry> cacheMap = (Map)rawCache.rules.stream().collect(Collectors.toMap((r) -> r.name, (r) -> r));

               for(RuleData serverRule : CarpetGUIClient.incompleteRulesFromServer) {
                  if (serverRule.name != null) {
                     RulesCacheManager.CachedRuleEntry cached = (RulesCacheManager.CachedRuleEntry)cacheMap.get(serverRule.name);
                     if (cached != null) {
                        boolean hasLocalName = cached.localName.containsKey(lang);
                        boolean hasLocalDesc = cached.localDescription.containsKey(lang);
                        boolean hasAllCategories = rawCache.categories.stream().filter((cat) -> cached.categories.contains(cat.key)).allMatch((cat) -> cat.value.containsKey(lang));
                        if (hasLocalName && hasLocalDesc && hasAllCategories) {
                           knownRuleNames.add(serverRule.name);
                        }
                     }
                  }
               }
            }
         }

         ClientPlayNetworking.send(new RequestRulesPayload(lang, knownRuleNames));
         CarpetGUIClient.requesting = true;
      } else {
         openScreenFromCache(client, instantAffect);
      }

   }

   private static void openScreenFromCache(Minecraft client, boolean instantAffect) {
      String addr = CarpetGUIClient.getServerAddress(client);
      if (addr != null) {
         String lang = client.getLanguageManager().getSelected();
         Optional<RulesCacheManager.CacheResult> cacheOpt = RulesCacheManager.loadCache(lang);
         cacheOpt.ifPresent((cache) -> client.execute(() -> {
               CarpetGUIClient.cachedCompleteRules.clear();

               for(RuleData ruleData : cache.rules()) {
                  CarpetGUIClient.cachedCompleteRules.put(ruleData.name, ruleData);
               }

               CarpetGUIClient.cachedCategories.clear();

               for(RulesEditScreen.DefaultCategory entry : RulesEditScreen.DefaultCategory.values()) {
                  if (!entry.equals(RulesEditScreen.DefaultCategory.SEARCHING)) {
                     CarpetGUIClient.cachedCategories.add(entry.getName());
                  }
               }

               CarpetGUIClient.cachedCategories.addAll(cache.rules().stream().flatMap((r) -> r.categories.stream()).distinct().map(Map.Entry::getValue).toList());
               CarpetGUIClient.defaultRules.clear();
               CarpetGUIClient.defaultRules.addAll(Arrays.stream(cache.defaults().split(";")).toList());
               CarpetGUIClient.favoriteRules.clear();
               CarpetGUIClient.favoriteRules.addAll(CarpetGUIConfigManager.readFavoriteRules());
               if (CarpetGUIClient.incompleteRulesFromServer != null && !CarpetGUIClient.incompleteRulesFromServer.isEmpty()) {
                  Map<String, RuleData> cachedMap = new HashMap();

                  for(RuleData rule : CarpetGUIClient.cachedCompleteRules.values()) {
                     if (rule.name != null) {
                        cachedMap.put(rule.name, rule);
                     }
                  }

                  for(RuleData serverRule : CarpetGUIClient.incompleteRulesFromServer) {
                     if (serverRule.name != null && !serverRule.name.isEmpty()) {
                        RuleData existing = (RuleData)cachedMap.get(serverRule.name);
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
                           newRule.suggestions = !serverRule.value.equals("true") && !serverRule.value.equals("false") ? List.of("") : List.of("true", "false");
                           newRule.categories = List.of(Map.entry("unkown", Component.translatable("gui.category.unknown").getString()));
                           newRule.isGamerule = false;
                           CarpetGUIClient.cachedCompleteRules.put(newRule.name, newRule);
                           cachedMap.put(newRule.name, newRule);
                        }
                     }
                  }

                  for(RuleData cachedRule : CarpetGUIClient.cachedCompleteRules.values()) {
                     if (cachedRule.name != null) {
                        boolean existsOnServer = CarpetGUIClient.incompleteRulesFromServer.stream().anyMatch((sr) -> sr.name != null && sr.name.equals(cachedRule.name));
                        if (!existsOnServer && !cachedRule.isGamerule) {
                           cachedMap.remove(cachedRule.name);
                        }
                     }
                  }
               }

               client.setScreenAndShow(new RulesEditScreen(instantAffect));
            }));
      }
   }
}
