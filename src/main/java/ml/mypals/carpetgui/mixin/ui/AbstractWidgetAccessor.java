package ml.mypals.carpetgui.mixin.ui;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({AbstractWidget.class})
public interface AbstractWidgetAccessor {
   @Accessor("height")
   void carpetGUI$setHeight(int var1);

   @Accessor("width")
   void carpetGUI$setWidth(int var1);

   @Accessor("x")
   void carpetGUI$setX(int var1);

   @Accessor("y")
   void carpetGUI$setY(int var1);

   @Accessor("tooltip")
   WidgetTooltipHolder carpetGUI$getTooltip();
}
