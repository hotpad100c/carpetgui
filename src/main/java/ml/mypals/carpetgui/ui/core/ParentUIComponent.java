package ml.mypals.carpetgui.ui.core;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import ml.mypals.carpetgui.ui.util.Observable;
import net.minecraft.ChatFormatting;
//? if >=1.21.9 {
/*import net.minecraft.client.input.MouseButtonEvent;
*///?} else {
import ml.mypals.carpetgui.compat.input.MouseButtonEvent;
//?}
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ParentUIComponent extends UIComponent {
   void layout(Size var1);

   void onChildMutated(UIComponent var1);

   void queue(Runnable var1);

   default ParentUIComponent alignment(HorizontalAlignment horizontalAlignment, VerticalAlignment verticalAlignment) {
      this.horizontalAlignment(horizontalAlignment);
      this.verticalAlignment(verticalAlignment);
      return this;
   }

   ParentUIComponent verticalAlignment(VerticalAlignment var1);

   VerticalAlignment verticalAlignment();

   ParentUIComponent horizontalAlignment(HorizontalAlignment var1);

   HorizontalAlignment horizontalAlignment();

   ParentUIComponent padding(Insets var1);

   Observable<Insets> padding();

   ParentUIComponent allowOverflow(boolean var1);

   boolean allowOverflow();

   ParentUIComponent surface(Surface var1);

   Surface surface();

   List<UIComponent> children();

   ParentUIComponent removeChild(UIComponent var1);

   default void drawTooltip(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
      if (this.hasParent()) {
         UIComponent.super.drawTooltip(context, mouseX, mouseY, partialTicks, delta);
      } else {
         ArrayList<UIComponent> hoveredDescendants = new ArrayList<>();
         Objects.requireNonNull(hoveredDescendants);
         this.forEachDescendantWhere(hoveredDescendants::add, (component) -> component.isInBoundingBox((double)mouseX, (double)mouseY));
         hoveredDescendants.remove(this);

         for(int i = hoveredDescendants.size() - 1; i >= 0; --i) {
            ParentUIComponent nextParent = null;

            for(int parentIdx = i - 1; parentIdx >= 0; --parentIdx) {
               Object var11 = hoveredDescendants.get(parentIdx);
               if (var11 instanceof ParentUIComponent) {
                   nextParent = (ParentUIComponent)var11;
                  break;
               }
            }

            UIComponent current = (UIComponent)hoveredDescendants.get(i);
            if (nextParent != null && current.carpetGUI$parent() != nextParent) {
               break;
            }

            if (current.carpetGUI$shouldDrawTooltip((double)mouseX, (double)mouseY)) {
               context.push();

               while(i >= 0 && (i <= 0 || ((UIComponent)hoveredDescendants.get(i)).carpetGUI$parent() == hoveredDescendants.get(i - 1))) {
                  context.translate(0.0F, 0.0F);
                  --i;
               }

               current.drawTooltip(context, mouseX, mouseY, partialTicks, delta);
               context.pop();
               break;
            }
         }

      }
   }

   default boolean carpetGUI$onMouseDown(MouseButtonEvent click, boolean doubled) {
      ListIterator<UIComponent> iter = this.children().listIterator(this.children().size());

      while(iter.hasPrevious()) {
         UIComponent child = (UIComponent)iter.previous();
         if (child.isInBoundingBox((double)this.carpetGUI$x() + click.x(), (double)this.carpetGUI$y() + click.y()) && child.carpetGUI$onMouseDown(new MouseButtonEvent((double)this.carpetGUI$x() + click.x() - (double)child.carpetGUI$x(), (double)this.carpetGUI$y() + click.y() - (double)child.carpetGUI$y(), click.buttonInfo()), doubled)) {
            return true;
         }
      }

      return false;
   }

   default boolean carpetGUI$onMouseScroll(double mouseX, double mouseY, double amount) {
      ListIterator<UIComponent> iter = this.children().listIterator(this.children().size());

      while(iter.hasPrevious()) {
         UIComponent child = (UIComponent)iter.previous();
         if (child.isInBoundingBox((double)this.carpetGUI$x() + mouseX, (double)this.carpetGUI$y() + mouseY) && child.carpetGUI$onMouseScroll((double)this.carpetGUI$x() + mouseX - (double)child.carpetGUI$x(), (double)this.carpetGUI$y() + mouseY - (double)child.carpetGUI$y(), amount)) {
            return true;
         }
      }

      return false;
   }

   default void carpetGUI$update(float delta, int mouseX, int mouseY) {
      for(int i = 0; i < this.children().size(); ++i) {
         ((UIComponent)this.children().get(i)).carpetGUI$update(delta, mouseX, mouseY);
      }

   }

   default MutableComponent inspectorDescriptor() {
      Insets padding = (Insets)this.padding().get();
      MutableComponent var10000 = UIComponent.super.inspectorDescriptor();
      int var10001 = padding.top();
      return var10000.append(new net.minecraft.network.chat.TextComponent(" >" + var10001 + "," + padding.bottom() + "," + padding.left() + "," + padding.right() + "<").setStyle(Style.EMPTY.withColor(ChatFormatting.AQUA)));
   }

   default <T extends UIComponent> T childById(@NotNull Class<T> expectedClass, @NotNull String id) {
      ListIterator<UIComponent> iter = this.children().listIterator(this.children().size());

      while(iter.hasPrevious()) {
         UIComponent child = (UIComponent)iter.previous();
         if (Objects.equals(child.carpetGUI$id(), id)) {
            if (!expectedClass.isAssignableFrom(child.getClass())) {
               throw new IllegalStateException("Expected child with id '" + id + "' to be a " + expectedClass.getSimpleName() + " but it is a " + child.getClass().getSimpleName());
            }

            return (T)child;
         }

         if (child instanceof ParentUIComponent parent) {
            T candidate = parent.childById(expectedClass, id);
            if (candidate != null) {
               return candidate;
            }
         }
      }

      return null;
   }

   default @Nullable UIComponent childAt(int x, int y) {
      ListIterator<UIComponent> iter = this.children().listIterator(this.children().size());

      while(iter.hasPrevious()) {
         UIComponent child = (UIComponent)iter.previous();
         if (child.isInBoundingBox((double)x, (double)y)) {
            if (child instanceof ParentUIComponent parent) {
                return parent.childAt(x, y);
            }

            return child;
         }
      }

      return this.isInBoundingBox((double)x, (double)y) ? this : null;
   }

   default void collectDescendants(ArrayList<UIComponent> into) {
      Objects.requireNonNull(into);
      this.forEachDescendant(into::add);
   }

   default void forEachDescendant(Consumer<UIComponent> action) {
      action.accept(this);

      for(UIComponent child : this.children()) {
         if (child instanceof ParentUIComponent parent) {
            parent.forEachDescendant(action);
         } else {
            action.accept(child);
         }
      }

   }

   default void forEachDescendantWhere(Consumer<UIComponent> action, Predicate<UIComponent> condition) {
      action.accept(this);

      for(UIComponent child : this.children()) {
         if (condition.test(child)) {
            if (child instanceof ParentUIComponent parent) {
                parent.forEachDescendantWhere(action, condition);
            } else {
               action.accept(child);
            }
         }
      }

   }
}
