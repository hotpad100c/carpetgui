package ml.mypals.carpetgui.ui.component;

import java.util.function.Consumer;
import ml.mypals.carpetgui.ui.UI;
import ml.mypals.carpetgui.ui.core.CursorStyle;
import ml.mypals.carpetgui.ui.core.OwoUIGraphics;
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.util.NinePatchTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
//? if <26.1 {
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?}
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ButtonComponent extends Button {
   public static final ResourceLocation ACTIVE_TEXTURE = UI.id("button/active");
   public static final ResourceLocation HOVERED_TEXTURE = UI.id("button/hovered");
   public static final ResourceLocation DISABLED_TEXTURE = UI.id("button/disabled");
   protected boolean textShadow;

   //? if <1.19.3 {
   public int getX() { return this.x; }
   public int getY() { return this.y; }
   //?}

   protected ButtonComponent(Component message, Consumer<ButtonComponent> onPress) {
      //? if <1.19 {
      super(0, 0, 0, 0, message, (button) -> onPress.accept((ButtonComponent)button));
      //?} else {
      /*super(0, 0, 0, 0, message, (button) -> onPress.accept((ButtonComponent)button), Button.DEFAULT_NARRATION);
      *///?}
      this.textShadow = true;
      this.sizing(Sizing.content());
   }

   //? if <1.19.3 {
   @Override
   public void renderButton(com.mojang.blaze3d.vertex.PoseStack poseStack, int mouseX, int mouseY, float a) {
      this.renderContents(OwoUIGraphics.of(new GuiGraphics(Minecraft.getInstance(), poseStack, Minecraft.getInstance().renderBuffers().bufferSource())), mouseX, mouseY, a);
   }
   //?} elif <1.20 {
   /*@Override
   public void renderWidget(com.mojang.blaze3d.vertex.PoseStack poseStack, int mouseX, int mouseY, float a) {
      this.renderContents(OwoUIGraphics.of(new GuiGraphics(Minecraft.getInstance(), poseStack, Minecraft.getInstance().renderBuffers().bufferSource())), mouseX, mouseY, a);
   }
   *///?}

   //? if <26.1 {
   protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float a) {
   //?} else {
   /*protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
   *///?}
      ResourceLocation texture = this.active ? (this.isHovered ? HOVERED_TEXTURE : ACTIVE_TEXTURE) : DISABLED_TEXTURE;
      NinePatchTexture.draw(texture, (OwoUIGraphics)graphics, this.getX(), this.getY(), this.width, this.height);

      Font textRenderer = Minecraft.getInstance().font;
      int color = this.active ? -1 : -6250336;
      if (this.textShadow) {
         //? if <26.1 {
         graphics.drawCenteredString(textRenderer, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, color);
         //?} else {
         /*graphics.centeredText(textRenderer, this.getMessage(), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, color);
         *///?}
      } else {
         //? if <26.1 {
         graphics.drawString(textRenderer, this.getMessage(), (int)((float)this.getX() + (float)this.width / 2.0F - (float)textRenderer.width(this.getMessage()) / 2.0F), (int)((float)this.getY() + (float)(this.height - 8) / 2.0F), color, false);
         //?} else {
         /*graphics.text(textRenderer, this.getMessage(), (int)((float)this.getX() + (float)this.width / 2.0F - (float)textRenderer.width(this.getMessage()) / 2.0F), (int)((float)this.getY() + (float)(this.height - 8) / 2.0F), color, false);
         *///?}
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
