package ml.mypals.carpetgui;

import carpet.CarpetExtension;
import carpet.CarpetServer;
import carpet.api.settings.CarpetRule;
import carpet.api.settings.RuleHelper;
import carpet.api.settings.SettingsManager;
import com.google.gson.Gson;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import ml.mypals.carpetgui.mixin.accessors.GameRulesAccessor;
import ml.mypals.carpetgui.mixin.accessors.TypeAccessor;
import ml.mypals.carpetgui.network.RuleData;
import ml.mypals.carpetgui.network.client.RequestRulesPayload;
import ml.mypals.carpetgui.network.server.HelloPacketPayload;
import ml.mypals.carpetgui.network.server.RulesPacketPayload;
import ml.mypals.carpetgui.translate.TranslationHelper;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ml.mypals.carpetgui.settings.GamerulesDefaultValueSorter.gamerulesDefaultValues;
import static ml.mypals.carpetgui.translate.TranslationHelper.getDescTranslation;
import static ml.mypals.carpetgui.translate.TranslationHelper.getNameTranslation;

public class CarpetGUI implements ModInitializer {
    private static final Gson GSON = new Gson();

    public static final String MOD_ID = "carpetgui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final String VERSION = /*$ mod_version*/ "1.0.0";
    public static final String MINECRAFT = /*$ minecraft*/ "1.21.1";

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(RequestRulesPayload.ID, RequestRulesPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(RulesPacketPayload.ID, RulesPacketPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(HelloPacketPayload.ID, HelloPacketPayload.CODEC);
        ServerPlayConnectionEvents.JOIN.register((impl, sender, server)->{
            sender.sendPacket(new HelloPacketPayload());
        });
        ServerPlayNetworking.registerGlobalReceiver(RequestRulesPayload.ID,

                (payload, context) -> context.server().execute(() -> {
                    String lang = payload.lang();
                    ServerPlayer player = context.player();

                    RulesPacketPayload rulesPacketPayload = new RulesPacketPayload(this.getRules(lang), getDefaults());

                    ServerPlayNetworking.send(player, rulesPacketPayload);

                })
        );
    }
    public List<RuleData> getRules(String lang) {
        List<RuleData> rules = new ArrayList<>(getRules(CarpetServer.settingsManager, lang));
        for(CarpetExtension carpetExtension : CarpetServer.extensions){
            SettingsManager settingsManager = carpetExtension.extensionSettingsManager();
            if(settingsManager != null && !settingsManager.equals(CarpetServer.settingsManager)){
                rules.addAll(getRules(settingsManager, lang));
            }
        }
        rules.addAll(getGamerulesAsRules());
        return rules;
    }
    public List<RuleData> getGamerulesAsRules(){
        List<RuleData> fakeCarpetRules = new ArrayList<>();
        MinecraftServer server = CarpetServer.minecraft_server;
        if(server == null){
            return new ArrayList<>();
        }
        GameRulesAccessor rulesAccessor = ((GameRulesAccessor) server.getGameRules());
        for(Map.Entry<GameRules.Key<?>, GameRules.Value<?>> entry : rulesAccessor.carpetGUI$getRules().entrySet()){
            GameRules.Key<?> rule = entry.getKey();
            GameRules.Value<?> value = entry.getValue();
            GameRules.Type<?> type = ((TypeAccessor)value).carpetGUI$getType();
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
        return fakeCarpetRules;
    }
    public List<RuleData> getRules(SettingsManager settingsManager, String lang) {
        List<RuleData> rules = new ArrayList<>();
        String managerID = settingsManager.identifier();
        settingsManager.getCarpetRules().forEach(rule -> {
            if (rule instanceof CarpetRule<?>) {
                String orName = rule.name();
                String localName = lang.equals("en_us")?orName:
                        getNameTranslation(lang, managerID, rule.name());
                String orgDesc = RuleHelper.translatedDescription(rule);
                String localDescription =
                        lang.equals("en_us")?orgDesc:
                        getDescTranslation(lang, managerID, rule.name());

                List<Map.Entry<String,String>> translatedCategories = rule.categories().stream().map(
                        cat -> {
                            String translated = TranslationHelper.getCategoryTranslation(lang,managerID,cat);
                            return Map.entry(cat, translated);
                        }
                        ).toList();

                rules.add(
                        new RuleData(
                                managerID,
                                orName,
                                localName,
                                rule.type(),
                                rule.defaultValue().toString(),
                                rule.value().toString(),
                                orgDesc,
                                localDescription,
                                rule.suggestions().stream().toList(),
                                translatedCategories.stream().toList())
                );
            }
        });
        return rules;
    }
    public String getDefaults() {
        StringBuilder defaults = new StringBuilder();
        readSettingsFromConf(getConfigFile()).forEach(c -> {
            defaults.append(c).append(";");
        });
        return defaults.toString();
    }

    private List<String> readSettingsFromConf(Path path) {
        try (BufferedReader reader = Files.newBufferedReader(path)) {
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
    private Path getConfigFile() {
        return CarpetServer.minecraft_server.getWorldPath(LevelResource.ROOT).resolve(CarpetServer.settingsManager.identifier() + ".conf");
    }


}