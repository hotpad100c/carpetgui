package ml.mypals.carpetgui.screen.ruleStack;

import java.util.List;
import ml.mypals.carpetgui.network.server.RuleStackSyncPayload;

public record RuleStackData(String activePrefabName, List<String> allPrefabNames, List<RuleStackSyncPayload.LayerInfo> layers, List<RuleStackSyncPayload.ChangeInfo> pendingChanges, List<RuleStackSyncPayload.LayerInfo> futureLayers) {
}
