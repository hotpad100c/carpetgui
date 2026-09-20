package ml.mypals.carpetgui.network.server;

import java.util.List;
import ml.mypals.carpetgui.CarpetGUI;
import ml.mypals.carpetgui.network.RuleData;
import ml.mypals.carpetgui.network.client.RequestRuleStackPayload;
import ml.mypals.carpetgui.network.client.RequestRulesPayload;
import ml.mypals.carpetgui.ruleStack.Prefab;
import ml.mypals.carpetgui.ruleStack.PrefabManager;
import ml.mypals.carpetgui.ruleStack.RuleChange;
import ml.mypals.carpetgui.ruleStack.RuleLayer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
//? if <1.19.4 {
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
//?}
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class CarpetGUIServerPacketHandler {
   public static void handleRequestRules(RequestRulesPayload payload, ServerPlayer player, MinecraftServer server) {
      server.execute(() -> {
         String lang = payload.lang();
         List<RuleData> allRules = CarpetGUI.getRules(lang);
         List<String> known = payload.knownRuleNames();
         List<RuleData> toSend = known.isEmpty() ? allRules : allRules.stream().filter((r) -> !known.contains(r.name)).toList();
         //? if <1.19.4 {
         FriendlyByteBuf buf = PacketByteBufs.create();
         new RulesPacketPayload(toSend, CarpetGUI.getDefaults(), !known.isEmpty()).write(buf);
         ServerPlayNetworking.send(player, RulesPacketPayload.ID, buf);
         //?} else {
         /*ServerPlayNetworking.send(player, new RulesPacketPayload(toSend, CarpetGUI.getDefaults(), !known.isEmpty()));
         *///?}
      });
   }

   public static void handleRequestRuleStack(RequestRuleStackPayload payload, ServerPlayer player, MinecraftServer server) {
      server.execute(() -> {
         PrefabManager mgr = CarpetGUI.getPrefabManager();
         if (mgr != null) {
            Prefab active = mgr.getActivePrefab();
            List<RuleStackSyncPayload.LayerInfo> layerInfos = active.getLayers().stream().map(CarpetGUIServerPacketHandler::convertLayerToInfo).toList();
            List<RuleStackSyncPayload.LayerInfo> futureLayerInfos = active.getFutureLayers().stream().map(CarpetGUIServerPacketHandler::convertLayerToInfo).toList();
            List<RuleStackSyncPayload.ChangeInfo> pending = mgr.getPendingChanges().stream().map(CarpetGUIServerPacketHandler::convertChangeToInfo).toList();
            //? if <1.19.4 {
            FriendlyByteBuf buf = PacketByteBufs.create();
            new RuleStackSyncPayload(mgr.getActiveName(), mgr.getAllPrefabs().stream().map(Prefab::getName).toList(), layerInfos, pending, futureLayerInfos).write(buf);
            ServerPlayNetworking.send(player, RuleStackSyncPayload.ID, buf);
            //?} else {
            /*ServerPlayNetworking.send(player, new RuleStackSyncPayload(mgr.getActiveName(), mgr.getAllPrefabs().stream().map(Prefab::getName).toList(), layerInfos, pending, futureLayerInfos));
            *///?}
         }
      });
   }

   private static RuleStackSyncPayload.LayerInfo convertLayerToInfo(RuleLayer layer) {
      return new RuleStackSyncPayload.LayerInfo(layer.getId(), layer.getMessage(), layer.getTimestamp(), layer.getChanges().stream().map(CarpetGUIServerPacketHandler::convertChangeToInfo).toList());
   }

   private static RuleStackSyncPayload.ChangeInfo convertChangeToInfo(RuleChange c) {
      return new RuleStackSyncPayload.ChangeInfo(c.ruleKey(), c.previousSnapshot().value(), c.previousSnapshot().isDefault(), c.newSnapshot().value(), c.newSnapshot().isDefault());
   }
}
