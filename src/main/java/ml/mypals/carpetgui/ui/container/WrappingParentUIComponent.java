package ml.mypals.carpetgui.ui.container;

import java.util.Collections;
import java.util.List;
import ml.mypals.carpetgui.ui.base.BaseParentUIComponent;
import ml.mypals.carpetgui.ui.core.Insets;
import ml.mypals.carpetgui.ui.core.ParentUIComponent;
import ml.mypals.carpetgui.ui.core.Size;
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.core.UIComponent;

public abstract class WrappingParentUIComponent<C extends UIComponent> extends BaseParentUIComponent {
   protected C child;
   protected List<UIComponent> childView;

   protected WrappingParentUIComponent(Sizing horizontalSizing, Sizing verticalSizing, C child) {
      super(horizontalSizing, verticalSizing);
      this.child = child;
      this.childView = Collections.singletonList(this.child);
   }

   protected int determineHorizontalContentSize(Sizing sizing) {
      return this.child.fullSize().width() + ((Insets)this.padding.get()).horizontal();
   }

   protected int determineVerticalContentSize(Sizing sizing) {
      return this.child.fullSize().height() + ((Insets)this.padding.get()).vertical();
   }

   public void layout(Size space) {
      this.child.inflate(this.calculateChildSpace(space));
      this.child.mount(this, this.childMountX(), this.childMountY());
   }

   protected int childMountX() {
      return this.x + ((Insets)this.child.margins().get()).left() + ((Insets)this.padding.get()).left();
   }

   protected int childMountY() {
      return this.y + ((Insets)this.child.margins().get()).top() + ((Insets)this.padding.get()).top();
   }

   public WrappingParentUIComponent<C> child(C newChild) {
      if (this.child != null) {
         this.child.dismount(UIComponent.DismountReason.REMOVED);
      }

      this.child = newChild;
      this.childView = Collections.singletonList(this.child);
      this.updateLayout();
      return this;
   }

   public C child() {
      return this.child;
   }

   public List<UIComponent> children() {
      return this.childView;
   }

   public ParentUIComponent removeChild(UIComponent child) {
      throw new UnsupportedOperationException("Cannot remove the child of a wrapping component");
   }
}
