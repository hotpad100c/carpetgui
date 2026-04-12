package ml.mypals.carpetgui.mixin;

import carpet.network.ClientNetworkHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import ml.mypals.carpetgui.network.RuleData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

import static ml.mypals.carpetgui.CarpetGUIClient.incompleteRulesFromServer;

@Mixin(value = ClientNetworkHandler.class, remap = false)
public class CarpetClientNetworkMixin {
    @WrapOperation(method = "lambda$static$1",
            at = @At(target = "Lnet/minecraft/nbt/CompoundTag;get(Ljava/lang/String;)Lnet/minecraft/nbt/Tag;"
                    , value = "INVOKE"))
    private static Tag onRuleSet(CompoundTag instance, String string, Operation<Tag> original) {

        CompoundTag ruleNBT = (CompoundTag) original.call(instance, string);
        if (ruleNBT.contains("Manager")) {
            //? if >=1.21.5 {
            String ruleName = ruleNBT.getString("Rule").get();
            String managerName = ruleNBT.getString("Manager").get();
            String value = ruleNBT.getString("Value").get();
            //?} else {
            /*String ruleName = ruleNBT.getString("Rule");
            String managerName = ruleNBT.getString("Manager");
            String value = ruleNBT.getString("Value");
            *///?}

            RuleData ruleData = new RuleData();
            ruleData.manager = managerName;
            ruleData.value = value;
            ruleData.name = ruleName;

            Optional<RuleData> existing = incompleteRulesFromServer.stream()
                    .filter(r -> r.name.equals(ruleData.name))
                    .findFirst();

            if (existing.isPresent()) {
                existing.get().value = ruleData.value;
            } else {
                incompleteRulesFromServer.add(ruleData);
            }
        }
        return ruleNBT;
    }
}
