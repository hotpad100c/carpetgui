package ml.mypals.carpetgui.mixin;

import carpet.network.ClientNetworkHandler;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import ml.mypals.carpetgui.CarpetGUIClient;
import ml.mypals.carpetgui.network.RuleData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {ClientNetworkHandler.class},
   remap = false
)
public class CarpetClientNetworkMixin {
   @Shadow
   private static Map dataHandlers;

   @Inject(
      method = {"<clinit>"},
      at = {@At("TAIL")}
   )
   private static void hookRulesHandler(CallbackInfo ci) {
      BiConsumer original = (BiConsumer) dataHandlers.get("Rules");
      if (original != null) {
         dataHandlers.put("Rules", (BiConsumer) (player, tag) -> {
            if (tag instanceof CompoundTag ruleset) {
               //? if <1.21.6 {
               /*for (String ruleKey : ruleset.getAllKeys()) {
               *///?} else {
               for (String ruleKey : ruleset.keySet()) {
               //?}
                  Tag rTag = ruleset.get(ruleKey);
                  if (rTag instanceof CompoundTag ruleNBT) {
                     if (ruleNBT.contains("Manager")) {
                        //? if <1.21.5 {
                        /*String ruleName = (String)ruleNBT.getString("Rule");
                        String managerName = (String)ruleNBT.getString("Manager");
                        String value = (String)ruleNBT.getString("Value");
                        *///?} else {
                        String ruleName = (String)ruleNBT.getString("Rule").get();
                        String managerName = (String)ruleNBT.getString("Manager").get();
                        String value = (String)ruleNBT.getString("Value").get();
                        //?}
                        RuleData ruleData = new RuleData();
                        ruleData.manager = managerName;
                        ruleData.value = value;
                        ruleData.name = ruleName;
                        Optional<RuleData> existing = CarpetGUIClient.incompleteRulesFromServer.stream().filter((r) -> r.name.equals(ruleData.name)).findFirst();
                        if (existing.isPresent()) {
                           ((RuleData)existing.get()).value = ruleData.value;
                        } else {
                           CarpetGUIClient.incompleteRulesFromServer.add(ruleData);
                        }
                     }
                  }
               }
            }
            original.accept(player, tag);
         });
      }
   }
}

