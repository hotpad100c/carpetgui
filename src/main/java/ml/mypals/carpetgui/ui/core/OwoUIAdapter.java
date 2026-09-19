package ml.mypals.carpetgui.ui.core;

import com.mojang.blaze3d.platform.Window;
import java.util.function.BiFunction;
import ml.mypals.carpetgui.ui.UI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.narration.NarratableEntry.NarrationPriority;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

public class OwoUIAdapter<R extends ParentUIComponent> implements GuiEventListener, Renderable, NarratableEntry {
   private static boolean isRendering = false;
   public final R rootComponent;
   public final CursorAdapter cursorAdapter;
   protected boolean disposed = false;
   protected int x;
   protected int y;
   protected int width;
   protected int height;
   public boolean enableInspector = false;
   public boolean globalInspector = false;
   public int inspectorZOffset = 1000;

   protected OwoUIAdapter(int x, int y, int width, int height, R rootComponent) {
      this.x = x;
      this.y = y;
      this.width = width;
      this.height = height;
      this.cursorAdapter = CursorAdapter.ofClientWindow();
      this.rootComponent = rootComponent;
   }

   public static <R extends ParentUIComponent> OwoUIAdapter<R> create(Screen screen, BiFunction<Sizing, Sizing, R> rootComponentMaker) {
      R rootComponent = (R)(rootComponentMaker.apply(Sizing.fill(100), Sizing.fill(100)));
      OwoUIAdapter<R> adapter = new OwoUIAdapter<R>(0, 0, screen.width, screen.height, rootComponent);
      screen.addRenderableWidget(adapter);
      screen.setFocused(adapter);
      return adapter;
   }

   public static <R extends ParentUIComponent> OwoUIAdapter<R> createWithoutScreen(int x, int y, int width, int height, BiFunction<Sizing, Sizing, R> rootComponentMaker) {
      R rootComponent = (R)(rootComponentMaker.apply(Sizing.fill(100), Sizing.fill(100)));
      return new OwoUIAdapter<R>(x, y, width, height, rootComponent);
   }

   public void inflateAndMount() {
      this.rootComponent.inflate(Size.of(this.width, this.height));
      this.rootComponent.mount((ParentUIComponent)null, this.x, this.y);
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

   public boolean toggleInspector() {
      return this.enableInspector = !this.enableInspector;
   }

   public boolean toggleGlobalInspector() {
      return this.globalInspector = !this.globalInspector;
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

   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
      if (!(graphics instanceof OwoUIGraphics)) {
         graphics = OwoUIGraphics.of(graphics);
      }

      OwoUIGraphics owoGraphics = (OwoUIGraphics)graphics;

      try {
         isRendering = true;
         float delta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
         Window window = Minecraft.getInstance().getWindow();
         this.rootComponent.update(delta, mouseX, mouseY);
         graphics.enableScissor(0, 0, window.getWidth(), window.getHeight());
         this.rootComponent.draw(owoGraphics, mouseX, mouseY, a, delta);
         graphics.disableScissor();
         UIComponent hovered = this.rootComponent.childAt(mouseX, mouseY);
         if (!this.disposed && hovered != null) {
            this.cursorAdapter.applyStyle(hovered.cursorStyle());
         }

         if (this.enableInspector) {
            OwoUIGraphics.drawInspector(owoGraphics, this.rootComponent, (double)mouseX, (double)mouseY, !this.globalInspector);
         }
      } finally {
         isRendering = false;
      }

   }

   public void drawTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
      if (!(graphics instanceof OwoUIGraphics)) {
         graphics = OwoUIGraphics.of(graphics);
      }

      OwoUIGraphics owoContext = (OwoUIGraphics)graphics;
      float delta = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaTicks();
      this.rootComponent.drawTooltip(owoContext, mouseX, mouseY, partialTicks, delta);
      graphics.extractDeferredElements(mouseX, mouseY, partialTicks);
   }

   public boolean isMouseOver(double mouseX, double mouseY) {
      return this.rootComponent.isInBoundingBox(mouseX, mouseY);
   }

   public void setFocused(boolean focused) {
   }

   public boolean isFocused() {
      return true;
   }

   public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
      return this.rootComponent.onMouseDown(click, doubled);
   }

   public boolean mouseReleased(MouseButtonEvent click) {
      return this.rootComponent.onMouseUp(click);
   }

   public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
      return this.rootComponent.onMouseScroll(mouseX, mouseY, verticalAmount);
   }

   public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
      return this.rootComponent.onMouseDrag(click, deltaX, deltaY);
   }

   public boolean keyPressed(KeyEvent input) {
      if (UI.DEBUG && input.key() == 340) {
         if (input.hasControlDown()) {
            this.toggleInspector();
         } else if (input.hasAltDown()) {
            this.toggleGlobalInspector();
         }
      }

      return this.rootComponent.onKeyPress(input);
   }

   public boolean charTyped(CharacterEvent input) {
      return this.rootComponent.onCharTyped(input);
   }

   public NarratableEntry.NarrationPriority narrationPriority() {
      return NarrationPriority.NONE;
   }

   public void updateNarration(NarrationElementOutput builder) {
   }

   public static boolean isRendering() {
      return isRendering;
   }

   public static class CursorAdapter {
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
         return new CursorAdapter(Minecraft.getInstance().getWindow().handle());
      }

      public static CursorAdapter ofWindow(Window window) {
         return new CursorAdapter(window.handle());
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
   }
}
