package ml.mypals.carpetgui.ui.base;

import ml.mypals.carpetgui.ui.UI;
import ml.mypals.carpetgui.ui.core.CursorStyle;
import ml.mypals.carpetgui.ui.core.OwoUIAdapter;
import ml.mypals.carpetgui.ui.core.ParentUIComponent;
import ml.mypals.carpetgui.ui.core.UIComponent;
import ml.mypals.carpetgui.ui.inject.GreedyInputUIComponent;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
//? if <26.1 {
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?}
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
//? if >=1.21.9 {
/*import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
*///?} else {
import ml.mypals.carpetgui.compat.input.KeyEvent;
import ml.mypals.carpetgui.compat.input.MouseButtonEvent;
//?}
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
      this(net.minecraft.network.chat.TextComponent.EMPTY);
   }

   protected abstract @NotNull OwoUIAdapter<R> createAdapter();

   protected abstract void build(R var1);

   protected void init() {
      if (!this.invalid) {
         if (this.uiAdapter != null) {
            this.uiAdapter.moveAndResize(0, 0, this.width, this.height);
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

         if (this.uiAdapter != null) {
            //? if <1.17 {
            this.addWidget(this.uiAdapter);
            //?} else {
            /*this.addRenderableWidget(this.uiAdapter);
            *///?}
         }

         //? if <1.20 {
         ScreenEvents.afterRender(this).register((screen, poseStack, mouseX, mouseY, tickDelta) -> this.drawComponentTooltip(new GuiGraphics(Minecraft.getInstance(), poseStack, Minecraft.getInstance().renderBuffers().bufferSource()), mouseX, mouseY, tickDelta));
         //?} elif <26.1 {
         /*ScreenEvents.afterRender(this).register((ScreenEvents.AfterRender)(screen, drawContext, mouseX, mouseY, tickDelta) -> this.drawComponentTooltip(drawContext, mouseX, mouseY, tickDelta));
         *///?} else {
         /*ScreenEvents.afterExtract(this).register((ScreenEvents.AfterExtract)(screen, drawContext, mouseX, mouseY, tickDelta) -> this.drawComponentTooltip(drawContext, mouseX, mouseY, tickDelta));
         *///?}
      }
   }

   //? if <26.1 {
   protected void drawComponentTooltip(GuiGraphics drawContext, int mouseX, int mouseY, float tickDelta) {
      if (this.uiAdapter != null) {
         this.uiAdapter.drawTooltip(drawContext, mouseX, mouseY, tickDelta);
      }
   }

   public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float a) {
   }

   //? if <1.20 {
   public void render(com.mojang.blaze3d.vertex.PoseStack poseStack, int mouseX, int mouseY, float a) {
      if (!this.invalid) {
         super.render(poseStack, mouseX, mouseY, a);
         //? if <1.17 {
         if (this.uiAdapter != null) {
            this.uiAdapter.render(poseStack, mouseX, mouseY, a);
         }
         //?}
      } else {
         this.onClose();
      }
   }

   public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
      this.render(graphics.pose(), mouseX, mouseY, a);
   }
   
   //?} else {
   /*public void render(GuiGraphics graphics, int mouseX, int mouseY, float a) {
      if (!this.invalid) {
         super.render(graphics, mouseX, mouseY, a);
      } else {
         this.onClose();
      }
   }
   *///?}
   //?} else {
   /*protected void drawComponentTooltip(GuiGraphicsExtractor drawContext, int mouseX, int mouseY, float tickDelta) {
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
   *///?}

   //? if <1.21.9 {
   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      KeyEvent input = new KeyEvent(keyCode, scanCode, modifiers);
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

         if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
         } else if (input.isEscape() && this.shouldCloseOnEsc()) {
            this.onClose();
            return true;
         } else {
            return false;
         }
      }
   }

   @Override
   public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
      return this.uiAdapter == null ? false : this.uiAdapter.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
   }
   //?} else {
   /*public boolean keyPressed(KeyEvent input) {
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
   *///?}

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
