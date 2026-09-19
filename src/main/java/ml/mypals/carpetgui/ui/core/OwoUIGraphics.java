package ml.mypals.carpetgui.ui.core;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import ml.mypals.carpetgui.mixin.ui.GuiGraphicsExtractorAccessor;
import ml.mypals.carpetgui.ui.event.WindowResizeCallback;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

public class OwoUIGraphics extends GuiGraphicsExtractor {
   private final Consumer<Runnable> setTooltipDrawer;

   protected OwoUIGraphics(Minecraft client, GuiRenderState renderState, int mouseX, int mouseY, Consumer<Runnable> setTooltipDrawer) {
      super(client, renderState, mouseX, mouseY);
      this.setTooltipDrawer = setTooltipDrawer;
   }

   public static OwoUIGraphics of(GuiGraphicsExtractor graphics) {
      Minecraft var10002 = Minecraft.getInstance();
      GuiRenderState var10003 = graphics.guiRenderState;
      int var10004 = ((GuiGraphicsExtractorAccessor)graphics).carpetGUI$getMouseY();
      int var10005 = ((GuiGraphicsExtractorAccessor)graphics).carpetGUI$getMouseX();
      GuiGraphicsExtractorAccessor var10006 = (GuiGraphicsExtractorAccessor)graphics;
      Objects.requireNonNull((GuiGraphicsExtractorAccessor)graphics);
      OwoUIGraphics owoContext = new OwoUIGraphics(var10002, var10003, var10004, var10005, var10006::carpetGUI$setDeferredTooltip);
      ((GuiGraphicsExtractorAccessor)owoContext).carpetGUI$setScissorStack(((GuiGraphicsExtractorAccessor)graphics).carpetGUI$getScissorStack());
      ((GuiGraphicsExtractorAccessor)owoContext).carpetGUI$setPose(((GuiGraphicsExtractorAccessor)graphics).carpetGUI$getPose());
      return owoContext;
   }

   public static UtilityScreen utilityScreen() {
      return OwoUIGraphics.UtilityScreen.get();
   }

   public Matrix3x2fStack getMatrixStack() {
      return this.pose();
   }

   public OwoUIGraphics push() {
      this.pose().pushMatrix();
      return this;
   }

   public OwoUIGraphics pop() {
      this.pose().popMatrix();
      return this;
   }

   public OwoUIGraphics translate(double x, double y) {
      this.pose().translate((float)x, (float)y);
      return this;
   }

   public OwoUIGraphics translate(float x, float y) {
      this.pose().translate(x, y);
      return this;
   }

   public OwoUIGraphics scale(float x, float y) {
      this.pose().scale(x, y);
      return this;
   }

   public OwoUIGraphics mul(Matrix3x2f matrix) {
      this.pose().mul(matrix);
      return this;
   }

   public boolean intersectsScissor(PositionedRectangle other) {
      other = other.transform(this.getMatrixStack());
      ScreenRectangle rect = this.scissorStack.peek();
      if (rect == null) {
         return true;
      } else {
         ScreenPosition pos = rect.position();
         return other.carpetGUI$x() < pos.x() + rect.width() && other.carpetGUI$x() + other.carpetGUI$width() >= pos.x() && other.carpetGUI$y() < pos.y() + rect.height() && other.carpetGUI$y() + other.carpetGUI$height() >= pos.y();
      }
   }

   public void drawRectOutline(int x, int y, int width, int height, int color) {
      this.drawRectOutline(RenderPipelines.GUI, x, y, width, height, color);
   }

   public void drawRectOutline(RenderPipeline pipeline, int x, int y, int width, int height, int color) {
      this.fill(pipeline, x, y, x + width, y + 1, color);
      this.fill(pipeline, x, y + height - 1, x + width, y + height, color);
      this.fill(pipeline, x, y + 1, x + 1, y + height - 1, color);
      this.fill(pipeline, x + width - 1, y + 1, x + width, y + height - 1, color);
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
      ((GuiGraphicsExtractorAccessor)this).carpetGUI$tooltip(textRenderer, components, x, y, DefaultTooltipPositioner.INSTANCE, texture);
   }

   protected void setTooltipForNextFrameInternal(Font textRenderer, List<ClientTooltipComponent> components, int x, int y, ClientTooltipPositioner positioner, @Nullable Identifier texture, boolean replaceExisting) {
      super.setTooltipForNextFrameInternal(textRenderer, components, x, y, positioner, texture, replaceExisting);
      this.setTooltipDrawer.accept(((GuiGraphicsExtractorAccessor)this).carpetGUI$getDeferredTooltip());
   }

   public static class UtilityScreen extends Screen {
      private static UtilityScreen INSTANCE;

      private UtilityScreen() {
         super(Component.empty());
      }

      public static UtilityScreen get() {
         if (INSTANCE == null) {
            INSTANCE = new UtilityScreen();
            Minecraft client = Minecraft.getInstance();
            INSTANCE.init(client.getWindow().getGuiScaledWidth(), client.getWindow().getGuiScaledHeight());
         }

         return INSTANCE;
      }

      public boolean handleTextClick(Style style, Screen screenAfterRun) {
         if (style.getClickEvent() == null) {
            return false;
         } else {
            defaultHandleGameClickEvent(style.getClickEvent(), this.minecraft, screenAfterRun);
            return true;
         }
      }

      static {
         WindowResizeCallback.EVENT.register((WindowResizeCallback)(client, window) -> {
            if (INSTANCE != null) {
               INSTANCE.init(window.getGuiScaledWidth(), window.getGuiScaledHeight());
            }
         });
      }
   }
}
