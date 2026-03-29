package ml.mypals.carpetgui.screen.ruleStack;

import ml.mypals.carpetgui.network.server.RuleStackSyncPayload;

import java.util.List;

public record RuleStackData (String activePrefabName, List<String> allPrefabNames, List<RuleStackSyncPayload.LayerInfo> layers, List<RuleStackSyncPayload.ChangeInfo> pendingChanges){
}
