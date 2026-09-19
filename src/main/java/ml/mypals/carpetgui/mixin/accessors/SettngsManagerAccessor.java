package ml.mypals.carpetgui.mixin.accessors;

import carpet.api.settings.CarpetRule;
import carpet.api.settings.SettingsManager;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({SettingsManager.class})
public interface SettngsManagerAccessor {
   @Invoker("setRule")
   int carpetGUI$setRule(CommandSourceStack var1, CarpetRule<?> var2, String var3);

   @Invoker("setDefault")
   int carpetGUI$setDefault(CommandSourceStack var1, CarpetRule<?> var2, String var3);

   @Invoker("removeDefault")
   int carpetGUI$removeDefault(CommandSourceStack var1, CarpetRule<?> var2);
}
