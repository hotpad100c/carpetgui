package ml.mypals.carpetgui;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.CarpetSettings;
import carpet.api.settings.CarpetRule;
import carpet.api.settings.RuleHelper;
import carpet.api.settings.SettingsManager;
import carpet.utils.Translations;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import ml.mypals.carpetgui.network.RuleData;
import ml.mypals.carpetgui.network.client.RequestRuleStackPayload;
import ml.mypals.carpetgui.network.client.RequestRulesPayload;
import ml.mypals.carpetgui.network.server.CarpetGUIServerPacketHandler;
import ml.mypals.carpetgui.network.server.HelloPacketPayload;
import ml.mypals.carpetgui.network.server.RuleStackSyncPayload;
import ml.mypals.carpetgui.network.server.RulesPacketPayload;
import ml.mypals.carpetgui.ruleStack.PrefabManager;
import ml.mypals.carpetgui.ruleStack.RuleStackCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarpetGUI implements ModInitializer, CarpetExtension {
   public static final Gson GSON = new Gson();
   public static final String MOD_ID = "carpetgui";
   public static final Logger LOGGER = LoggerFactory.getLogger("carpetgui");
   public static final String VERSION = /*$ mod_version*/ "1.3.6";
   public static final String MINECRAFT = /*$ minecraft*/ "26.2";
   private static PrefabManager prefabManager;

   public void onInitialize() {
      CarpetServer.manageExtension(this);
      CommandRegistrationCallback.EVENT.register((CommandRegistrationCallback)(commandDispatcher, commandBuildContext, commandSelection) -> RuleStackCommand.register(commandDispatcher));
      PayloadTypeRegistry.serverboundPlay().register(RequestRulesPayload.ID, RequestRulesPayload.CODEC);
      PayloadTypeRegistry.serverboundPlay().register(RequestRuleStackPayload.ID, RequestRuleStackPayload.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(RulesPacketPayload.ID, RulesPacketPayload.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(HelloPacketPayload.ID, HelloPacketPayload.CODEC);
      PayloadTypeRegistry.clientboundPlay().register(RuleStackSyncPayload.ID, RuleStackSyncPayload.CODEC);
      ServerPlayNetworking.registerGlobalReceiver(RequestRulesPayload.ID, (payload, context) -> CarpetGUIServerPacketHandler.handleRequestRules(payload, context.player(), context.server()));
      ServerPlayNetworking.registerGlobalReceiver(RequestRuleStackPayload.ID, (payload, context) -> CarpetGUIServerPacketHandler.handleRequestRuleStack(payload, context.player(), context.server()));
      ServerPlayConnectionEvents.JOIN.register((ServerPlayConnectionEvents.Join)(impl, sender, server) -> sender.sendPacket(new HelloPacketPayload()));
   }

   public static List<RuleData> getRules(String lang) {
      List<RuleData> rules = new ArrayList(getRules(CarpetServer.settingsManager, lang));

      for(CarpetExtension carpetExtension : CarpetServer.extensions) {
         SettingsManager settingsManager = carpetExtension.extensionSettingsManager();
         if (settingsManager != null && !settingsManager.equals(CarpetServer.settingsManager)) {
            rules.addAll(getRules(settingsManager, lang));
         }
      }

      rules.addAll(getGamerulesAsRules());
      return rules;
   }

   public static List<RuleData> getGamerulesAsRules() {
      List<RuleData> fakeCarpetRules = new ArrayList();
      MinecraftServer server = CarpetServer.minecraft_server;
      if (server == null) {
         return new ArrayList();
      } else {
         GameRules gameRules = getGamerules();

         for(GameRule<?> rule : gameRules.availableRules().toList()) {
            fakeCarpetRules.add(new RuleData("gamerule", rule.id(), rule.id(), rule.valueClass(), rule.defaultValue().toString(), String.valueOf(gameRules.get(rule)), rule.getDescriptionId(), rule.getDescriptionId(), rule.argument().getExamples().stream().toList(), List.of(Map.entry("gamerule", "gui.category.gamerules : " + String.valueOf(rule.category().getDescriptionId())))));
         }

         return fakeCarpetRules;
      }
   }

   public static GameRules getGamerules() {
      MinecraftServer server = CarpetServer.minecraft_server;
      return server.getGameRules();
   }

   public static List<RuleData> getRules(SettingsManager settingsManager, String lang) {
      List<RuleData> rules = new ArrayList();
      String managerID = settingsManager.identifier();
      String originalLang = CarpetSettings.language;
      CarpetSettings.language = "en_us";
      Translations.updateLanguage();
      Map<CarpetRule<?>, String> enNames = new HashMap();
      Map<CarpetRule<?>, String> enDescs = new HashMap();
      settingsManager.getCarpetRules().forEach((rulex) -> {
         enNames.put(rulex, rulex.name());
         enDescs.put(rulex, RuleHelper.translatedDescription(rulex));
      });
      CarpetSettings.language = lang;
      Translations.updateLanguage();

      for(CarpetRule<?> rule : settingsManager.getCarpetRules()) {
         String localName = RuleHelper.translatedName(rule);
         String localDescription = RuleHelper.translatedDescription(rule);
         List<Map.Entry<String, String>> translatedCategories = rule.categories().stream().map((cat) -> Map.entry(cat, RuleHelper.translatedCategory(managerID, cat))).toList();
         String enName = (String)enNames.get(rule);
         rules.add(new RuleData(managerID, enName, localName, rule.type(), rule.defaultValue().toString().toLowerCase(), enName.equals("language") ? originalLang : rule.value().toString().toLowerCase(), (String)enDescs.get(rule), localDescription, rule.suggestions().stream().toList(), translatedCategories));
      }

      CarpetSettings.language = originalLang;
      Translations.updateLanguage();
      return rules;
   }

   public static String getDefaults() {
      StringBuilder defaults = new StringBuilder();
      forEachCarpetManager((settingsManager) -> readDefaultSettingsFromConf(getCarpetDefaultsConfigFile(settingsManager)).forEach((c) -> defaults.append(c).append(";")));
      readDefaultSettingsFromOrgConf().forEach((c) -> defaults.append(c).append(";"));
      return defaults.toString();
   }

   public static List<String> readDefaultSettingsFromConf(Path path) {
      try {
         BufferedReader reader = Files.newBufferedReader(path);
         String line = "";
         List<String> result = new ArrayList();

         while((line = reader.readLine()) != null) {
            line = line.replaceAll("[\\r\\n]", "");
            String[] fields = line.split("\\s+", 2);
            if (fields.length > 1 && (!result.isEmpty() || !fields[0].startsWith("#")) && !fields[1].startsWith("#")) {
               result.add(fields[0]);
            }
         }

         return result;
      } catch (IOException var5) {
         return new ArrayList();
      }
   }

   public static List<String> readDefaultSettingsFromOrgConf() {
      if (!FabricLoader.getInstance().isModLoaded("carpet-org-addition")) {
         return new ArrayList();
      } else {
         try {
            BufferedReader reader = Files.newBufferedReader(getOrgDefaultsConfigFile());

            Object var9;
            try {
               JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
               JsonObject rules = root.getAsJsonObject("rules");
               List<String> result = new ArrayList();
               if (rules != null) {
                  for(Map.Entry<String, JsonElement> entry : rules.entrySet()) {
                     result.add((String)entry.getKey());
                  }
               }

               var9 = result;
            } catch (Throwable var7) {
               if (reader != null) {
                  try {
                     reader.close();
                  } catch (Throwable var6) {
                     var7.addSuppressed(var6);
                  }
               }

               throw var7;
            }

            if (reader != null) {
               reader.close();
            }

            return (List<String>)var9;
         } catch (Exception var8) {
            return new ArrayList();
         }
      }
   }

   public static Path getCarpetDefaultsConfigFile(SettingsManager settingsManager) {
      return CarpetServer.minecraft_server.getWorldPath(LevelResource.ROOT).resolve(settingsManager.identifier() + ".conf");
   }

   public static Path getOrgDefaultsConfigFile() {
      return CarpetServer.minecraft_server.getWorldPath(LevelResource.ROOT).resolve("carpetorgaddition/config.json");
   }

   public void onServerLoadedWorlds(MinecraftServer server) {
      prefabManager = new PrefabManager(server);
      prefabManager.init();
   }

   public void onServerClosed(MinecraftServer server) {
      prefabManager = null;
   }

   public static PrefabManager getPrefabManager() {
      return prefabManager;
   }

   public static void forEachCarpetManager(Consumer<SettingsManager> consumer) {
      consumer.accept(CarpetServer.settingsManager);

      for(CarpetExtension e : CarpetServer.extensions) {
         SettingsManager manager = e.extensionSettingsManager();
         if (manager != null) {
            consumer.accept(manager);
         }
      }

   }
}
