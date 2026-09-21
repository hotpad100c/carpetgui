package ml.mypals.carpetgui.ui.core;

//? if >=1.21.6 {
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import net.minecraft.client.renderer.RenderPipelines;
//?}
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import ml.mypals.carpetgui.mixin.ui.GuiGraphicsExtractorAccessor;
import ml.mypals.carpetgui.ui.event.WindowResizeCallback;
//? if >=1.21.6 {
import org.joml.Matrix3x2fStack;
//?}
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
//? if <26.1 {
/*import net.minecraft.client.gui.GuiGraphics;
*///?} else {
import net.minecraft.client.gui.GuiGraphicsExtractor;
//?}
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
//? if >=1.20 {
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
//?}
//? if >=26.1 {
import net.minecraft.client.renderer.state.gui.GuiRenderState;
//?} elif >=1.21.6 {
/*import net.minecraft.client.gui.render.state.GuiRenderState;
*///?}
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

//? if <26.1 {
/*public class OwoUIGraphics extends GuiGraphics {
*///?} else {
public class OwoUIGraphics extends GuiGraphicsExtractor {
//?}
   private final Consumer<Runnable> setTooltipDrawer;

   //? if <1.21.6 {
   /*protected OwoUIGraphics(Minecraft client, net.minecraft.client.renderer.MultiBufferSource.BufferSource bufferSource) {
      super(client, bufferSource);
      this.setTooltipDrawer = (r) -> {};
   }

   //? if <1.20 {
   /^protected OwoUIGraphics(Minecraft client, com.mojang.blaze3d.vertex.PoseStack pose, net.minecraft.client.renderer.MultiBufferSource.BufferSource bufferSource) {
      super(client, pose, bufferSource);
      this.setTooltipDrawer = (r) -> {};
   }
   ^///?}

   public static OwoUIGraphics of(GuiGraphics graphics) {
      if (graphics instanceof OwoUIGraphics owo) return owo;
      Minecraft client = Minecraft.getInstance();
      //? if <1.20 {
      /^return new OwoUIGraphics(client, graphics.pose(), client.renderBuffers().bufferSource());
      ^///?} else {
      OwoUIGraphics owoContext = new OwoUIGraphics(client, client.renderBuffers().bufferSource());
      ((GuiGraphicsExtractorAccessor)owoContext).carpetGUI$setPose(((GuiGraphicsExtractorAccessor)graphics).carpetGUI$getPose());
      return owoContext;
      //?}
   }
   *///?} else {
   protected OwoUIGraphics(Minecraft client, GuiRenderState renderState, int mouseX, int mouseY, Consumer<Runnable> setTooltipDrawer) {
      //? if <1.21.11 {
      /*super(client, renderState);
      *///?} else {
      super(client, renderState, mouseX, mouseY);
      //?}
      this.setTooltipDrawer = setTooltipDrawer;
   }

   //? if <26.1 {
   /*public static OwoUIGraphics of(GuiGraphics graphics) {
   *///?} else {
   public static OwoUIGraphics of(GuiGraphicsExtractor graphics) {
   //?}
      Minecraft var10002 = Minecraft.getInstance();
      GuiRenderState var10003 = graphics.guiRenderState;
      //? if <1.21.11 {
      /*int var10004 = 0;
      int var10005 = 0;
      *///?} else {
      int var10004 = ((GuiGraphicsExtractorAccessor)graphics).carpetGUI$getMouseY();
      int var10005 = ((GuiGraphicsExtractorAccessor)graphics).carpetGUI$getMouseX();
      //?}
      GuiGraphicsExtractorAccessor var10006 = (GuiGraphicsExtractorAccessor)graphics;
      Objects.requireNonNull((GuiGraphicsExtractorAccessor)graphics);
      OwoUIGraphics owoContext = new OwoUIGraphics(var10002, var10003, var10004, var10005, var10006::carpetGUI$setDeferredTooltip);
      ((GuiGraphicsExtractorAccessor)owoContext).carpetGUI$setScissorStack(((GuiGraphicsExtractorAccessor)graphics).carpetGUI$getScissorStack());
      ((GuiGraphicsExtractorAccessor)owoContext).carpetGUI$setPose(((GuiGraphicsExtractorAccessor)graphics).carpetGUI$getPose());
      return owoContext;
   }
   //?}

   public static UtilityScreen utilityScreen() {
      return OwoUIGraphics.UtilityScreen.get();
   }

   //? if >=1.21.6 {
   public Matrix3x2fStack getMatrixStack() {
      return this.pose();
   }
   //?}

   public OwoUIGraphics push() {
      //? if <1.21.6 {
      /*this.pose().pushPose();
      *///?} else {
      this.pose().pushMatrix();
      //?}
      return this;
   }

   public OwoUIGraphics pop() {
      //? if <1.21.6 {
      /*this.pose().popPose();
      *///?} else {
      this.pose().popMatrix();
      //?}
      return this;
   }

   public OwoUIGraphics translate(double x, double y) {
      return this.translate((float)x, (float)y);
   }

   public OwoUIGraphics translate(float x, float y) {
      //? if <1.21.6 {
      /*this.pose().translate(x, y, 0.0F);
      *///?} else {
      this.pose().translate(x, y);
      //?}
      return this;
   }

   public OwoUIGraphics scale(float x, float y) {
      //? if <1.21.6 {
      /*this.pose().scale(x, y, 1.0F);
      *///?} else {
      this.pose().scale(x, y);
      //?}
      return this;
   }

   public boolean intersectsScissor(PositionedRectangle other) {
      //? if <1.21.6 {
      /*return true;
      *///?} else {
      other = other.carpetGUI$transform(this.getMatrixStack());
      ScreenRectangle rect = this.scissorStack.peek();
      if (rect == null) {
         return true;
      } else {
         ScreenPosition pos = rect.position();
         return other.carpetGUI$x() < pos.x() + rect.width() && other.carpetGUI$x() + other.carpetGUI$width() >= pos.x() && other.carpetGUI$y() < pos.y() + rect.height() && other.carpetGUI$y() + other.carpetGUI$height() >= pos.y();
      }
      //?}
   }

   //? if <1.21.6 {
   /*public void drawRectOutline(int x, int y, int width, int height, int color) {
      this.renderOutline(x, y, width, height, color);
   }
   *///?} else {
   public void drawRectOutline(int x, int y, int width, int height, int color) {
      this.drawRectOutline(RenderPipelines.GUI, x, y, width, height, color);
   }

   public void drawRectOutline(RenderPipeline pipeline, int x, int y, int width, int height, int color) {
      this.fill(pipeline, x, y, x + width, y + 1, color);
      this.fill(pipeline, x, y + height - 1, x + width, y + height, color);
      this.fill(pipeline, x, y + 1, x + 1, y + height - 1, color);
      this.fill(pipeline, x + width - 1, y + 1, x + width, y + height - 1, color);
   }
   //?}

   public void blitTexture(Identifier texture, int x, int y, float u, float v, int width, int height, int textureWidth, int textureHeight) {
      this.blitTexture(texture, x, y, u, v, width, height, width, height, textureWidth, textureHeight, -1);
   }

   public void blitTexture(Identifier texture, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight) {
      this.blitTexture(texture, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, -1);
   }

   public void blitTexture(Identifier texture, int x, int y, float u, float v, int width, int height, int uWidth, int vHeight, int textureWidth, int textureHeight, int color) {
      //? if >=1.21.6 {
      this.blit(RenderPipelines.GUI_TEXTURED, texture, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, color);
      //?} elif >=1.21.4 {
      /*this.blit(net.minecraft.client.renderer.RenderType::guiTextured, texture, x, y, u, v, width, height, uWidth, vHeight, textureWidth, textureHeight, color);
      *///?} else {
      /*this.blit(texture, x, y, width, height, u, v, uWidth, vHeight, textureWidth, textureHeight);
      *///?}
   }

   public void drawGradientRect(int x, int y, int width, int height, int topLeftColor, int topRightColor, int bottomRightColor, int bottomLeftColor) {
      this.fillGradient(x, y, x + width, y + height, averageArgb(topLeftColor, topRightColor), averageArgb(bottomLeftColor, bottomRightColor));
   }

   private static int averageArgb(int first, int second) {
      if (first == second) {
         return first;
      } else {
         int result = 0;

         for(int shift = 0; shift < 32; shift += 8) {
            int channel = ((first >>> shift & 255) + (second >>> shift & 255)) / 2;
            result |= channel << shift;
         }

         return result;
      }
   }

   public void drawTooltip(Font textRenderer, int x, int y, List<ClientTooltipComponent> components) {
      this.drawTooltip(textRenderer, x, y, components, (Identifier)null);
   }

   public void drawTooltip(Font textRenderer, int x, int y, List<ClientTooltipComponent> components, @Nullable Identifier texture) {
      //? if >=26.3 {
      this.tooltip(textRenderer, components, x, y, DefaultTooltipPositioner.INSTANCE, texture,false);
      //?} else if >=1.21.4 {
      /*((GuiGraphicsExtractorAccessor)this).carpetGUI$tooltip(textRenderer, components, x, y, DefaultTooltipPositioner.INSTANCE, texture);
       *///?} else if >=1.20 {
      /*((GuiGraphicsExtractorAccessor)this).carpetGUI$tooltip(textRenderer, components, x, y, DefaultTooltipPositioner.INSTANCE);
      *///?} else {
      /*this.renderTooltip(textRenderer, components, x, y);
      *///?}
   }

   //? if >=26.1 {
   protected void setTooltipForNextFrameInternal(Font textRenderer, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner, @Nullable Identifier texture, boolean replaceExisting) {
      super.setTooltipForNextFrameInternal(textRenderer, components, x, y, positioner, texture, replaceExisting);
      this.setTooltipDrawer.accept(((GuiGraphicsExtractorAccessor)this).carpetGUI$getDeferredTooltip());
   }
   //?}

   public static class UtilityScreen extends Screen {
      private static UtilityScreen INSTANCE;

      private UtilityScreen() {
         super(Component.empty());
      }

      public static UtilityScreen get() {
         if (INSTANCE == null) {
            INSTANCE = new UtilityScreen();
            Minecraft client = Minecraft.getInstance();
            //? if <1.21.11 {
            /*INSTANCE.init(client, client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
            *///?} else {
            INSTANCE.init(client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
            //?}
         }

         return INSTANCE;
      }

      public boolean handleTextClick(Style style, Screen screenAfterRun) {
         if (style.getClickEvent() == null) {
            return false;
         } else {
            //? if <1.21.6 {
            /*this.handleComponentClicked(style);
            *///?} else {
            defaultHandleGameClickEvent(style.getClickEvent(), this.minecraft, screenAfterRun);
            //?}
            return true;
         }
      }

      static {
         WindowResizeCallback.EVENT.register((WindowResizeCallback)(client, window) -> {
            if (INSTANCE != null) {
               //? if <1.21.11 {
               /*INSTANCE.init(client, window.getGuiScaledWidth(), window.getGuiScaledHeight());
               *///?} else {
               INSTANCE.init(window.getGuiScaledWidth(), window.getGuiScaledHeight());
               //?}
            }
         });
      }
   }
}
