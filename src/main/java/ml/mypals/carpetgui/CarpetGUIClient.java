package ml.mypals.carpetgui;

import com.mojang.blaze3d.platform.InputConstants;
import ml.mypals.carpetgui.localChache.RulesCacheManager;
import ml.mypals.carpetgui.network.RuleData;
import ml.mypals.carpetgui.network.client.RequestRulesPayload;
import ml.mypals.carpetgui.network.server.HelloPacketPayload;
import ml.mypals.carpetgui.network.server.RulesPacketPayload;
import ml.mypals.carpetgui.screen.ScreenSwitcherScreen;
import ml.mypals.carpetgui.screen.ruleGroup.RuleGroupScreen;
import ml.mypals.carpetgui.screen.rulesEditScreen.RulesEditScreen;
import ml.mypals.carpetgui.settings.CarpetGUIConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CarpetGUIClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("carpetgui");
    public static final String VERSION = /*$ mod_version*/ "1.0.0";
    public static final String MINECRAFT = /*$ minecraft*/ "1.21.1";

    public static KeyMapping carpetRulesKeyBind;
    public static List<RuleData> cachedRules = new ArrayList<>();
    public static List<RuleData> rulesFromServer = new ArrayList<>();
    public static List<String> cachedCategories = new ArrayList<>();
    public static List<String> cachedManagers = List.of("carpet");
    public static CopyOnWriteArrayList<String> defaultRules = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<String> favoriteRules = new CopyOnWriteArrayList<>();
    public static boolean hasModOnServer = false;
    public static boolean requesting = false;

    @Override
    @SuppressWarnings("resource")
    public void onInitializeClient() {
        CarpetGUIConfigManager.initializeConfig();

        ScreenSwitcherScreen.registerEntry(
                Component.translatable("carpetgui.screen.rules"),
                new ItemStack(Items.KNOWLEDGE_BOOK),
                () -> openRuleEditScreen(true)
        );
        ScreenSwitcherScreen.registerEntry(
                Component.translatable("carpetgui.screen.rule_groups"),
                new ItemStack(Items.DRIED_KELP_BLOCK),
                () -> {
                    Minecraft.getInstance().setScreen(new RuleGroupScreen());
                }
        );
        ScreenSwitcherScreen.registerEntry(
                Component.translatable("carpetgui.screen.rule_stack"),
                new ItemStack(Items.NETHERITE_SCRAP),
                () -> {}
        );


        carpetRulesKeyBind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.carpetRulesKeyBind",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F9,
                "key.category.carpetgui.name"
        ));
        ScreenSwitcherScreen.setTriggerKey(carpetRulesKeyBind);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (carpetRulesKeyBind.consumeClick()) {
                ScreenSwitcherScreen screen = new ScreenSwitcherScreen();
                Minecraft.getInstance().setScreen(screen);
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((listener, mc) -> {
            hasModOnServer = false;
            rulesFromServer.clear();
        });
        ClientPlayNetworking.registerGlobalReceiver(HelloPacketPayload.ID,
                (payload, context) -> context.client().execute(() -> {
                hasModOnServer = true;
                })
        );


        ClientPlayNetworking.registerGlobalReceiver(RulesPacketPayload.ID,
                (payload, context) -> context.client().execute(() -> {
                    Minecraft client = Minecraft.getInstance();
                    boolean fromRuleGroupScreen = client.screen instanceof RuleGroupScreen;
                    hasModOnServer = true;

                    cachedRules.clear();
                    cachedRules.addAll(payload.rules());

                    cachedCategories.clear();
                    for (var entry : RulesEditScreen.DefaultCategory.values()) {
                        if (!entry.equals(RulesEditScreen.DefaultCategory.SEARCHING))
                            cachedCategories.add(entry.getName());
                    }
                    Set<String> categories = new LinkedHashSet<>();

                    ListIterator<RuleData> it = cachedRules.listIterator();
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
                    if(!fromRuleGroupScreen){
                        defaultRules.addAll(Arrays.stream(payload.defaults().split(";")).toList());
                    }

                    favoriteRules.clear();
                    favoriteRules.addAll(CarpetGUIConfigManager.readFavoriteRules());
                    String lang = client.getLanguageManager().getSelected();
                    Thread.ofVirtual().name("carpetgui-cache-save").start(
                            () ->{
                                RulesCacheManager.saveCache(cachedRules, payload.defaults(), lang);
                                cachedManagers = RulesCacheManager.loadKnownManagers();
                            }
                    );

                    requesting = false;
                    client.setScreen(new RulesEditScreen(!fromRuleGroupScreen));
                }));
    }
    public static void openRuleEditScreen(boolean instantAffect){
        Minecraft client = Minecraft.getInstance();
        if (hasModOnServer) {
            String lang = client.getLanguageManager().getSelected();
            ClientPlayNetworking.send(new RequestRulesPayload(lang));
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
                cachedRules.clear();
                cachedRules.addAll(cache.rules());

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

                if (rulesFromServer != null && !rulesFromServer.isEmpty()) {
                    Map<String, RuleData> cachedMap = new HashMap<>();
                    for (RuleData rule : cachedRules) {
                        if (rule.name != null) {
                            cachedMap.put(rule.name, rule);
                        }
                    }

                    for (RuleData serverRule : rulesFromServer) {
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

                            cachedRules.add(newRule);
                            cachedMap.put(newRule.name, newRule);
                        }
                    }

                    Iterator<RuleData> iterator = cachedRules.iterator();
                    while (iterator.hasNext()) {
                        RuleData cachedRule = iterator.next();
                        if (cachedRule.name == null) continue;

                        boolean existsOnServer = rulesFromServer.stream()
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


    private static String getServerAddress(Minecraft client) {
        ServerData data = client.getCurrentServer();
        if (data != null) return data.ip;
        if (client.hasSingleplayerServer()) return "singleplayer";
        return null;
    }
}