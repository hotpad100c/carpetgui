package ml.mypals.carpetgui.mixin.accessors;

//? if <1.19 {
/*import carpet.settings.ParsedRule;
import carpet.settings.SettingsManager;
*///?} else {
import carpet.api.settings.CarpetRule;
import carpet.api.settings.SettingsManager;
//?}
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({SettingsManager.class})
public interface SettngsManagerAccessor {
   //? if <1.19 {
   /*@Invoker("setRule")
   int carpetGUI$setRule(CommandSourceStack var1, ParsedRule<?> var2, String var3);

   @Invoker("setDefault")
   int carpetGUI$setDefault(CommandSourceStack var1, ParsedRule<?> var2, String var3);

   @Invoker("removeDefault")
   int carpetGUI$removeDefault(CommandSourceStack var1, ParsedRule<?> var2);
   *///?} else {
   @Invoker("setRule")
   int carpetGUI$setRule(CommandSourceStack var1, CarpetRule<?> var2, String var3);

   @Invoker("setDefault")
   int carpetGUI$setDefault(CommandSourceStack var1, CarpetRule<?> var2, String var3);

   @Invoker("removeDefault")
   int carpetGUI$removeDefault(CommandSourceStack var1, CarpetRule<?> var2);
   //?}
}
