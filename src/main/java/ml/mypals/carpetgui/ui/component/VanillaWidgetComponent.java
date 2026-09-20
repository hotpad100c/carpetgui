package ml.mypals.carpetgui.ui.component;

import java.util.function.Consumer;
import ml.mypals.carpetgui.mixin.ui.AbstractWidgetAccessor;
import ml.mypals.carpetgui.mixin.ui.EditBoxAccessor;
import ml.mypals.carpetgui.ui.base.BaseUIComponent;
import ml.mypals.carpetgui.ui.core.Insets;
import ml.mypals.carpetgui.ui.core.OwoUIGraphics;
import ml.mypals.carpetgui.ui.core.ParentUIComponent;
import ml.mypals.carpetgui.ui.core.Size;
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.core.UIComponent;
import ml.mypals.carpetgui.ui.event.UIEvents.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
//? if >=1.21.9 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?} else {
/*import ml.mypals.carpetgui.compat.input.CharacterEvent;
import ml.mypals.carpetgui.compat.input.KeyEvent;
import ml.mypals.carpetgui.compat.input.MouseButtonEvent;
*///?}

public class VanillaWidgetComponent extends BaseUIComponent {
   private final AbstractWidget widget;

   protected VanillaWidgetComponent(AbstractWidget widget) {
      this.widget = widget;
      this.horizontalSizing.set(Sizing.fixed(this.widget.getWidth()));
      this.verticalSizing.set(Sizing.fixed(this.widget.getHeight()));
      if (widget instanceof EditBox) {
         this.carpetGUI$margins(Insets.none());
      }

   }

   public boolean hovered() {
      return this.hovered;
   }

   public void carpetGUI$mount(ParentUIComponent parent, int x, int y) {
      super.carpetGUI$mount(parent, x, y);
      this.applyToWidget();
   }

   protected void updateHoveredState(int mouseX, int mouseY, boolean nowHovered) {
      this.hovered = nowHovered;
      if (nowHovered) {
         if (this.root() == null || this.root().childAt(mouseX, mouseY) != this.widget) {
            this.hovered = false;
            return;
         }

         ((MouseEnter)this.mouseEnterEvents.sink()).onMouseEnter();
      } else {
         ((MouseLeave)this.mouseLeaveEvents.sink()).onMouseLeave();
      }

   }

   public void applyToWidget() {
      AbstractWidgetAccessor accessor = (AbstractWidgetAccessor)this.widget;
      accessor.carpetGUI$setX(this.x);
      accessor.carpetGUI$setY(this.y);
      accessor.carpetGUI$setWidth(this.width);
      accessor.carpetGUI$setHeight(this.height);
      if (this.widget instanceof EditBox) {
         //? if >=1.21.6 {
         ((EditBoxAccessor)this.widget).carpetGUI$updateTextPosition();
         //?}
      }

   }

   public void carpetGUI$inflate(Size space) {
      super.carpetGUI$inflate(space);
      this.applyToWidget();
   }

   public void carpetGUI$updateX(int x) {
      super.carpetGUI$updateX(x);
      this.applyToWidget();
   }

   public void carpetGUI$updateY(int y) {
      super.carpetGUI$updateY(y);
      this.applyToWidget();
   }

   protected int determineHorizontalContentSize(Sizing sizing) {
      if (this.widget instanceof Button || this.widget instanceof Checkbox) {
         return Minecraft.getInstance().font.width(this.widget.getMessage()) + (this.widget instanceof Checkbox ? 24 : 12);
      } else if (this.widget instanceof AbstractSliderButton) {
         return Minecraft.getInstance().font.width(this.widget.getMessage()) + 8;
      } else {
         return super.determineHorizontalContentSize(sizing);
      }
   }

   protected int determineVerticalContentSize(Sizing sizing) {
      if (this.widget instanceof Button || this.widget instanceof Checkbox || this.widget instanceof AbstractSliderButton) {
         return 20;
      } else {
         return super.determineVerticalContentSize(sizing);
      }
   }

   public <C extends UIComponent> C carpetGUI$configure(Consumer<C> closure) {
      try {
         this.runAndDeferEvents(() -> closure.accept((C) this.widget));
      } catch (ClassCastException theUserDidBadItWasNotMyFault) {
         throw new IllegalArgumentException("Invalid target class passed when configuring component of type " + this.getClass().getSimpleName(), theUserDidBadItWasNotMyFault);
      }

      return (C)this.widget;
   }

   public void notifyParentIfMounted() {
      super.notifyParentIfMounted();
   }

   public void carpetGUI$draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
      //? if <1.20 {
      /*this.widget.render(graphics.pose(), mouseX, mouseY, 0.0F);
      *///?} elif <26.1 {
      /*this.widget.render(graphics, mouseX, mouseY, 0.0F);
      *///?} else {
      this.widget.extractRenderState(graphics, mouseX, mouseY, 0.0F);
      //?}
   }

   public boolean carpetGUI$shouldDrawTooltip(double mouseX, double mouseY) {
      return this.widget.visible && this.widget.active && super.carpetGUI$shouldDrawTooltip(mouseX, mouseY);
   }

   public boolean carpetGUI$onMouseScroll(double mouseX, double mouseY, double amount) {
      //? if <1.20.2 {
      /*return this.widget.mouseScrolled((double)this.x + mouseX, (double)this.y + mouseY, amount) | super.carpetGUI$onMouseScroll(mouseX, mouseY, amount);
      *///?} else {
      return this.widget.mouseScrolled((double)this.x + mouseX, (double)this.y + mouseY, (double)0.0F, amount) | super.carpetGUI$onMouseScroll(mouseX, mouseY, amount);
      //?}
   }

   //? if <1.21.9 {
   /*public boolean carpetGUI$onMouseDown(MouseButtonEvent click, boolean doubled) {
      return this.widget.mouseClicked((double)this.x + click.x(), (double)this.y + click.y(), click.button()) | super.carpetGUI$onMouseDown(click, doubled);
   }

   public boolean carpetGUI$onMouseUp(MouseButtonEvent click) {
      return this.widget.mouseReleased((double)this.x + click.x(), (double)this.y + click.y(), click.button()) | super.carpetGUI$onMouseUp(click);
   }

   public boolean carpetGUI$onMouseDrag(MouseButtonEvent click, double deltaX, double deltaY) {
      return this.widget.mouseDragged((double)this.x + click.x(), (double)this.y + click.y(), click.button(), deltaX, deltaY) | super.carpetGUI$onMouseDrag(click, deltaX, deltaY);
   }

   public boolean carpetGUI$onCharTyped(CharacterEvent input) {
      return this.widget.charTyped((char)input.codepoint(), input.modifiers()) | super.carpetGUI$onCharTyped(input);
   }

   public boolean carpetGUI$onKeyPress(KeyEvent input) {
      return this.widget.keyPressed(input.key(), input.scancode(), input.modifiers()) | super.carpetGUI$onKeyPress(input);
   }
   *///?} else {
   public boolean carpetGUI$onMouseDown(MouseButtonEvent click, boolean doubled) {
      return this.widget.mouseClicked(new MouseButtonEvent((double)this.x + click.x(), (double)this.y + click.y(), click.buttonInfo()), doubled) | super.carpetGUI$onMouseDown(click, doubled);
   }

   public boolean carpetGUI$onMouseUp(MouseButtonEvent click) {
      return this.widget.mouseReleased(new MouseButtonEvent((double)this.x + click.x(), (double)this.y + click.y(), click.buttonInfo())) | super.carpetGUI$onMouseUp(click);
   }

   public boolean carpetGUI$onMouseDrag(MouseButtonEvent click, double deltaX, double deltaY) {
      return this.widget.mouseDragged(new MouseButtonEvent((double)this.x + click.x(), (double)this.y + click.y(), click.buttonInfo()), deltaX, deltaY) | super.carpetGUI$onMouseDrag(click, deltaX, deltaY);
   }

   public boolean carpetGUI$onCharTyped(CharacterEvent input) {
      return this.widget.charTyped(input) | super.carpetGUI$onCharTyped(input);
   }

   public boolean carpetGUI$onKeyPress(KeyEvent input) {
      return this.widget.keyPressed(input) | super.carpetGUI$onKeyPress(input);
   }
   //?}
}
