package ml.mypals.carpetgui.mixin.ui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import ml.mypals.carpetgui.ui.base.BaseOwoScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({Screen.class})
public class ScreenMixin {
   @ModifyExpressionValue(
      method = {"keyPressed"},
      at = {@At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/gui/screens/Screen;shouldCloseOnEsc()Z",
   ordinal = 0
)}
   )
   private boolean dontCloseOwoScreens(boolean original) {
      return (Object) this instanceof BaseOwoScreen ? false : original;
   }
}
