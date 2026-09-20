package ml.mypals.carpetgui.mixin.ui;

import net.minecraft.client.gui.components.EditBox;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({EditBox.class})
public interface EditBoxAccessor {
   @Accessor("bordered")
   boolean carpetGUI$bordered();

   //? if >=1.21.6 {
   /*@Invoker("updateTextPosition")
   void carpetGUI$updateTextPosition();
   *///?}
}
