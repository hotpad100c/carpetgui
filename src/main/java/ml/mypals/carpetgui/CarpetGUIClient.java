package ml.mypals.carpetgui;

import com.mojang.blaze3d.platform.InputConstants;
import ml.mypals.carpetgui.network.RuleData;
import ml.mypals.carpetgui.network.client.CarpetGUIClientPacketHandler;
import ml.mypals.carpetgui.network.server.HelloPacketPayload;
import ml.mypals.carpetgui.network.server.RuleStackSyncPayload;
import ml.mypals.carpetgui.network.server.RulesPacketPayload;
import ml.mypals.carpetgui.screen.ruleStack.RuleStackData;
import ml.mypals.carpetgui.settings.CarpetGUIConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//? if <26.1 {

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//?} else {
/*import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
*///?}
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static ml.mypals.carpetgui.CarpetGUI.MOD_ID;

public class CarpetGUIClient implements ClientModInitializer {

    public static final Logger LOGGER = LoggerFactory.getLogger("carpetgui");
    public static final String VERSION = /*$ mod_version*/ "1.3.2";
    public static final String MINECRAFT = /*$ minecraft*/ "1.21.11";

    public static KeyMapping carpetRulesKeyBind;
    public static RuleStackData cachedRuleStackData;
    public static Map<String, RuleData> cachedCompleteRules = new HashMap<>();
    public static HashSet<RuleData> incompleteRulesFromServer = new HashSet<>();
    public static LinkedHashSet<String> cachedCategories = new LinkedHashSet<>();
    public static List<String> cachedManagers = List.of("carpet");
    public static CopyOnWriteArrayList<String> defaultRules = new CopyOnWriteArrayList<>();
    public static CopyOnWriteArrayList<String> favoriteRules = new CopyOnWriteArrayList<>();
    public static boolean hasModOnServer = false;
    public static boolean requesting = false;

    @Override
    @SuppressWarnings("resource")
    public void onInitializeClient() {
        CarpetGUIConfigManager.initializeConfig();

        //? if <26.1 {
        carpetRulesKeyBind = KeyBindingHelper.registerKeyBinding(new KeyMapping(
        //?} else {
        /*carpetRulesKeyBind = KeyMappingHelper.registerKeyMapping(new KeyMapping(
        *///?}
                "carpetgui.key.carpetRulesKeyBind",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_F9,
                //? if < 1.21.9 {
                /*"key.category.carpetgui.main"
                 *///?} else {
                KeyMapping.Category.register(Identifier.fromNamespaceAndPath(MOD_ID,"main"))
                //?}
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (carpetRulesKeyBind.consumeClick()) {
                CarpetGUIClientPacketHandler.openRuleEditScreen(true);
            }
        });

        ClientPlayConnectionEvents.DISCONNECT.register((listener, mc) -> {
            hasModOnServer = false;
            incompleteRulesFromServer.clear();
        });
        //? if >=1.20.5 {
        ClientPlayNetworking.registerGlobalReceiver(
                HelloPacketPayload.ID,
                (payload, context)-> CarpetGUIClientPacketHandler.handleHelloPacket(payload)
        );

        ClientPlayNetworking.registerGlobalReceiver(
                RuleStackSyncPayload.ID,
                (payload, context)-> CarpetGUIClientPacketHandler.handleRuleStackSync(payload)
        );

        ClientPlayNetworking.registerGlobalReceiver(
                RulesPacketPayload.ID,
                (payload, context)-> CarpetGUIClientPacketHandler.handleRulesPacket(payload)
        );
        //?} else {
        /*ClientPlayNetworking.registerGlobalReceiver(
                HelloPacketPayload.ID,
                (helloPacketPayload, localPlayer, packetSender) -> {
                    CarpetGUIClientPacketHandler.handleHelloPacket(helloPacketPayload);
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                RuleStackSyncPayload.ID,
                (ruleStackSyncPayload, localPlayer, packetSender) -> {
                    CarpetGUIClientPacketHandler.handleRuleStackSync(ruleStackSyncPayload);
                }
        );

        ClientPlayNetworking.registerGlobalReceiver(
                RulesPacketPayload.ID,
                (rulesPacketPayload, localPlayer, packetSender) -> {
                    CarpetGUIClientPacketHandler.handleRulesPacket(rulesPacketPayload);
                }
        );
        *///?}
    }





    public static String getServerAddress(Minecraft client) {
        ServerData data = client.getCurrentServer();
        if (data != null) return data.ip;
        if (client.hasSingleplayerServer()) return "singleplayer";
        return null;
    }
}