package ml.mypals.carpetgui;

import carpet.CarpetExtension;
import carpet.CarpetServer;

import carpet.CarpetSettings;
import carpet.api.settings.CarpetRule;
import carpet.api.settings.RuleHelper;
import carpet.api.settings.SettingsManager;
import carpet.utils.Translations;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import com.google.gson.Gson;
import ml.mypals.carpetgui.network.RuleData;
import ml.mypals.carpetgui.network.client.RequestRuleStackPayload;
import ml.mypals.carpetgui.network.client.RequestRulesPayload;
import ml.mypals.carpetgui.network.server.CarpetGUIServerPacketHandler;
import ml.mypals.carpetgui.network.server.HelloPacketPayload;
import ml.mypals.carpetgui.ruleStack.PrefabManager;
import ml.mypals.carpetgui.ruleStack.RuleStackCommand;
import ml.mypals.carpetgui.translate.TranslationHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
//? if < 1.21.11 {
/*import net.minecraft.world.level.GameRules;
import net.minecraft.commands.CommandSourceStack;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import ml.mypals.carpetgui.mixin.accessors.GameRulesAccessor;
import ml.mypals.carpetgui.mixin.accessors.TypeAccessor;
import java.util.Objects;
import static ml.mypals.carpetgui.settings.GamerulesDefaultValueSorter.gamerulesDefaultValues;
*///?} else {
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRules;
//?}
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static ml.mypals.carpetgui.translate.TranslationHelper.getDescTranslation;
import static ml.mypals.carpetgui.translate.TranslationHelper.getNameTranslation;

//? if >=1.20.5 {
import ml.mypals.carpetgui.network.server.RuleStackSyncPayload;
import ml.mypals.carpetgui.network.server.RulesPacketPayload;
import ml.mypals.carpetgui.ruleStack.Prefab;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
//?}

public class CarpetGUI implements ModInitializer, CarpetExtension {
    public static final Gson GSON = new Gson();

    public static final String MOD_ID = "carpetgui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String VERSION = /*$ mod_version*/ "1.3.2";
    public static final String MINECRAFT = /*$ minecraft*/ "1.21.11";

    private static PrefabManager prefabManager;

    @Override
    public void onInitialize() {
        CarpetServer.manageExtension(this);
        //? if >1.18.2 {
        CommandRegistrationCallback.EVENT.register(((commandDispatcher, commandBuildContext, commandSelection) -> {
            RuleStackCommand.register(commandDispatcher);
        }));
        //?} else {
        /*CommandRegistrationCallback.EVENT.register(((commandDispatcher, bl ) -> {
            RuleStackCommand.register(commandDispatcher);
        }));
        *///?}

        //? if >=1.20.5 {
        
        PayloadTypeRegistry.playC2S().register(RequestRulesPayload.ID, RequestRulesPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(RequestRuleStackPayload.ID, RequestRuleStackPayload.CODEC);

        PayloadTypeRegistry.playS2C().register(RulesPacketPayload.ID, RulesPacketPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(HelloPacketPayload.ID, HelloPacketPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RuleStackSyncPayload.ID, RuleStackSyncPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(RequestRulesPayload.ID,
            (payload, context) ->
                CarpetGUIServerPacketHandler.handleRequestRules(payload, context.player(),
                    //? if <1.21.9 {
                    /*Objects.requireNonNull(context.player().getServer())
                    *///?} else {
                    context.server()
                    //?}
                )
        );

        ServerPlayNetworking.registerGlobalReceiver(RequestRuleStackPayload.ID,
            (payload, context) ->
                CarpetGUIServerPacketHandler.handleRequestRuleStack(payload, context.player(),
                    //? if <1.21.9 {
                    /*Objects.requireNonNull(context.player().getServer())
                     *///?} else {
                    context.server()
                    //?}
                )
        );
        //?} else {

        /*ServerPlayNetworking.registerGlobalReceiver(RequestRuleStackPayload.ID.getId(),
                (server, player, handler, buf, responseSender) ->
                        CarpetGUIServerPacketHandler.handleRequestRuleStack(RequestRuleStackPayload.ID.read(buf), player, Objects.requireNonNull(player.getServer()))
        );
        ServerPlayNetworking.registerGlobalReceiver(RequestRulesPayload.ID.getId(),
                (server, player, handler, buf, responseSender) ->
                        CarpetGUIServerPacketHandler.handleRequestRules(RequestRulesPayload.ID.read(buf), player, Objects.requireNonNull(player.getServer()))
        );
        *///?}
        ServerPlayConnectionEvents.JOIN.register((impl, sender, server) -> {
            sender.sendPacket(new HelloPacketPayload());
        });
    }

    public static List<RuleData> getRules(String lang) {
        List<RuleData> rules = new ArrayList<>(getRules(CarpetServer.settingsManager, lang));
        for (CarpetExtension carpetExtension : CarpetServer.extensions) {
            //? if >1.18.2 {
            SettingsManager settingsManager = carpetExtension.extensionSettingsManager();
             //?} else {
            /*SettingsManager settingsManager = carpetExtension.customSettingsManager();
            *///?}
            if (settingsManager != null && !settingsManager.equals(CarpetServer.settingsManager)) {
                rules.addAll(getRules(settingsManager, lang));
            }
        }
        rules.addAll(getGamerulesAsRules());
        return rules;
    }

    public static List<RuleData> getGamerulesAsRules() {
        List<RuleData> fakeCarpetRules = new ArrayList<>();
        MinecraftServer server = CarpetServer.minecraft_server;
        if (server == null) {
            return new ArrayList<>();
        }
        //? if <1.21.11 {
        /*GameRulesAccessor rulesAccessor = ((GameRulesAccessor) getGamerules());
        for (Map.Entry<GameRules.Key<?>, GameRules.Value<?>> entry : rulesAccessor.carpetGUI$getRules().entrySet()) {
            GameRules.Key<?> rule = entry.getKey();
            GameRules.Value<?> value = entry.getValue();
            GameRules.Type<?> type = ((TypeAccessor) value).carpetGUI$getType();
            RequiredArgumentBuilder<CommandSourceStack, ?> argumentBuilder = type.createArgument("");

            fakeCarpetRules.add(new RuleData(
                    "gamerule",
                    rule.getId(),
                    rule.getId(),
                    value.getClass(),
                    gamerulesDefaultValues.get(rule),
                    String.valueOf(server.getGameRules().getRule(rule)),
                    rule.getDescriptionId(),
                    rule.getDescriptionId(),
                    argumentBuilder.getType().getExamples().stream().toList(),
                    List.of(Map.entry("gamerule", "gui.category.gamerules" + " : " + rule.getCategory().getDescriptionId()))
            ));
        }
        *///?} else {

        GameRules gameRules = getGamerules();
        for (GameRule<?> rule : gameRules.availableRules().toList()) {
            fakeCarpetRules.add(new RuleData(
                    "gamerule",
                    rule.id(),
                    rule.id(),
                    rule.valueClass(),
                    rule.defaultValue().toString(),
                    String.valueOf(gameRules.get(rule)),
                    rule.getDescriptionId(),
                    rule.getDescriptionId(),
                    rule.argument().getExamples().stream().toList(),
                    List.of(Map.entry("gamerule", "gui.category.gamerules" + " : " + rule.category().getDescriptionId()))
            ));
        }
        //?}
        return fakeCarpetRules;
    }
    public static GameRules getGamerules(){
        MinecraftServer server = CarpetServer.minecraft_server;
        //? if <1.21.11 {
        /*return server.getGameRules();
        *///?} else if <26.1 {
        return server.getWorldData().getGameRules();
        //?} else {
        /*return server.getGameRules();
        *///?}
    }
    public static List<RuleData> getRules(SettingsManager settingsManager, String lang) {
        List<RuleData> rules = new ArrayList<>();
        String managerID = settingsManager.identifier();

        String originalLang = CarpetSettings.language;

        CarpetSettings.language = "en_us";
        Translations.updateLanguage();

        Map<CarpetRule<?>, String> enNames = new HashMap<>();
        Map<CarpetRule<?>, String> enDescs = new HashMap<>();

        settingsManager.getCarpetRules().forEach(rule -> {
            enNames.put(rule, RuleHelper.translatedName(rule));
            enDescs.put(rule, RuleHelper.translatedDescription(rule));
        });

        CarpetSettings.language = lang;
        Translations.updateLanguage();

        for (var rule : settingsManager.getCarpetRules()) {

            String localName = RuleHelper.translatedName(rule);
            String localDescription = RuleHelper.translatedDescription(rule);

            List<Map.Entry<String, String>> translatedCategories =
                    rule.categories().stream()
                            .map(cat -> Map.entry(cat, RuleHelper.translatedCategory(managerID, cat)))
                            .toList();

            CarpetSettings.language = originalLang;
            Translations.updateLanguage();

            rules.add(new RuleData(
                    managerID,
                    enNames.get(rule),
                    localName,
                    rule.type(),
                    rule.defaultValue().toString().toLowerCase(),
                    rule.value().toString().toLowerCase(),
                    enDescs.get(rule),
                    localDescription,
                    rule.suggestions().stream().toList(),
                    translatedCategories
            ));
        }


        return rules;
    }

    public static String getDefaults() {
        StringBuilder defaults = new StringBuilder();

        forEachCarpetManager(settingsManager ->
                readDefaultSettingsFromConf(getCarpetDefaultsConfigFile(settingsManager))
                        .forEach(c -> defaults.append(c).append(";"))
        );

        readDefaultSettingsFromOrgConf().forEach(c -> defaults.append(c).append(";"));



        return defaults.toString();
    }

    public static List<String> readDefaultSettingsFromConf(Path path) {
        try {
            BufferedReader reader = Files.newBufferedReader(path);
            String line = "";
            List<String> result = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("[\\r\\n]", "");
                String[] fields = line.split("\\s+", 2);
                if (fields.length > 1) {
                    if (result.isEmpty() && fields[0].startsWith("#") || fields[1].startsWith("#")) {
                        continue;
                    }
                    result.add(fields[0]);
                }
            }
            return result;
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }
    public static List<String> readDefaultSettingsFromOrgConf() {
        if (!FabricLoader.getInstance().isModLoaded("carpet-org-addition")) {
            return new ArrayList<>();
        }
        try (BufferedReader reader = Files.newBufferedReader(getOrgDefaultsConfigFile())) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject rules = root.getAsJsonObject("rules");
            List<String> result = new ArrayList<>();
            if (rules != null) {
                for (Map.Entry<String, JsonElement> entry : rules.entrySet()) {
                    result.add(entry.getKey());
                }
            }
            return result;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static Path getCarpetDefaultsConfigFile(SettingsManager settingsManager) {
        return CarpetServer.minecraft_server.getWorldPath(LevelResource.ROOT)
                //? if >1.18.2 {
                .resolve(settingsManager.identifier() + ".conf");
                //?} else {
                /*.resolve(settingsManager.getIdentifier() + ".conf");
                *///?}
    }
    public static Path getOrgDefaultsConfigFile() {
        return CarpetServer.minecraft_server.getWorldPath(LevelResource.ROOT)
                .resolve("carpetorgaddition/config.json");
    }

    @Override
    public void onServerLoadedWorlds(MinecraftServer server) {
        prefabManager = new PrefabManager(server);
        prefabManager.init();
    }

    @Override
    public void onServerClosed(MinecraftServer server) {
        prefabManager = null;
    }

    public static PrefabManager getPrefabManager() {
        return prefabManager;
    }

    public static void forEachCarpetManager(Consumer<SettingsManager> consumer)
    {
        consumer.accept(CarpetServer.settingsManager);
        for (CarpetExtension e : CarpetServer.extensions)
        {
            //? if >1.18.2 {
            SettingsManager manager = e.extensionSettingsManager();
            //?} else {
            /*SettingsManager manager = e.customSettingsManager();
            *///?}
            if (manager != null)
            {
                consumer.accept(manager);
            }
        }
    }
}