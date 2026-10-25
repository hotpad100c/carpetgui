package ml.mypals.carpetgui.ui.component;

import java.util.function.IntConsumer;
import ml.mypals.carpetgui.ui.base.BaseUIComponent;
import ml.mypals.carpetgui.ui.core.CursorStyle;
import ml.mypals.carpetgui.ui.core.OwoUIGraphics;
import ml.mypals.carpetgui.ui.core.Sizing;
//? if >=1.21.9 {
import net.minecraft.client.input.MouseButtonEvent;
//?} else {
/*import ml.mypals.carpetgui.compat.input.MouseButtonEvent;
*///?}

public class SliderComponent extends BaseUIComponent {
   private static final int HANDLE_WIDTH = 4;
   protected final int min;
   protected final int max;
   protected int value;
   protected IntConsumer onChanged = (v) -> {};
   protected Runnable onReleased = () -> {};
   protected boolean dragging = false;

   protected SliderComponent(int min, int max, int value) {
      this.min = min;
      this.max = max;
      this.value = Math.max(min, Math.min(max, value));
      this.carpetGUI$cursorStyle(CursorStyle.HAND);
   }

   public SliderComponent onChanged(IntConsumer onChanged) {
      this.onChanged = onChanged;
      return this;
   }

   public SliderComponent onReleased(Runnable onReleased) {
      this.onReleased = onReleased;
      return this;
   }

   public int value() {
      return this.value;
   }

   public SliderComponent value(int value) {
      this.value = Math.max(this.min, Math.min(this.max, value));
      return this;
   }

   protected int determineHorizontalContentSize(Sizing sizing) {
      return 60;
   }

   protected int determineVerticalContentSize(Sizing sizing) {
      return 10;
   }

   public void carpetGUI$draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
      int centerY = this.y + this.height / 2;
      int handleX = this.x + this.handleOffset();
      graphics.fill(this.x, centerY - 1, this.x + this.width, centerY + 1, 1442840575);
      graphics.fill(this.x, centerY - 1, handleX, centerY + 1, -5592406);
      int handleColor = this.hovered || this.dragging ? -1 : -3355444;
      graphics.fill(handleX, this.y, handleX + HANDLE_WIDTH, this.y + this.height, handleColor);
   }

   private int handleOffset() {
      int range = this.max - this.min;
      return range <= 0 ? 0 : (int)Math.round((double)(this.value - this.min) / (double)range * (double)(this.width - HANDLE_WIDTH));
   }

   private void setFromMouse(double mouseX) {
      int track = this.width - HANDLE_WIDTH;
      double t = track <= 0 ? 0.0 : (mouseX - (double)HANDLE_WIDTH / 2.0) / (double)track;
      t = Math.max(0.0, Math.min(1.0, t));
      this.setValue((int)Math.round((double)this.min + t * (double)(this.max - this.min)));
   }

   private void setValue(int newValue) {
      newValue = Math.max(this.min, Math.min(this.max, newValue));
      if (newValue != this.value) {
         this.value = newValue;
         this.onChanged.accept(newValue);
      }

   }

   public boolean carpetGUI$canFocus(FocusSource source) {
      return source == FocusSource.MOUSE_CLICK;
   }

   public boolean carpetGUI$onMouseDown(MouseButtonEvent click, boolean doubled) {
      super.carpetGUI$onMouseDown(click, doubled);
      this.dragging = true;
      this.setFromMouse(click.x());
      return true;
   }

   public boolean carpetGUI$onMouseDrag(MouseButtonEvent click, double deltaX, double deltaY) {
      super.carpetGUI$onMouseDrag(click, deltaX, deltaY);
      if (this.dragging) {
         this.setFromMouse(click.x());
      }

      return true;
   }

   public boolean carpetGUI$onMouseUp(MouseButtonEvent click) {
      super.carpetGUI$onMouseUp(click);
      if (this.dragging) {
         this.dragging = false;
         this.onReleased.run();
      }

      return true;
   }

   public boolean carpetGUI$onMouseScroll(double mouseX, double mouseY, double amount) {
      super.carpetGUI$onMouseScroll(mouseX, mouseY, amount);
      this.setValue(this.value + (amount > 0 ? 1 : -1));
      this.onReleased.run();
      return true;
   }
}
