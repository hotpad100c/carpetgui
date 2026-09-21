package ml.mypals.carpetgui.ui.container;

import java.util.Collections;
import java.util.List;
import ml.mypals.carpetgui.ui.base.BaseParentUIComponent;
import ml.mypals.carpetgui.ui.core.Insets;
import ml.mypals.carpetgui.ui.core.OwoUIGraphics;
import ml.mypals.carpetgui.ui.core.ParentUIComponent;
import ml.mypals.carpetgui.ui.core.Positioning;
import ml.mypals.carpetgui.ui.core.Size;
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.core.Surface;
import ml.mypals.carpetgui.ui.core.UIComponent;
import ml.mypals.carpetgui.ui.event.UIEvents.KeyPress;
import ml.mypals.carpetgui.ui.util.EventStream;
//? if >=1.21.9 {
import net.minecraft.client.input.MouseButtonEvent;
//?} else {
/*import ml.mypals.carpetgui.compat.input.MouseButtonEvent;
*///?}
import org.jetbrains.annotations.Nullable;

public class OverlayContainer<C extends UIComponent> extends BaseParentUIComponent {
   protected C child;
   protected List<UIComponent> childView;
   protected boolean closeOnClick = true;
   protected @Nullable EventStream.Subscription exitSubscription = null;

   protected OverlayContainer(C child) {
      super(Sizing.fill(100), Sizing.fill(100));
      this.child = child;
      this.childView = Collections.singletonList(this.child);
      this.carpetGUI$positioning(Positioning.absolute(0, 0));
      this.surface(Surface.VANILLA_TRANSLUCENT);
   }

   public C child() {
      return this.child;
   }

   public List<UIComponent> children() {
      return this.childView;
   }

   public ParentUIComponent removeChild(UIComponent child) {
      throw new UnsupportedOperationException("Cannot remove the child of an overlay container");
   }

   public void layout(Size space) {
      this.child.carpetGUI$inflate(this.calculateChildSpace(space));
      this.child.carpetGUI$mount(this, this.childMountX(), this.childMountY());
   }

   protected int determineHorizontalContentSize(Sizing sizing) {
      return this.child.carpetGUI$fullSize().width() + ((Insets)this.padding.get()).horizontal();
   }

   protected int determineVerticalContentSize(Sizing sizing) {
      return this.child.carpetGUI$fullSize().height() + ((Insets)this.padding.get()).vertical();
   }

   public void carpetGUI$draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
      super.carpetGUI$draw(graphics, mouseX, mouseY, partialTicks, delta);
      this.drawChildren(graphics, mouseX, mouseY, partialTicks, delta, this.childView);
   }

   public void drawFocusHighlight(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
   }

   public void carpetGUI$mount(ParentUIComponent parent, int x, int y) {
      super.carpetGUI$mount(parent, x, y);
      this.exitSubscription = this.carpetGUI$root().carpetGUI$keyPress().subscribe((KeyPress)(input) -> {
         if (input.isEscape()) {
            this.carpetGUI$remove();
            return true;
         } else {
            return false;
         }
      });
   }

   public void carpetGUI$dismount(UIComponent.DismountReason reason) {
      super.carpetGUI$dismount(reason);
      if (this.exitSubscription != null) {
         this.exitSubscription.cancel();
      }
   }

   public boolean carpetGUI$onMouseDown(MouseButtonEvent click, boolean doubled) {
      boolean handled = super.carpetGUI$onMouseDown(click, doubled) || this.child.carpetGUI$isInBoundingBox(click.x(), click.y());
      if (!handled && this.closeOnClick) {
         this.carpetGUI$remove();
         return true;
      } else {
         return handled;
      }
   }

   public boolean carpetGUI$onMouseScroll(double mouseX, double mouseY, double amount) {
      super.carpetGUI$onMouseScroll(mouseX, mouseY, amount);
      return true;
   }

   public boolean carpetGUI$canFocus(UIComponent.FocusSource source) {
      return source == UIComponent.FocusSource.KEYBOARD_CYCLE;
   }

   protected int childMountX() {
      return this.x + ((Insets)this.padding.get()).left() + (this.width - this.child.carpetGUI$fullSize().width()) / 2;
   }

   protected int childMountY() {
      return this.y + ((Insets)this.padding.get()).top() + (this.carpetGUI$height() - this.child.carpetGUI$fullSize().height()) / 2;
   }

   public OverlayContainer<C> closeOnClick(boolean closeOnClick) {
      this.closeOnClick = closeOnClick;
      return this;
   }

   public boolean closeOnClick() {
      return this.closeOnClick;
   }
}
