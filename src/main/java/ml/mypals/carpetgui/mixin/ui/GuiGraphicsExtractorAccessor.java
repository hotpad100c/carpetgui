package ml.mypals.carpetgui.mixin.ui;

import java.util.List;
import net.minecraft.client.gui.Font;
//? if <26.1 {
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?}
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
//? if >=1.20 {
/*import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
*///?}
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
//? if >=1.21.6 {
/*import org.joml.Matrix3x2fStack;
*///?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

//? if <1.20 {
@Mixin({net.minecraft.client.Minecraft.class})
//?} elif <26.1 {
/*@Mixin({GuiGraphics.class})
*///?} else {
/*@Mixin({GuiGraphicsExtractor.class})
*///?}
public interface GuiGraphicsExtractorAccessor {
   //? if >= 26.3 {

   //?} else if >=26.1 {
   /*@Invoker("tooltip")
   void carpetGUI$tooltip(Font var1, List<ClientTooltipComponent> var2, int var3, int var4, ClientTooltipPositioner var5, @Nullable ResourceLocation var6);
   *///?} elif >=1.21.6 {
   /*@Invoker("renderTooltip")
   void carpetGUI$tooltip(Font var1, List<ClientTooltipComponent> var2, int var3, int var4, ClientTooltipPositioner var5, @Nullable ResourceLocation var6);
   *///?} elif >=1.21.4 {
   /*@Invoker("renderTooltipInternal")
   void carpetGUI$tooltip(Font var1, List<ClientTooltipComponent> var2, int var3, int var4, ClientTooltipPositioner var5, @Nullable ResourceLocation var6);
   *///?} elif >=1.20 {
   /*@Invoker("renderTooltipInternal")
   void carpetGUI$tooltip(Font var1, List<ClientTooltipComponent> var2, int var3, int var4, ClientTooltipPositioner var5);
   *///?}

   //? if >=1.20 && <1.21.6 {
   /*@Accessor("pose")
   com.mojang.blaze3d.vertex.PoseStack carpetGUI$getPose();

   @Mutable
   @Accessor("pose")
   void carpetGUI$setPose(com.mojang.blaze3d.vertex.PoseStack var1);
   *///?} elif >=1.20 {
   /*@Accessor("pose")
   Matrix3x2fStack carpetGUI$getPose();

   @Mutable
   @Accessor("pose")
   void carpetGUI$setPose(Matrix3x2fStack var1);
   *///?}

   //? if >=1.21.6 {
   /*//? if <26.1 {
   @Accessor("scissorStack")
   GuiGraphics.ScissorStack carpetGUI$getScissorStack();

   @Mutable
   @Accessor("scissorStack")
   void carpetGUI$setScissorStack(GuiGraphics.ScissorStack var1);
   //?} else {
   /^@Accessor("scissorStack")
   GuiGraphicsExtractor.ScissorStack carpetGUI$getScissorStack();

   @Mutable
   @Accessor("scissorStack")
   void carpetGUI$setScissorStack(GuiGraphicsExtractor.ScissorStack var1);
   ^///?}

   @Accessor("deferredTooltip")
   void carpetGUI$setDeferredTooltip(Runnable var1);

   @Accessor("deferredTooltip")
   Runnable carpetGUI$getDeferredTooltip();
   *///?}

   //? if >=1.21.11 {
   /*@Accessor("mouseX")
   int carpetGUI$getMouseX();

   @Accessor("mouseY")
   int carpetGUI$getMouseY();
   *///?}
}
