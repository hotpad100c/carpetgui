package ml.mypals.carpetgui.ui.core;

import com.mojang.blaze3d.platform.Window;
import java.util.function.BiFunction;

import net.minecraft.client.Minecraft;
//? if <26.1 {
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?}
//? if <1.19 {
import net.minecraft.client.gui.components.Widget;
//?} else {
/*import net.minecraft.client.gui.components.Renderable;
*///?}
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
//? if >=1.21.9 {
/*import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
*///?} else {
import ml.mypals.carpetgui.compat.input.CharacterEvent;
import ml.mypals.carpetgui.compat.input.KeyEvent;
import ml.mypals.carpetgui.compat.input.MouseButtonEvent;
//?}

public class OwoUIAdapter<R extends ParentUIComponent> implements GuiEventListener,
//? if <1.19 {
Widget,
//?} else {
/*Renderable,
*///?}
NarratableEntry {
   private static boolean isRendering = false;
   public final R rootComponent;
   public final CursorAdapter cursorAdapter;
   protected boolean disposed = false;
   protected int x;
   protected int y;
   protected int width;
   protected int height;

   protected OwoUIAdapter(int x, int y, int width, int height, R rootComponent) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.cursorAdapter = CursorAdapter.ofClientWindow();
      this.rootComponent = rootComponent;
   }

   public static <R extends ParentUIComponent> OwoUIAdapter<R> create(Screen screen, BiFunction<Sizing, Sizing, R> rootComponentMaker) {
      R rootComponent = rootComponentMaker.apply(Sizing.fill(100), Sizing.fill(100));
      OwoUIAdapter<R> adapter = new OwoUIAdapter<>(0, 0, screen.width, screen.height, rootComponent);
      screen.setFocused(adapter);
      return adapter;
   }

   public void inflateAndMount() {
      this.rootComponent.carpetGUI$inflate(Size.of(this.width, this.height));
      this.rootComponent.carpetGUI$mount((ParentUIComponent)null, this.x, this.y);
   }

   public void moveAndResize(int x, int y, int width, int height) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.inflateAndMount();
   }

   public void dispose() {
      this.cursorAdapter.dispose();
      this.disposed = true;
   }

   public int x() {
      return this.x;
   }

   public int y() {
      return this.y;
   }

   public int width() {
      return this.width;
   }

   public int height() {
      return this.height;
   }

   private static float getDeltaTicks() {
      //? if >=1.21.2 {
      /*return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
      *///?} elif >=1.21 {
      /*return Minecraft.getInstance().getTimer().getGameTimeDeltaTicks();
      *///?} else {
      return Minecraft.getInstance().getDeltaFrameTime();
      //?}
   }

   //? if <1.20 {
   public void render(com.mojang.blaze3d.vertex.PoseStack poseStack, int mouseX, int mouseY, float a) {
      this.render(new GuiGraphics(Minecraft.getInstance(), poseStack, Minecraft.getInstance().renderBuffers().bufferSource()), mouseX, mouseY, a);
   }
   public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
   //?} elif <26.1 {
   /*public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
   *///?} else {
   /*public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
   *///?}
      if (!(graphics instanceof OwoUIGraphics)) {
         graphics = OwoUIGraphics.of(graphics);
      }

      OwoUIGraphics owoGraphics = (OwoUIGraphics)graphics;

      try {
         isRendering = true;
         float delta = getDeltaTicks();
         Window window = Minecraft.getInstance().getWindow();
         this.rootComponent.carpetGUI$update(delta, mouseX, mouseY);
         graphics.enableScissor(0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight());
         this.rootComponent.carpetGUI$draw(owoGraphics, mouseX, mouseY, a, delta);
         graphics.disableScissor();
         UIComponent hovered = this.rootComponent.childAt(mouseX, mouseY);
         if (!this.disposed && hovered != null) {
            this.cursorAdapter.applyStyle(hovered.carpetGUI$cursorStyle());
         }
      } finally {
         isRendering = false;
      }

   }

   //? if <26.1 {
   public void drawTooltip(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
   //?} else {
   /*public void drawTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
   *///?}
      if (!(graphics instanceof OwoUIGraphics)) {
         graphics = OwoUIGraphics.of(graphics);
      }

      OwoUIGraphics owoContext = (OwoUIGraphics)graphics;
      float delta = getDeltaTicks();
      this.rootComponent.drawTooltip(owoContext, mouseX, mouseY, partialTicks, delta);
      //? if >=26.1 {
      /*graphics.extractDeferredElements(mouseX, mouseY, partialTicks);
      *///?} elif >=1.21.9 {
      /*graphics.renderDeferredElements();
      *///?}
   }

   public boolean isMouseOver(double mouseX, double mouseY) {
      return this.rootComponent.isInBoundingBox(mouseX, mouseY);
   }

   public void setFocused(boolean focused) {
   }

   public boolean isFocused() {
      return true;
   }

   //? if <1.21.9 {
   public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return this.rootComponent.carpetGUI$onMouseDown(new MouseButtonEvent(mouseX, mouseY, button), false);
   }

   public boolean mouseReleased(double mouseX, double mouseY, int button) {
      return this.rootComponent.carpetGUI$onMouseUp(new MouseButtonEvent(mouseX, mouseY, button));
   }

   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      return this.rootComponent.carpetGUI$onMouseDrag(new MouseButtonEvent(mouseX, mouseY, button), deltaX, deltaY);
   }

   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.rootComponent.carpetGUI$onKeyPress(new KeyEvent(keyCode, scanCode, modifiers));
   }

   public boolean charTyped(char chr, int modifiers) {
      return this.rootComponent.carpetGUI$onCharTyped(new CharacterEvent(chr, modifiers));
   }
   //?} else {
   /*public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
      return this.rootComponent.carpetGUI$onMouseDown(click, doubled);
   }

   public boolean mouseReleased(MouseButtonEvent click) {
      return this.rootComponent.carpetGUI$onMouseUp(click);
   }

   public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
      return this.rootComponent.carpetGUI$onMouseDrag(click, deltaX, deltaY);
   }

   public boolean keyPressed(KeyEvent input) {
      return this.rootComponent.carpetGUI$onKeyPress(input);
   }

   public boolean charTyped(CharacterEvent input) {
      return this.rootComponent.carpetGUI$onCharTyped(input);
   }
   *///?}

   //? if >=1.20.2 {
   /*public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      return this.rootComponent.carpetGUI$onMouseScroll(mouseX, mouseY, verticalAmount);
   }
   *///?} else {
   public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
      return this.rootComponent.carpetGUI$onMouseScroll(mouseX, mouseY, amount);
   }
   //?}

   public NarratableEntry.NarrationPriority narrationPriority() {
      return NarrationPriority.NONE;
   }

   public void updateNarration(NarrationElementOutput builder) {
   }

   public static boolean isRendering() {
      return isRendering;
   }

   public static class CursorAdapter {
//? if <26.3 {
      protected static final CursorStyle[] ACTIVE_STYLES = new CursorStyle[]{CursorStyle.POINTER, CursorStyle.TEXT, CursorStyle.HAND, CursorStyle.CROSSHAIR, CursorStyle.MOVE, CursorStyle.HORIZONTAL_RESIZE, CursorStyle.VERTICAL_RESIZE, CursorStyle.NWSE_RESIZE, CursorStyle.NESW_RESIZE, CursorStyle.NOT_ALLOWED};
      protected final java.util.EnumMap<CursorStyle, Long> cursors = new java.util.EnumMap<>(CursorStyle.class);
      protected final long windowHandle;
      protected CursorStyle lastCursorStyle;
      protected boolean disposed;

      protected CursorAdapter(long windowHandle) {
         this.lastCursorStyle = CursorStyle.POINTER;
         this.disposed = false;
         this.windowHandle = windowHandle;

         for(CursorStyle style : ACTIVE_STYLES) {
            long pointer = org.lwjgl.glfw.GLFW.glfwCreateStandardCursor(style.glfw);
            if (pointer != 0L) {
               this.cursors.put(style, pointer);
            }
         }
      }

      public static CursorAdapter ofClientWindow() {
         return new CursorAdapter(Minecraft.getInstance().getWindow().getWindow());
      }

      public static CursorAdapter ofWindow(Window window) {
         return new CursorAdapter(window.getWindow());
      }

      public static CursorAdapter ofWindow(long windowHandle) {
         return new CursorAdapter(windowHandle);
      }

      public void applyStyle(CursorStyle style) {
         if (!this.disposed && this.lastCursorStyle != style) {
            if (style == CursorStyle.NONE) {
               org.lwjgl.glfw.GLFW.glfwSetCursor(this.windowHandle, 0L);
            } else {
               org.lwjgl.glfw.GLFW.glfwSetCursor(this.windowHandle, (Long)this.cursors.getOrDefault(style, 0L));
            }
            this.lastCursorStyle = style;
         }
      }

      public void dispose() {
         if (!this.disposed) {
            this.cursors.values().forEach(org.lwjgl.glfw.GLFW::glfwDestroyCursor);
            this.disposed = true;
         }
      }
//?} else {
      /*protected CursorStyle lastCursorStyle = CursorStyle.POINTER;
      protected boolean disposed = false;

      protected CursorAdapter() {}

      public static CursorAdapter ofClientWindow() {
         return new CursorAdapter();
      }

      public static CursorAdapter ofWindow(Window window) {
         return new CursorAdapter();
      }

      public static CursorAdapter ofWindow(long windowHandle) {
         return new CursorAdapter();
      }

      public void applyStyle(CursorStyle style) {
         if (!this.disposed && this.lastCursorStyle != style) {
            com.mojang.blaze3d.platform.cursor.CursorType cursorType = switch (style) {
               case POINTER -> com.mojang.blaze3d.platform.cursor.CursorTypes.ARROW;
               case TEXT -> com.mojang.blaze3d.platform.cursor.CursorTypes.IBEAM;
               case HAND -> com.mojang.blaze3d.platform.cursor.CursorTypes.POINTING_HAND;
               case CROSSHAIR -> com.mojang.blaze3d.platform.cursor.CursorTypes.CROSSHAIR;
               case MOVE -> com.mojang.blaze3d.platform.cursor.CursorTypes.RESIZE_ALL;
               case HORIZONTAL_RESIZE -> com.mojang.blaze3d.platform.cursor.CursorTypes.RESIZE_EW;
               case VERTICAL_RESIZE -> com.mojang.blaze3d.platform.cursor.CursorTypes.RESIZE_NS;
               case NOT_ALLOWED -> com.mojang.blaze3d.platform.cursor.CursorTypes.NOT_ALLOWED;
               default -> com.mojang.blaze3d.platform.cursor.CursorType.DEFAULT;
            };
            Minecraft.getInstance().getWindow().selectCursor(cursorType);
            this.lastCursorStyle = style;
         }
      }

      public void dispose() {
         if (!this.disposed) {
            Minecraft.getInstance().getWindow().selectCursor(com.mojang.blaze3d.platform.cursor.CursorType.DEFAULT);
            this.disposed = true;
         }
      }
*///?}
   }
}
