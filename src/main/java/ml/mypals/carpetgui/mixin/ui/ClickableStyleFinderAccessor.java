package ml.mypals.carpetgui.mixin.ui;

import java.util.function.Consumer;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ActiveTextCollector.ClickableStyleFinder.class})
public interface ClickableStyleFinderAccessor {
   @Mutable
   @Accessor("styleScanner")
   void carpetGUI$setStyleScanner(Consumer<Style> var1);

   @Accessor("result")
   void carpetGUI$setResult(Style var1);
}
