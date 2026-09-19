package ml.mypals.carpetgui;

import com.mojang.blaze3d.platform.InputConstants.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import ml.mypals.carpetgui.network.RuleData;
import ml.mypals.carpetgui.network.client.CarpetGUIClientPacketHandler;
import ml.mypals.carpetgui.network.server.HelloPacketPayload;
import ml.mypals.carpetgui.network.server.RuleStackSyncPayload;
import ml.mypals.carpetgui.network.server.RulesPacketPayload;
import ml.mypals.carpetgui.screen.ruleStack.RuleStackData;
import ml.mypals.carpetgui.settings.CarpetGUIConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping.Category;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CarpetGUIClient implements ClientModInitializer {
   public static final Logger LOGGER = LoggerFactory.getLogger("carpetgui");
   public static final String VERSION = /*$ mod_version*/ "1.3.6";
   public static final String MINECRAFT = /*$ minecraft*/ "26.2";
   public static KeyMapping carpetRulesKeyBind;
   public static RuleStackData cachedRuleStackData;
   public static Map<String, RuleData> cachedCompleteRules = new HashMap();
   public static HashSet<RuleData> incompleteRulesFromServer = new HashSet();
   public static LinkedHashSet<String> cachedCategories = new LinkedHashSet();
   public static List<String> cachedManagers = List.of("carpet");
   public static CopyOnWriteArrayList<String> defaultRules = new CopyOnWriteArrayList();
   public static CopyOnWriteArrayList<String> favoriteRules = new CopyOnWriteArrayList();
   public static boolean hasModOnServer = false;
   public static boolean requesting = false;

   public void onInitializeClient() {
      CarpetGUIConfigManager.initializeConfig();
      carpetRulesKeyBind = KeyMappingHelper.registerKeyMapping(new KeyMapping("carpetgui.key.carpetRulesKeyBind", Type.KEYSYM, 298, Category.register(Identifier.fromNamespaceAndPath("carpetgui", "main"))));
      ClientTickEvents.END_CLIENT_TICK.register((ClientTickEvents.EndTick)(client) -> {
         if (carpetRulesKeyBind.consumeClick()) {
            CarpetGUIClientPacketHandler.openRuleEditScreen(true);
         }

      });
      ClientPlayConnectionEvents.DISCONNECT.register((ClientPlayConnectionEvents.Disconnect)(listener, mc) -> {
         hasModOnServer = false;
         incompleteRulesFromServer.clear();
      });
      ClientPlayNetworking.registerGlobalReceiver(HelloPacketPayload.ID, (payload, context) -> CarpetGUIClientPacketHandler.handleHelloPacket(payload));
      ClientPlayNetworking.registerGlobalReceiver(RuleStackSyncPayload.ID, (payload, context) -> CarpetGUIClientPacketHandler.handleRuleStackSync(payload));
      ClientPlayNetworking.registerGlobalReceiver(RulesPacketPayload.ID, (payload, context) -> CarpetGUIClientPacketHandler.handleRulesPacket(payload));
   }

   public static String getServerAddress(Minecraft client) {
      ServerData data = client.getCurrentServer();
      if (data != null) {
         return data.ip;
      } else {
         return client.hasSingleplayerServer() ? "singleplayer" : null;
      }
   }
}
