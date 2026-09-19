package ml.mypals.carpetgui.mixin.ui;

import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix3x2fStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin({GuiGraphicsExtractor.class})
public interface GuiGraphicsExtractorAccessor {
   @Invoker("tooltip")
   void carpetGUI$tooltip(Font var1, List<ClientTooltipComponent> var2, int var3, int var4, ClientTooltipPositioner var5, @Nullable Identifier var6);

   @Accessor("pose")
   Matrix3x2fStack carpetGUI$getPose();

   @Mutable
   @Accessor("pose")
   void carpetGUI$setPose(Matrix3x2fStack var1);

   @Accessor("scissorStack")
   GuiGraphicsExtractor.ScissorStack carpetGUI$getScissorStack();

   @Mutable
   @Accessor("scissorStack")
   void carpetGUI$setScissorStack(GuiGraphicsExtractor.ScissorStack var1);

   @Accessor("deferredTooltip")
   void carpetGUI$setDeferredTooltip(Runnable var1);

   @Accessor("deferredTooltip")
   Runnable carpetGUI$getDeferredTooltip();

   @Accessor("mouseX")
   int carpetGUI$getMouseX();

   @Accessor("mouseY")
   int carpetGUI$getMouseY();
}
