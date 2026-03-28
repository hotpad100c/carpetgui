package ml.mypals.carpetgui.mixin;

import carpet.network.ClientNetworkHandler;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import ml.mypals.carpetgui.network.RuleData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import static ml.mypals.carpetgui.CarpetGUIClient.rulesFromServer;

@Mixin(value = ClientNetworkHandler.class, remap = false)
public class CarpetClientNetworkMixin {
    @WrapOperation(method = "lambda$static$1",
            at = @At(target = "Lnet/minecraft/nbt/CompoundTag;get(Ljava/lang/String;)Lnet/minecraft/nbt/Tag;"
            , value = "INVOKE"))
    private static Tag onRuleSet(CompoundTag instance, String string, Operation<Tag> original) {

        CompoundTag ruleNBT = (CompoundTag)original.call(instance,string);
        if (ruleNBT.contains("Manager")) {
            String ruleName = ruleNBT.getString("Rule");
            String managerName = ruleNBT.getString("Manager");
            String value = ruleNBT.getString("Value");

            RuleData ruleData = new RuleData();
            ruleData.manager = managerName;
            ruleData.value = value;
            ruleData.name = ruleName;

            rulesFromServer.add(ruleData);
        }
        return ruleNBT;
    }
}
