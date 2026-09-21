package ml.mypals.carpetgui.mixin.ui;

import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;

//? if <1.17 {
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenAccessor {
    @Invoker("renderComponentHoverEffect")
    void carpetGUI$renderComponentHoverEffect(PoseStack pose, Style style, int x, int y);
}
//?} elif <1.19.3 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import ml.mypals.carpetgui.compat.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenAccessor {
    @Invoker("renderComponentHoverEffect")
    void carpetGUI$renderComponentHoverEffect(PoseStack pose, Style style, int x, int y);

    @Invoker("renderTooltipInternal")
    void carpetGUI$renderTooltipInternal(PoseStack pose, List<ClientTooltipComponent> components, int x, int y);
}
*///?} else if <1.20 {
/*import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import ml.mypals.carpetgui.compat.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Screen.class)
public interface ScreenAccessor {
    @Invoker("renderComponentHoverEffect")
    void carpetGUI$renderComponentHoverEffect(PoseStack pose, Style style, int x, int y);

    @Invoker("renderTooltipInternal")
    void carpetGUI$renderTooltipInternal(PoseStack pose, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner);
}
*///?} else {
/*@Mixin(Screen.class)
public interface ScreenAccessor {
}
*///?}
