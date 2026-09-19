package ml.mypals.carpetgui.ruleStack;

import carpet.CarpetServer;
import carpet.api.settings.CarpetRule;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import ml.mypals.carpetgui.CarpetGUI;
import ml.mypals.carpetgui.accessors.CommandSourceStackAccessor;
import ml.mypals.carpetgui.mixin.accessors.SettngsManagerAccessor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleType;
import net.minecraft.world.level.gamerules.GameRuleTypeVisitor;
import net.minecraft.world.level.gamerules.GameRules;
import org.jetbrains.annotations.NotNull;

public final class SettingsWatcher {
   private SettingsWatcher() {
   }

   public static Map<String, RuleValueSnapshot> takeSnapshot() {
      Map<String, Set<String>> defaultedRules = readAllDefaults();
      Map<String, RuleValueSnapshot> snap = new LinkedHashMap();
      CarpetGUI.forEachCarpetManager((mgr) -> {
         String id = mgr.identifier();
         Set<String> defaults = (Set)defaultedRules.getOrDefault(id, Set.of());

         for(CarpetRule<?> rule : mgr.getCarpetRules()) {
            String key = id + ":" + rule.name();
            String val = String.valueOf(rule.value()).toLowerCase();
            boolean isDefault = defaults.contains(rule.name());
            snap.put(key, new RuleValueSnapshot(val, isDefault));
         }

      });
      GameRules gameRules = CarpetGUI.getGamerules();
      gameRules.availableRules().toList().forEach((rule) -> {
         String var10000 = String.valueOf(rule.category());
         String key = "gamerule$" + var10000 + ":" + rule.id();
         String val = gameRules.get(rule).toString().toLowerCase();
         snap.put(key, new RuleValueSnapshot(val, false));
      });
      return snap;
   }

   private static Map<String, Set<String>> readAllDefaults() {
      Map<String, Set<String>> result = new HashMap();
      CarpetGUI.forEachCarpetManager((mgr) -> {
         Set<String> names = new HashSet();
         CarpetGUI.forEachCarpetManager((settingsManager) -> names.addAll(CarpetGUI.readDefaultSettingsFromConf(CarpetGUI.getCarpetDefaultsConfigFile(settingsManager))));
         names.addAll(CarpetGUI.readDefaultSettingsFromOrgConf());
         result.put(mgr.identifier(), names);
      });
      return result;
   }

   public static Map<String, RuleValueSnapshot> makeDefaultSnapshot() {
      Map<String, RuleValueSnapshot> snap = new LinkedHashMap();
      CarpetGUI.forEachCarpetManager((mgr) -> {
         String id = mgr.identifier();

         for(CarpetRule<?> rule : mgr.getCarpetRules()) {
            snap.put(id + ":" + rule.name(), new RuleValueSnapshot(rule.defaultValue().toString().toLowerCase(), false));
         }

      });
      GameRules gameRules = CarpetGUI.getGamerules();
      gameRules.availableRules().toList().forEach((rule) -> {
         String var10000 = String.valueOf(rule.category());
         String key = "gamerule$" + var10000 + ":" + rule.id();
         snap.put(key, new RuleValueSnapshot(rule.defaultValue().toString(), false));
      });
      return snap;
   }

   public static void applyRule(String ruleKey, RuleValueSnapshot snapshot, CommandSourceStack source) {
      boolean silentOrg = ((CommandSourceStackAccessor)source).carpetGUI$getSilent();
      ((CommandSourceStackAccessor)source).carpetGUI$setSilent(true);
      int sep = ruleKey.indexOf("]:") + 1;
      if (sep >= 0) {
         String gameruleId = ruleKey.substring(0, sep);
         String managerId;
         if (gameruleId.isEmpty()) {
            sep = ruleKey.indexOf(":");
            managerId = ruleKey.substring(0, sep);
         } else {
            managerId = gameruleId;
         }

         String ruleName = ruleKey.substring(sep + 1);
         if (managerId.startsWith("gamerule")) {
            GameRules gameRules = CarpetGUI.getGamerules();
            GameRule<?> rule = findRule(ruleName, gameRules);

            try {
               if (rule.gameRuleType() == GameRuleType.BOOL) {
                  gameRules.set((GameRule<Boolean>) rule, Boolean.parseBoolean(snapshot.value()), CarpetServer.minecraft_server);
               } else if (rule.gameRuleType() == GameRuleType.INT) {
                  gameRules.set((GameRule<Integer>) rule, Integer.parseInt(snapshot.value()), CarpetServer.minecraft_server);
               }
            } catch (Exception var11) {
            }
         } else {
            CarpetGUI.forEachCarpetManager((mgr) -> {
               if (mgr.identifier().equals(managerId)) {
                  CarpetRule<?> rule = mgr.getCarpetRule(ruleName);
                  if (rule != null) {
                     try {
                        if (snapshot.isDefault()) {
                           ((SettngsManagerAccessor)mgr).carpetGUI$setDefault(source, rule, snapshot.value());
                        } else {
                           ((SettngsManagerAccessor)mgr).carpetGUI$removeDefault(source, rule);
                        }

                        ((SettngsManagerAccessor)mgr).carpetGUI$setRule(source, rule, snapshot.value());
                     } catch (Exception var7) {
                     }

                  }
               }
            });
         }

         ((CommandSourceStackAccessor)source).carpetGUI$setSilent(silentOrg);
      }
   }

   private static GameRule<?> findRule(final String name, GameRules gameRules) {
      final GameRule<?>[] result = new GameRule[1];
      gameRules.visitGameRuleTypes(new GameRuleTypeVisitor() {
         public <T> void visit(@NotNull GameRule<T> gameRule) {
            String path = gameRule.getIdentifier().getPath();
            if (path.equals(name)) {
               result[0] = gameRule;
            }

         }
      });
      return result[0];
   }
}
