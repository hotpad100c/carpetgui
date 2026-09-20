package ml.mypals.carpetgui.mixin;

import carpet.network.ClientNetworkHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import java.util.Optional;
import ml.mypals.carpetgui.CarpetGUIClient;
import ml.mypals.carpetgui.network.RuleData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(
   value = {ClientNetworkHandler.class},
   remap = false
)
public class CarpetClientNetworkMixin {
   @WrapOperation(
      method = {"lambda$static$1"},
      at = {@At(
   target = "Lnet/minecraft/nbt/CompoundTag;get(Ljava/lang/String;)Lnet/minecraft/nbt/Tag;",
   value = "INVOKE"
)}
   )
   private static Tag onRuleSet(CompoundTag instance, String string, Operation<Tag> original) {
      CompoundTag ruleNBT = (CompoundTag)original.call(instance, string);
      // CompoundTag ruleNBT = (CompoundTag)original.call(new Object[]{instance, string});
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

      return ruleNBT;
   }
}
