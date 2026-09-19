package ml.mypals.carpetgui.ui.component;

import java.util.function.Consumer;
import ml.mypals.carpetgui.mixin.ui.AbstractWidgetAccessor;
import ml.mypals.carpetgui.ui.UI;
import ml.mypals.carpetgui.ui.core.CursorStyle;
import ml.mypals.carpetgui.ui.core.OwoUIGraphics;
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.util.NinePatchTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.WidgetTooltipHolder;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class ButtonComponent extends Button {
   public static final Identifier ACTIVE_TEXTURE = UI.id("button/active");
   public static final Identifier HOVERED_TEXTURE = UI.id("button/hovered");
   public static final Identifier DISABLED_TEXTURE = UI.id("button/disabled");
   protected boolean textShadow;

   protected ButtonComponent(Component message, Consumer<ButtonComponent> onPress) {
      super(0, 0, 0, 0, message, (button) -> onPress.accept((ButtonComponent)button), Button.DEFAULT_NARRATION);
      this.textShadow = true;
      this.sizing(Sizing.content());
   }

   protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
      Identifier texture = this.active ? (this.isHovered ? HOVERED_TEXTURE : ACTIVE_TEXTURE) : DISABLED_TEXTURE;
      NinePatchTexture.draw(texture, (OwoUIGraphics)graphics, this.getX(), this.getY(), this.width, this.height);

      Font textRenderer = Minecraft.getInstance().font;
      int color = this.active ? -1 : -6250336;
      if (this.textShadow) {
         graphics.centeredText(textRenderer, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, color);
      } else {
         graphics.text(textRenderer, this.getMessage(), (int)((float)this.getX() + (float)this.width / 2.0F - (float)textRenderer.width(this.getMessage()) / 2.0F), (int)((float)this.getY() + (float)(this.height - 8) / 2.0F), color, false);
      }

      WidgetTooltipHolder tooltip = ((AbstractWidgetAccessor)this).carpetGUI$getTooltip();
      if (this.isHovered && tooltip.get() != null) {
         graphics.setTooltipForNextFrame(textRenderer, tooltip.get().toCharSequence(Minecraft.getInstance()), DefaultTooltipPositioner.INSTANCE, mouseX, mouseY, false);
      }
   }

   public ButtonComponent textShadow(boolean textShadow) {
      this.textShadow = textShadow;
      return this;
   }

   public boolean textShadow() {
      return this.textShadow;
   }

   public ButtonComponent active(boolean active) {
      this.active = active;
      return this;
   }

   public boolean active() {
      return this.active;
   }

   protected CursorStyle carpetGUI$preferredCursorStyle() {
      return CursorStyle.HAND;
   }
}
