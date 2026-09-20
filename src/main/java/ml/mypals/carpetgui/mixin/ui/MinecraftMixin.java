package ml.mypals.carpetgui.mixin.ui;

import com.mojang.blaze3d.platform.Window;
import ml.mypals.carpetgui.ui.event.WindowResizeCallback;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({Minecraft.class})
public class MinecraftMixin {
   @Shadow
   @Final
   private Window window;

   //? if <26.1 {
   @Inject(
      method = {"resizeDisplay"},
      at = {@At("TAIL")}
   )
   //?} else {
   /*@Inject(
      method = {"resizeGui"},
      at = {@At("TAIL")}
   )
   *///?}
   private void captureResize(CallbackInfo ci) {
      ((WindowResizeCallback)WindowResizeCallback.EVENT.invoker()).onResized((Minecraft)(Object)this, this.window);
   }
}
