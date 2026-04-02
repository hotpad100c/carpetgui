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
import net.minecraft.client.Minecraft;

import java.util.*;

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
            boolean fromRuleGroupScreen = client.screen instanceof RuleGroupScreen ruleGroupScreen && ruleGroupScreen.requestingRulesForNewGroup;

            CarpetGUIClient.hasModOnServer = true;

            CarpetGUIClient.cachedRules.clear();
            CarpetGUIClient.cachedRules.addAll(payload.rules());

            cachedCategories.clear();
            for (var entry : RulesEditScreen.DefaultCategory.values()) {
                if (!entry.equals(RulesEditScreen.DefaultCategory.SEARCHING)) {
                    cachedCategories.add(entry.getName());
                }
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
            if (!fromRuleGroupScreen) {
                defaultRules.addAll(Arrays.stream(payload.defaults().split(";")).toList());
            }

            favoriteRules.clear();
            favoriteRules.addAll(CarpetGUIConfigManager.readFavoriteRules());

            String lang = client.getLanguageManager().getSelected();

            new Thread(() -> {
                RulesCacheManager.saveCache(cachedRules, payload.defaults(), lang);
                cachedManagers = RulesCacheManager.loadKnownManagers();
            }, "carpetgui-cache-save").start();

            requesting = false;

            client.setScreen(new RulesEditScreen(!fromRuleGroupScreen));
        });
    }
}
