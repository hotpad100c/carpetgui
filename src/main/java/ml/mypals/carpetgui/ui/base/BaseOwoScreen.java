package ml.mypals.carpetgui.ui.base;

import ml.mypals.carpetgui.ui.UI;
import ml.mypals.carpetgui.ui.core.CursorStyle;
import ml.mypals.carpetgui.ui.core.OwoUIAdapter;
import ml.mypals.carpetgui.ui.core.ParentUIComponent;
import ml.mypals.carpetgui.ui.core.UIComponent;
import ml.mypals.carpetgui.ui.inject.GreedyInputUIComponent;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseOwoScreen<R extends ParentUIComponent> extends Screen {
   protected OwoUIAdapter<R> uiAdapter;
   protected boolean invalid;

   protected BaseOwoScreen(Component title) {
      super(title);
      this.uiAdapter = null;
      this.invalid = false;
   }

   protected BaseOwoScreen() {
      this(Component.empty());
   }

   protected abstract @NotNull OwoUIAdapter<R> createAdapter();

   protected abstract void build(R var1);

   protected void init() {
      if (!this.invalid) {
         if (this.uiAdapter != null) {
            this.uiAdapter.moveAndResize(0, 0, this.width, this.height);
            this.addRenderableWidget(this.uiAdapter);
         } else {
            try {
               this.uiAdapter = this.createAdapter();
               this.build(this.uiAdapter.rootComponent);
               this.uiAdapter.inflateAndMount();
            } catch (Exception error) {
               UI.LOGGER.error("Could not initialize owo screen", error);
               this.invalid = true;
            }
         }

         ScreenEvents.afterExtract(this).register((ScreenEvents.AfterExtract)(screen, drawContext, mouseX, mouseY, tickDelta) -> this.drawComponentTooltip(drawContext, mouseX, mouseY, tickDelta));
      }
   }

   protected void drawComponentTooltip(GuiGraphicsExtractor drawContext, int mouseX, int mouseY, float tickDelta) {
      if (this.uiAdapter != null) {
         this.uiAdapter.drawTooltip(drawContext, mouseX, mouseY, tickDelta);
      }
   }

   public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
   }

   public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
      if (!this.invalid) {
         super.extractRenderState(graphics, mouseX, mouseY, a);
      } else {
         this.onClose();
      }

   }

   public boolean keyPressed(KeyEvent input) {
      if (this.uiAdapter == null) {
         return false;
      } else {
         if (!input.hasControlDown()) {
            UIComponent var3 = this.uiAdapter.rootComponent.carpetGUI$focusHandler().focused();
            if (var3 instanceof GreedyInputUIComponent) {
               GreedyInputUIComponent inputComponent = (GreedyInputUIComponent)var3;
               if (inputComponent.carpetGUI$onKeyPress(input)) {
                  return true;
               }
            }
         }

         if (super.keyPressed(input)) {
            return true;
         } else if (input.isEscape() && this.shouldCloseOnEsc()) {
            this.onClose();
            return true;
         } else {
            return false;
         }
      }
   }

   public boolean mouseDragged(MouseButtonEvent click, double deltaX, double deltaY) {
      return this.uiAdapter == null ? false : this.uiAdapter.mouseDragged(click, deltaX, deltaY);
   }

   public @Nullable GuiEventListener getFocused() {
      return this.uiAdapter;
   }

   public void removed() {
      if (this.uiAdapter != null) {
         this.uiAdapter.cursorAdapter.applyStyle(CursorStyle.NONE);
         this.dispose();
      }
   }

   public void dispose() {
      if (this.uiAdapter != null) {
         this.uiAdapter.dispose();
      }

   }
}
