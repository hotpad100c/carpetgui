package ml.mypals.carpetgui.mixin.accessors;

import ml.mypals.carpetgui.accessors.CommandSourceStackAccessor;
import net.minecraft.commands.CommandSourceStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin({CommandSourceStack.class})
public class CommandSourceStackMixin implements CommandSourceStackAccessor {
   @Shadow
   @Final
   @Mutable
   private boolean silent;

   public void carpetGUI$setSilent(boolean silent) {
      this.silent = silent;
   }

   public boolean carpetGUI$getSilent() {
      return this.silent;
   }
}
