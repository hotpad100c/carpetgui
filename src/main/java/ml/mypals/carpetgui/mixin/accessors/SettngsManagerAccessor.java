package ml.mypals.carpetgui.mixin.accessors;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.SettingsManager;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SettingsManager.class)
public interface SettngsManagerAccessor {
    @Invoker("setRule")
    int carpetGUI$setRule(CommandSourceStack source, CarpetRule<?> rule, String newValue);
    @Invoker("setDefault")
    int carpetGUI$setDefault(CommandSourceStack source, CarpetRule<?> rule, String stringValue);
    @Invoker("removeDefault")
    int carpetGUI$removeDefault(CommandSourceStack source, CarpetRule<?> rule);
}
