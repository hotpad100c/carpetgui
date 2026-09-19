package ml.mypals.carpetgui.ui.core;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;
import ml.mypals.carpetgui.mixin.ui.GuiGraphicsExtractorAccessor;
import ml.mypals.carpetgui.ui.UI;
import ml.mypals.carpetgui.ui.event.WindowResizeCallback;
import ml.mypals.carpetgui.ui.util.NinePatchTexture;
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
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

public class OwoUIGraphics extends GuiGraphicsExtractor {
   public static final Identifier PANEL_NINE_PATCH_TEXTURE = UI.id("panel/default");
   public static final Identifier DARK_PANEL_NINE_PATCH_TEXTURE = UI.id("panel/dark");
   public static final Identifier PANEL_INSET_NINE_PATCH_TEXTURE = UI.id("panel/inset");
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
         return other.x() < pos.x() + rect.width() && other.x() + other.width() >= pos.x() && other.y() < pos.y() + rect.height() && other.y() + other.height() >= pos.y();
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

   public void drawPanel(int x, int y, int width, int height, boolean dark) {
      NinePatchTexture.draw(dark ? DARK_PANEL_NINE_PATCH_TEXTURE : PANEL_NINE_PATCH_TEXTURE, this, x, y, width, height);
   }

   public void drawText(Component text, float x, float y, float scale, int color) {
      this.drawText(text, x, y, scale, color, OwoUIGraphics.TextAnchor.TOP_LEFT);
   }

   public void drawText(Component text, float x, float y, float scale, int color, TextAnchor anchorPoint) {
      Font textRenderer = Minecraft.getInstance().font;
      this.pose().pushMatrix();
      this.pose().scale(scale, scale);
      switch (anchorPoint.ordinal()) {
         case 0:
            x -= (float)textRenderer.width(text) * scale;
            break;
         case 1:
            x -= (float)textRenderer.width(text) * scale;
            Objects.requireNonNull(textRenderer);
            y -= 9.0F * scale;
         case 2:
         default:
            break;
         case 3:
            Objects.requireNonNull(textRenderer);
            y -= 9.0F * scale;
      }

      this.text(textRenderer, text, (int)(x * (1.0F / scale)), (int)(y * (1.0F / scale)), color, false);
      this.pose().popMatrix();
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

   public static void drawInsets(OwoUIGraphics self, int x, int y, int width, int height, Insets insets, int color) {
      drawInsets(self, RenderPipelines.GUI, x, y, width, height, insets, color);
   }

   public static void drawInsets(OwoUIGraphics self, RenderPipeline pipeline, int x, int y, int width, int height, Insets insets, int color) {
      self.fill(pipeline, x - insets.left(), y - insets.top(), x + width + insets.right(), y, color);
      self.fill(pipeline, x - insets.left(), y + height, x + width + insets.right(), y + height + insets.bottom(), color);
      self.fill(pipeline, x - insets.left(), y, x, y + height, color);
      self.fill(pipeline, x + width, y, x + width + insets.right(), y + height, color);
   }

   public static void drawInspector(OwoUIGraphics self, ParentUIComponent root, double mouseX, double mouseY, boolean onlyHovered) {
      Minecraft client = Minecraft.getInstance();
      Font textRenderer = client.font;
      ArrayList<UIComponent> children = new ArrayList();
      if (!onlyHovered) {
         root.collectDescendants(children);
      } else if (root.childAt((int)mouseX, (int)mouseY) != null) {
         children.add(root.childAt((int)mouseX, (int)mouseY));
      }

      RenderPipeline pipeline = RenderPipelines.GUI;

      for(UIComponent child : children) {
         if (child instanceof ParentUIComponent parentComponent) {
            drawInsets(self, pipeline, parentComponent.x(), parentComponent.y(), parentComponent.width(), parentComponent.height(), ((Insets)parentComponent.padding().get()).inverted(), -1492325155);
         }

         Insets margins = (Insets)child.margins().get();
         drawInsets(self, pipeline, child.x(), child.y(), child.width(), child.height(), margins, -1476398280);
         self.drawRectOutline(pipeline, child.x(), child.y(), child.width(), child.height(), -12930817);
         if (onlyHovered) {
            int inspectorX = child.x() + 1;
            int inspectorY = child.y() + child.height() + ((Insets)child.margins().get()).bottom() + 1;
            MutableComponent message = Component.literal(child.getClass().getSimpleName()).append(child.id() == null ? "\n" : " '" + child.id() + "'\n").append(child.inspectorDescriptor());
            List<FormattedCharSequence> wrappedMessage = textRenderer.split(message, client.getWindow().getGuiScaledWidth() + 4);
            int inspectorWidth = wrappedMessage.stream().mapToInt(textRenderer::width).max().orElse(30);
            Objects.requireNonNull(textRenderer);
            int inspectorHeight = 9 * wrappedMessage.size() + 4;
            if (inspectorY > client.getWindow().getGuiScaledHeight() - inspectorHeight) {
               inspectorY -= child.fullSize().height() + inspectorHeight + 1;
               if (child instanceof ParentUIComponent) {
                  ParentUIComponent parentComponent = (ParentUIComponent)child;
                  inspectorX += ((Insets)parentComponent.padding().get()).left();
                  inspectorY += ((Insets)parentComponent.padding().get()).top();
               }
            }

            if (inspectorY < 0) {
               inspectorY = 1;
            }

            if (inspectorX > client.getWindow().getGuiScaledWidth() - inspectorWidth) {
               inspectorX = client.getWindow().getGuiScaledWidth() - inspectorWidth - 2;
            }

            if (inspectorX < 0) {
               inspectorX = 1;
            }

            self.fill(pipeline, inspectorX, inspectorY, inspectorX + inspectorWidth + 3, inspectorY + inspectorHeight, -1493172224);
            self.drawRectOutline(pipeline, inspectorX, inspectorY, inspectorWidth + 3, inspectorHeight, -1493172224);
            self.textWithWordWrap(textRenderer, message, inspectorX + 2, inspectorY + 2, inspectorWidth, -1, false);
         }
      }

   }

   public static enum TextAnchor {
      TOP_RIGHT,
      BOTTOM_RIGHT,
      TOP_LEFT,
      BOTTOM_LEFT;

      private static TextAnchor[] $values() {
         return new TextAnchor[]{TOP_RIGHT, BOTTOM_RIGHT, TOP_LEFT, BOTTOM_LEFT};
      }
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
