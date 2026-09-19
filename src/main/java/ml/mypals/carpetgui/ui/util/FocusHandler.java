package ml.mypals.carpetgui.ui.util;

import java.util.ArrayList;
import ml.mypals.carpetgui.ui.core.ParentUIComponent;
import ml.mypals.carpetgui.ui.core.UIComponent;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

public class FocusHandler {
   protected final ParentUIComponent root;
   protected @Nullable UIComponent focused = null;
   protected UIComponent.@Nullable FocusSource lastFocusSource = null;

   public FocusHandler(ParentUIComponent root) {
      this.root = root;
   }

   public void updateClickFocus(double mouseX, double mouseY) {
      UIComponent clicked = this.root.childAt((int)mouseX, (int)mouseY);
      this.focus(clicked != null && clicked.canFocus(UIComponent.FocusSource.MOUSE_CLICK) ? clicked : null, UIComponent.FocusSource.MOUSE_CLICK);
   }

   @Contract(
      pure = true
   )
   public @Nullable UIComponent focused() {
      return this.focused;
   }

   public UIComponent.FocusSource lastFocusSource() {
      return this.lastFocusSource;
   }

   public void cycle(boolean forwards) {
      ArrayList<UIComponent> allChildren = new ArrayList();
      this.root.collectDescendants(allChildren);
      allChildren.removeIf((component) -> !component.canFocus(UIComponent.FocusSource.KEYBOARD_CYCLE));
      if (!allChildren.isEmpty()) {
         int newIndex = this.focused == null ? (forwards ? 0 : allChildren.size() - 1) : allChildren.indexOf(this.focused) + (forwards ? 1 : -1);
         if (newIndex >= allChildren.size()) {
            newIndex -= allChildren.size();
         }

         if (newIndex < 0) {
            newIndex += allChildren.size();
         }

         this.focus((UIComponent)allChildren.get(newIndex), UIComponent.FocusSource.KEYBOARD_CYCLE);
      }
   }

   public void moveFocus(int keyCode) {
      if (this.focused != null) {
         ArrayList<UIComponent> allChildren = new ArrayList();
         this.root.collectDescendants(allChildren);
         allChildren.removeIf((component) -> !component.canFocus(UIComponent.FocusSource.KEYBOARD_CYCLE));
         if (!allChildren.isEmpty()) {
            UIComponent closest = this.focused;
            switch (keyCode) {
               case 262: {
                  int closestX = Integer.MAX_VALUE;
                  int closestY = Integer.MAX_VALUE;

                  for(UIComponent child : allChildren) {
                     if (child != this.focused && child.x() >= this.focused.x() + this.focused.width() && child.x() <= closestX && Math.abs(child.y() - this.focused.y()) <= closestY) {
                        closest = child;
                        closestX = child.x();
                        closestY = Math.abs(child.y() - this.focused.y());
                     }
                  }
                  break;
               }
               case 263: {
                  int closestX = 0;
                  int closestY = Integer.MAX_VALUE;

                  for(UIComponent child : allChildren) {
                     if (child != this.focused && child.x() + child.width() <= this.focused.x() && child.x() + child.width() >= closestX && Math.abs(child.y() - this.focused.y()) <= closestY) {
                        closest = child;
                        closestX = child.x() + child.width();
                        closestY = Math.abs(child.y() - this.focused.y());
                     }
                  }
                  break;
               }
               case 264: {
                  int closestX = Integer.MAX_VALUE;
                  int closestY = Integer.MAX_VALUE;

                  for(UIComponent child : allChildren) {
                     if (child != this.focused && child.y() >= this.focused.y() + this.focused.height() && child.y() + child.height() <= closestY && Math.abs(child.x() - this.focused.x()) <= closestX) {
                        closest = child;
                        closestX = Math.abs(child.x() - this.focused.x());
                        closestY = child.y() + child.height();
                     }
                  }
                  break;
               }
               case 265: {
                  int closestX = Integer.MAX_VALUE;
                  int closestY = 0;

                  for(UIComponent child : allChildren) {
                     if (child != this.focused && child.y() + child.height() <= this.focused.y() && child.y() + child.height() >= closestY && Math.abs(child.x() - this.focused.x()) <= closestX) {
                        closest = child;
                        closestX = Math.abs(child.x() - this.focused.x());
                        closestY = child.y() + child.height();
                     }
                  }
                  break;
               }
            }

            this.focus(closest, UIComponent.FocusSource.KEYBOARD_CYCLE);
         }
      }
   }

   public void focus(@Nullable UIComponent component, UIComponent.FocusSource source) {
      if (this.focused != component) {
         if (this.focused != null) {
            this.focused.onFocusLost();
         }

         if ((this.focused = component) != null) {
            this.focused.onFocusGained(source);
            this.lastFocusSource = source;
         } else {
            this.lastFocusSource = null;
         }
      }

   }
}
