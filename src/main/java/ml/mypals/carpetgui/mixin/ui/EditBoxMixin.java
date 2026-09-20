package ml.mypals.carpetgui.mixin.ui;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Cancellable;
import ml.mypals.carpetgui.ui.component.TextBoxComponent;
import ml.mypals.carpetgui.ui.core.UIComponent;
import ml.mypals.carpetgui.ui.inject.GreedyInputUIComponent;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EditBox.class})
public abstract class EditBoxMixin extends AbstractWidget implements GreedyInputUIComponent {
   @Shadow
   private String value;

   public EditBoxMixin(int x, int y, int width, int height, Component message) {
      super(x, y, width, height, message);
   }

   @Inject(
      method = {"onValueChange"},
      at = {@At("HEAD")}
   )
   private void callOwoListener(String newText, CallbackInfo ci) {
      if ((Object) this instanceof TextBoxComponent textBox) {
         textBox.textValue().set(newText);
      }
   }

   public void carpetGUI$onFocusGained(UIComponent.FocusSource source) {
      super.carpetGUI$onFocusGained(source);
      this.setFocused(true);
   }

   @ModifyExpressionValue(
      method = {"insertText"},
      at = {@At(
   value = "INVOKE",
   target = "Ljava/lang/StringBuilder;toString()Ljava/lang/String;"
)}
   )
   private String injectFilter(String original, @Cancellable CallbackInfo ci) {
      if ((Object) this instanceof TextBoxComponent textBox) {
         if (!textBox.getFilter().test(original)) {
            ci.cancel();
            return this.value;
         }
      }

      return original;
   }

   @ModifyExpressionValue(
      //? if <1.20.2 {
      method = {"deleteChars"},
      //?} else {
      /*method = {"deleteCharsToPos"},
      *///?}
      at = {@At(
   value = "INVOKE",
   target = "Ljava/lang/StringBuilder;toString()Ljava/lang/String;"
)}
   )
   private String injectFilterButDoItAgain(String original, @Cancellable CallbackInfo ci) {
      if ((Object) this instanceof TextBoxComponent textBox) {
         if (!textBox.getFilter().test(original)) {
            ci.cancel();
            return this.value;
         }
      }

      return original;
   }
}
