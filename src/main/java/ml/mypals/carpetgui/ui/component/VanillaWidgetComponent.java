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
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

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

   protected int determineVerticalContentSize(Sizing sizing) {
      if (!(this.widget instanceof Button) && !(this.widget instanceof Checkbox) && !(this.widget instanceof AbstractSliderButton)) {
         AbstractWidget var3 = this.widget;
         if (var3 instanceof EditBox) {
            EditBox textField = (EditBox)var3;
            return ((EditBoxAccessor)textField).carpetGUI$bordered() ? 20 : 9;
         } else {
            throw new UnsupportedOperationException(this.widget.getClass().getSimpleName() + " does not support Sizing.content() on the vertical axis");
         }
      } else {
         return 20;
      }
   }

   protected int determineHorizontalContentSize(Sizing sizing) {
      AbstractWidget var4 = this.widget;
      if (var4 instanceof Button button) {
         return Minecraft.getInstance().font.width(button.getMessage()) + 8;
      } else {
         var4 = this.widget;
         if (var4 instanceof Checkbox checkbox) {
            return Minecraft.getInstance().font.width(checkbox.getMessage()) + 24;
         } else {
            throw new UnsupportedOperationException(this.widget.getClass().getSimpleName() + " does not support Sizing.content() on the horizontal axis");
         }
      }
   }

   public BaseUIComponent carpetGUI$margins(Insets margins) {
      return this.widget instanceof EditBox ? super.carpetGUI$margins(margins.add(1, 1, 1, 1)) : super.carpetGUI$margins(margins);
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

   private void applyToWidget() {
      AbstractWidgetAccessor accessor = (AbstractWidgetAccessor)this.widget;
      accessor.carpetGUI$setX(this.x + this.widget.carpetGUI$xOffset());
      accessor.carpetGUI$setY(this.y + this.widget.carpetGUI$yOffset());
      accessor.carpetGUI$setWidth(this.width + this.widget.carpetGUI$widthOffset());
      accessor.carpetGUI$setHeight(this.height + this.widget.carpetGUI$heightOffset());
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
      this.widget.extractRenderState(graphics, mouseX, mouseY, 0.0F);
   }

   public boolean carpetGUI$shouldDrawTooltip(double mouseX, double mouseY) {
      return this.widget.visible && this.widget.active && super.carpetGUI$shouldDrawTooltip(mouseX, mouseY);
   }

   public boolean carpetGUI$onMouseDown(MouseButtonEvent click, boolean doubled) {
      return this.widget.mouseClicked(new MouseButtonEvent((double)this.x + click.x(), (double)this.y + click.y(), click.buttonInfo()), doubled) | super.carpetGUI$onMouseDown(click, doubled);
   }

   public boolean carpetGUI$onMouseUp(MouseButtonEvent click) {
      return this.widget.mouseReleased(new MouseButtonEvent((double)this.x + click.x(), (double)this.y + click.y(), click.buttonInfo())) | super.carpetGUI$onMouseUp(click);
   }

   public boolean carpetGUI$onMouseScroll(double mouseX, double mouseY, double amount) {
      return this.widget.mouseScrolled((double)this.x + mouseX, (double)this.y + mouseY, (double)0.0F, amount) | super.carpetGUI$onMouseScroll(mouseX, mouseY, amount);
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
}
