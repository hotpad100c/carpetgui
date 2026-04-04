package ml.mypals.carpetgui.network.server;

import ml.mypals.carpetgui.CarpetGUI;
import ml.mypals.carpetgui.CarpetGUIClient;
import ml.mypals.carpetgui.localChache.RulesCacheManager;
import ml.mypals.carpetgui.network.RuleData;
import ml.mypals.carpetgui.network.client.RequestRuleStackPayload;
import ml.mypals.carpetgui.network.client.RequestRulesPayload;
import ml.mypals.carpetgui.ruleStack.Prefab;
import ml.mypals.carpetgui.ruleStack.PrefabManager;
import ml.mypals.carpetgui.ruleStack.RuleChange;
import ml.mypals.carpetgui.ruleStack.RuleLayer;
import ml.mypals.carpetgui.screen.ruleGroup.RuleGroupScreen;
import ml.mypals.carpetgui.screen.ruleStack.RuleStackData;
import ml.mypals.carpetgui.screen.ruleStack.RuleStackScreen;
import ml.mypals.carpetgui.screen.rulesEditScreen.RulesEditScreen;
import ml.mypals.carpetgui.settings.CarpetGUIConfigManager;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.*;

import static ml.mypals.carpetgui.CarpetGUIClient.*;

public class CarpetGUIServerPacketHandler {
    public static void handleRequestRules(RequestRulesPayload payload, ServerPlayer player, MinecraftServer server) {
        server.execute(() -> {

            String lang = payload.lang();
            List<RuleData> allRules = CarpetGUI.getRules(lang);
            List<String> known = payload.knownRuleNames();

            List<RuleData> toSend = known.isEmpty()
                    ? allRules
                    : allRules.stream()
                    .filter(r -> !known.contains(r.name))
                    .toList();

            ServerPlayNetworking.send(player, new RulesPacketPayload(toSend, CarpetGUI.getDefaults(), !known.isEmpty()));
        });
    }

    public static void handleRequestRuleStack(RequestRuleStackPayload payload, ServerPlayer player, MinecraftServer server) {
        server.execute(() -> {
            PrefabManager mgr = CarpetGUI.getPrefabManager();
            if (mgr == null) return;

            Prefab active = mgr.getActivePrefab();

            List<RuleStackSyncPayload.LayerInfo> layerInfos = active.getLayers().stream()
                    .map(CarpetGUIServerPacketHandler::convertLayerToInfo)
                    .toList();

            List<RuleStackSyncPayload.LayerInfo> futureLayerInfos = active.getFutureLayers().stream()
                    .map(CarpetGUIServerPacketHandler::convertLayerToInfo)
                    .toList();

            List<RuleStackSyncPayload.ChangeInfo> pending = mgr.getPendingChanges().stream()
                    .map(CarpetGUIServerPacketHandler::convertChangeToInfo)
                    .toList();

            ServerPlayNetworking.send(player, new RuleStackSyncPayload(
                    mgr.getActiveName(),
                    mgr.getAllPrefabs().stream().map(Prefab::getName).toList(),
                    layerInfos,
                    pending,
                    futureLayerInfos
            ));
        });
    }

    private static RuleStackSyncPayload.LayerInfo convertLayerToInfo(RuleLayer layer) {
        return new RuleStackSyncPayload.LayerInfo(
                layer.getId(),
                layer.getMessage(),
                layer.getTimestamp(),
                layer.getChanges().stream().map(CarpetGUIServerPacketHandler::convertChangeToInfo).toList()
        );
    }

    private static RuleStackSyncPayload.ChangeInfo convertChangeToInfo(RuleChange c) {
        return new RuleStackSyncPayload.ChangeInfo(
                c.ruleKey(),
                c.previousSnapshot().value(), c.previousSnapshot().isDefault(),
                c.newSnapshot().value(),      c.newSnapshot().isDefault()
        );
    }
}
