package ml.mypals.carpetgui.ui.core;

import ml.mypals.carpetgui.ui.util.NinePatchTexture;
import net.minecraft.client.gui.screens.inventory.tooltip.TooltipRenderUtil;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public interface Surface {
   Surface BLANK = (context, component) -> {
   };
   Surface PANEL = (context, component) -> context.drawPanel(component.x(), component.y(), component.width(), component.height(), false);
   Surface DARK_PANEL = (context, component) -> context.drawPanel(component.x(), component.y(), component.width(), component.height(), true);
   Surface PANEL_INSET = (context, component) -> NinePatchTexture.draw((Identifier)OwoUIGraphics.PANEL_INSET_NINE_PATCH_TEXTURE, (OwoUIGraphics)context, (PositionedRectangle)component);
   Surface VANILLA_TRANSLUCENT = (context, component) -> context.drawGradientRect(component.x(), component.y(), component.width(), component.height(), -1072689136, -1072689136, -804253680, -804253680);
   Surface TOOLTIP = tooltip((Identifier)null);

   static Surface tooltip(@Nullable Identifier texture) {
      return (context, component) -> TooltipRenderUtil.extractTooltipBackground(context, component.x() + 4, component.y() + 4, component.width() - 8, component.height() - 8, texture);
   }

   static Surface blur(float quality, float size) {
      return flat(1610612736);
   }

   static Surface optionsBackground() {
      return blur(5.0F, 10.0F);
   }

   static Surface flat(int color) {
      return (context, component) -> context.fill(component.x(), component.y(), component.x() + component.width(), component.y() + component.height(), color);
   }

   static Surface outline(int color) {
      return (context, component) -> context.drawRectOutline(component.x(), component.y(), component.width(), component.height(), color);
   }

   static Surface tiled(Identifier texture, int textureWidth, int textureHeight) {
      return (context, component) -> context.blit(RenderPipelines.GUI_TEXTURED, texture, component.x(), component.y(), 0.0F, 0.0F, component.width(), component.height(), textureWidth, textureHeight);
   }

   static Surface panelWithInset(int insetWidth) {
      return PANEL.and((context, component) -> NinePatchTexture.draw(OwoUIGraphics.PANEL_INSET_NINE_PATCH_TEXTURE, context, component.x() + insetWidth, component.y() + insetWidth, component.width() - insetWidth * 2, component.height() - insetWidth * 2));
   }

   void draw(OwoUIGraphics var1, ParentUIComponent var2);

   default Surface and(Surface surface) {
      return (context, component) -> {
         this.draw(context, component);
         surface.draw(context, component);
      };
   }
}
