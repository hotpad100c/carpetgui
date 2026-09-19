package ml.mypals.carpetgui.ui.container;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import ml.mypals.carpetgui.ui.base.BaseParentUIComponent;
import ml.mypals.carpetgui.ui.core.HorizontalAlignment;
import ml.mypals.carpetgui.ui.core.Insets;
import ml.mypals.carpetgui.ui.core.OwoUIGraphics;
import ml.mypals.carpetgui.ui.core.Size;
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.core.UIComponent;
import ml.mypals.carpetgui.ui.core.VerticalAlignment;
import ml.mypals.carpetgui.ui.util.Observable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.apache.commons.lang3.mutable.MutableInt;

public class FlowLayout extends BaseParentUIComponent {
   protected final List<UIComponent> children = new ArrayList();
   protected final List<UIComponent> childrenView;
   protected final Algorithm algorithm;
   protected Size contentSize;
   protected Observable<Integer> gap;

   protected FlowLayout(Sizing horizontalSizing, Sizing verticalSizing, Algorithm algorithm) {
      super(horizontalSizing, verticalSizing);
      this.childrenView = Collections.unmodifiableList(this.children);
      this.contentSize = Size.zero();
      this.gap = Observable.<Integer>of(0);
      this.algorithm = algorithm;
      this.gap.observe((integer) -> this.updateLayout());
   }

   protected int determineHorizontalContentSize(Sizing sizing) {
      return this.contentSize.width() + ((Insets)this.padding.get()).horizontal();
   }

   protected int determineVerticalContentSize(Sizing sizing) {
      return this.contentSize.height() + ((Insets)this.padding.get()).vertical();
   }

   public void layout(Size space) {
      this.algorithm.layout(this);
   }

   public FlowLayout child(UIComponent child) {
      this.children.add(child);
      this.updateLayout();
      return this;
   }

   public FlowLayout children(Collection<? extends UIComponent> children) {
      this.children.addAll(children);
      this.updateLayout();
      return this;
   }

   public FlowLayout child(int index, UIComponent child) {
      this.children.add(index, child);
      this.updateLayout();
      return this;
   }

   public FlowLayout children(int index, Collection<? extends UIComponent> children) {
      this.children.addAll(index, children);
      this.updateLayout();
      return this;
   }

   public FlowLayout removeChild(UIComponent child) {
      if (this.children.remove(child)) {
         child.dismount(UIComponent.DismountReason.REMOVED);
         this.updateLayout();
      }

      return this;
   }

   public FlowLayout clearChildren() {
      for(UIComponent child : this.children) {
         child.dismount(UIComponent.DismountReason.REMOVED);
      }

      this.children.clear();
      this.updateLayout();
      return this;
   }

   public List<UIComponent> children() {
      return this.childrenView;
   }

   public FlowLayout gap(int gap) {
      this.gap.set(gap);
      return this;
   }

   public int gap() {
      return (Integer)this.gap.get();
   }

   public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
      super.draw(graphics, mouseX, mouseY, partialTicks, delta);
      this.drawChildren(graphics, mouseX, mouseY, partialTicks, delta, this.children);
   }

   public MutableComponent inspectorDescriptor() {
      MutableComponent descriptor = super.inspectorDescriptor();
      return this.gap() == 0 ? descriptor : descriptor.append(Component.literal(" [" + this.gap() + "]"));
   }

   @FunctionalInterface
   public interface Algorithm {
      Algorithm HORIZONTAL = (container) -> {
         MutableInt layoutWidth = new MutableInt(0);
         MutableInt layoutHeight = new MutableInt(0);
         ArrayList<UIComponent> layout = new ArrayList();
         Insets padding = (Insets)container.padding.get();
         Size childSpace = container.calculateChildSpace(container.space);
         MountingHelper.inflateWithExpand(container.children, childSpace, false, container.gap());
         Objects.requireNonNull(container);
         MountingHelper mountState = MountingHelper.mountEarly((x$0, x$1) -> container.mountChild(x$0, x$1), container.children, (child) -> {
            layout.add(child);
            child.mount(container, container.x + padding.left() + ((Insets)child.margins().get()).left() + layoutWidth.intValue(), container.y + padding.top() + ((Insets)child.margins().get()).top());
            Size childSize = child.fullSize();
            layoutWidth.add(childSize.width() + container.gap());
            if (childSize.height() > layoutHeight.intValue()) {
               layoutHeight.setValue(childSize.height());
            }

         });
         layoutWidth.subtract(container.gap());
         container.contentSize = Size.of(layoutWidth.intValue(), layoutHeight.intValue());
         container.applySizing();
         if (container.verticalAlignment() != VerticalAlignment.TOP) {
            for(UIComponent component : layout) {
               component.updateY(component.baseY() + container.verticalAlignment().align(component.fullSize().height(), container.height - padding.vertical()));
            }
         }

         if (container.horizontalAlignment() != HorizontalAlignment.LEFT) {
            for(UIComponent component : layout) {
               if (container.horizontalAlignment() == HorizontalAlignment.CENTER) {
                  component.updateX(component.baseX() + (container.width - padding.horizontal() - layoutWidth.intValue()) / 2);
               } else {
                  component.updateX(component.baseX() + (container.width - padding.horizontal() - layoutWidth.intValue()));
               }
            }
         }

         mountState.mountLate();
      };
      Algorithm VERTICAL = (container) -> {
         MutableInt layoutHeight = new MutableInt(0);
         MutableInt layoutWidth = new MutableInt(0);
         ArrayList<UIComponent> layout = new ArrayList();
         Insets padding = (Insets)container.padding.get();
         Size childSpace = container.calculateChildSpace(container.space);
         MountingHelper.inflateWithExpand(container.children, childSpace, true, container.gap());
         Objects.requireNonNull(container);
         MountingHelper mountState = MountingHelper.mountEarly((x$0, x$1) -> container.mountChild(x$0, x$1), container.children, (child) -> {
            layout.add(child);
            child.mount(container, container.x + padding.left() + ((Insets)child.margins().get()).left(), container.y + padding.top() + ((Insets)child.margins().get()).top() + layoutHeight.intValue());
            Size childSize = child.fullSize();
            layoutHeight.add(childSize.height() + container.gap());
            if (childSize.width() > layoutWidth.intValue()) {
               layoutWidth.setValue(childSize.width());
            }

         });
         layoutHeight.subtract(container.gap());
         container.contentSize = Size.of(layoutWidth.intValue(), layoutHeight.intValue());
         container.applySizing();
         if (container.horizontalAlignment() != HorizontalAlignment.LEFT) {
            for(UIComponent component : layout) {
               component.updateX(component.baseX() + container.horizontalAlignment().align(component.fullSize().width(), container.width - padding.horizontal()));
            }
         }

         if (container.verticalAlignment() != VerticalAlignment.TOP) {
            for(UIComponent component : layout) {
               if (container.verticalAlignment() == VerticalAlignment.CENTER) {
                  component.updateY(component.baseY() + (container.height - padding.vertical() - layoutHeight.intValue()) / 2);
               } else {
                  component.updateY(component.baseY() + (container.height - padding.vertical() - layoutHeight.intValue()));
               }
            }
         }

         mountState.mountLate();
      };
      Algorithm LTR_TEXT = (container) -> {
         if (((Sizing)container.horizontalSizing.get()).isContent()) {
            throw new IllegalStateException("An LTR-text-flow layout must use content-independent horizontal sizing");
         } else {
            MutableInt layoutWidth = new MutableInt(0);
            MutableInt layoutHeight = new MutableInt(0);
            MutableInt rowWidth = new MutableInt(0);
            MutableInt rowOffset = new MutableInt(0);
            ArrayList<UIComponent> layout = new ArrayList();
            Insets padding = (Insets)container.padding.get();
            Size childSpace = container.calculateChildSpace(container.space);
            container.children.forEach((child) -> child.inflate(childSpace));
            Objects.requireNonNull(container);
            MountingHelper mountState = MountingHelper.mountEarly((x$0, x$1) -> container.mountChild(x$0, x$1), container.children, (child) -> {
               layout.add(child);
               int x = container.x + padding.left() + ((Insets)child.margins().get()).left() + rowWidth.intValue();
               int y = container.y + padding.top() + ((Insets)child.margins().get()).top() + rowOffset.intValue();
               Size childSize = child.fullSize();
               if (rowWidth.intValue() + childSize.width() > childSpace.width()) {
                  x -= rowWidth.intValue();
                  y = y - rowOffset.intValue() + layoutHeight.intValue();
                  rowOffset.setValue(layoutHeight);
                  rowWidth.setValue(0);
               }

               child.mount(container, x, y);
               rowWidth.add(childSize.width() + container.gap());
               if (rowOffset.intValue() + childSize.height() > layoutHeight.intValue()) {
                  layoutHeight.setValue(rowOffset.intValue() + childSize.height());
               }

               if (rowWidth.intValue() > layoutWidth.intValue()) {
                  layoutWidth.setValue(rowWidth.intValue());
               }

            });
            layoutWidth.subtract(container.gap());
            container.contentSize = Size.of(layoutWidth.intValue(), layoutHeight.intValue());
            container.applySizing();
            if (container.verticalAlignment() != VerticalAlignment.TOP) {
               for(UIComponent component : layout) {
                  component.updateY(component.baseY() + container.verticalAlignment().align(layoutHeight.intValue(), container.height - padding.vertical()));
               }
            }

            if (container.horizontalAlignment() != HorizontalAlignment.LEFT) {
               for(UIComponent component : layout) {
                  if (container.horizontalAlignment() == HorizontalAlignment.CENTER) {
                     component.updateX(component.baseX() + (container.width - padding.horizontal() - layoutWidth.intValue()) / 2);
                  } else {
                     component.updateX(component.baseX() + (container.width - padding.horizontal() - layoutWidth.intValue()));
                  }
               }
            }

            mountState.mountLate();
         }
      };

      void layout(FlowLayout var1);
   }

   public static class MountingHelper {
      protected final ComponentSink sink;
      protected final List<UIComponent> lateChildren;

      protected MountingHelper(ComponentSink sink, List<UIComponent> children) {
         this.sink = sink;
         this.lateChildren = children;
      }

      public static void inflateWithExpand(List<UIComponent> children, Size childSpace, boolean vertical, int gap) {
         ArrayList<UIComponent> nonExpandChildren = new ArrayList<>();
         children.forEach((child) -> {
            if (!((Sizing)child.verticalSizing().get()).isExpand() && !((Sizing)child.horizontalSizing().get()).isExpand()) {
               if (((ml.mypals.carpetgui.ui.core.Positioning)child.positioning().get()).type == ml.mypals.carpetgui.ui.core.Positioning.Type.LAYOUT) {
                  nonExpandChildren.add(child);
               }
               child.inflate(childSpace);
            }
         });
         Size remainingSpace;
         if (vertical) {
            int height = childSpace.height();
            for(UIComponent nonExpandChild : nonExpandChildren) {
               height -= nonExpandChild.fullSize().height();
            }
            height -= gap * Math.max(children.size() - 1, 0);
            remainingSpace = Size.of(childSpace.width(), Math.max(0, height));
         } else {
            int width = childSpace.width();
            for(UIComponent nonExpandChild : nonExpandChildren) {
               width -= nonExpandChild.fullSize().width();
            }
            width -= gap * Math.max(children.size() - 1, 0);
            remainingSpace = Size.of(Math.max(0, width), childSpace.height());
         }

         children.forEach((child) -> {
            if (((Sizing)child.verticalSizing().get()).isExpand() || ((Sizing)child.horizontalSizing().get()).isExpand()) {
               child.inflate(remainingSpace);
            }
         });
      }

      public static MountingHelper mountEarly(ComponentSink sink, List<UIComponent> children, java.util.function.Consumer<UIComponent> layoutFunc) {
         ArrayList<UIComponent> lateChildren = new ArrayList<>();
         for(UIComponent child : children) {
            if (!((ml.mypals.carpetgui.ui.core.Positioning)child.positioning().get()).isRelative()) {
               sink.accept(child, layoutFunc);
            } else {
               lateChildren.add(child);
            }
         }
         return new MountingHelper(sink, lateChildren);
      }

      public void mountLate() {
         for(UIComponent child : this.lateChildren) {
            this.sink.accept(child, (component) -> {
               throw new IllegalStateException("A layout-positioned child was mounted late");
            });
         }
         this.lateChildren.clear();
      }

      @FunctionalInterface
      public interface ComponentSink {
         void accept(UIComponent child, java.util.function.Consumer<UIComponent> layoutFunc);
      }
   }
}
