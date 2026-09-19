package ml.mypals.carpetgui.ui.container;

import ml.mypals.carpetgui.ui.core.Insets;
import ml.mypals.carpetgui.ui.core.OwoUIGraphics;
import ml.mypals.carpetgui.ui.core.ParentUIComponent;
import ml.mypals.carpetgui.ui.core.Positioning;
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.core.Surface;
import ml.mypals.carpetgui.ui.core.UIComponent;
import ml.mypals.carpetgui.ui.event.UIEvents.KeyPress;
import ml.mypals.carpetgui.ui.util.EventSource;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.Nullable;

public class OverlayContainer<C extends UIComponent> extends WrappingParentUIComponent<C> {
   protected boolean closeOnClick = true;
   protected @Nullable EventSource<?>.Subscription exitSubscription = null;

   protected OverlayContainer(C child) {
      super(Sizing.fill(100), Sizing.fill(100), child);
      this.positioning(Positioning.absolute(0, 0));
      this.surface(Surface.VANILLA_TRANSLUCENT);
   }

   public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
      super.draw(graphics, mouseX, mouseY, partialTicks, delta);
      this.drawChildren(graphics, mouseX, mouseY, partialTicks, delta, this.childView);
   }

   public void drawFocusHighlight(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
   }

   public void mount(ParentUIComponent parent, int x, int y) {
      super.mount(parent, x, y);
      this.exitSubscription = this.root().keyPress().subscribe((KeyPress)(input) -> {
         if (input.isEscape()) {
            this.remove();
            return true;
         } else {
            return false;
         }
      });
   }

   public void dismount(UIComponent.DismountReason reason) {
      super.dismount(reason);
      if (this.exitSubscription != null) {
         this.exitSubscription.cancel();
      }

   }

   public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
      boolean handled = super.onMouseDown(click, doubled) || this.child.isInBoundingBox(click.x(), click.y());
      if (!handled && this.closeOnClick) {
         this.remove();
         return true;
      } else {
         return handled;
      }
   }

   public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
      super.onMouseScroll(mouseX, mouseY, amount);
      return true;
   }

   public boolean canFocus(UIComponent.FocusSource source) {
      return source == UIComponent.FocusSource.KEYBOARD_CYCLE;
   }

   protected int childMountX() {
      return this.x + ((Insets)this.padding.get()).left() + (this.width - this.child.fullSize().width()) / 2;
   }

   protected int childMountY() {
      return this.y + ((Insets)this.padding.get()).top() + (this.height() - this.child.fullSize().height()) / 2;
   }

   public OverlayContainer<C> closeOnClick(boolean closeOnClick) {
      this.closeOnClick = closeOnClick;
      return this;
   }

   public boolean closeOnClick() {
      return this.closeOnClick;
   }
}
