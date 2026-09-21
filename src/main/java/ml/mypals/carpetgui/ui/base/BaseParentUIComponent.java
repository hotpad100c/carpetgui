package ml.mypals.carpetgui.ui.base;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import ml.mypals.carpetgui.ui.core.HorizontalAlignment;
import ml.mypals.carpetgui.ui.core.Insets;
import ml.mypals.carpetgui.ui.core.OwoUIGraphics;
import ml.mypals.carpetgui.ui.core.ParentUIComponent;
import ml.mypals.carpetgui.ui.core.Positioning;
import ml.mypals.carpetgui.ui.core.Size;
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.core.Surface;
import ml.mypals.carpetgui.ui.core.UIComponent;
import ml.mypals.carpetgui.ui.core.VerticalAlignment;
import ml.mypals.carpetgui.ui.util.FocusHandler;
import ml.mypals.carpetgui.ui.util.Observable;
//? if >=1.21.9 {
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//?} else {
/*import ml.mypals.carpetgui.compat.input.CharacterEvent;
import ml.mypals.carpetgui.compat.input.KeyEvent;
import ml.mypals.carpetgui.compat.input.MouseButtonEvent;
*///?}
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

public abstract class BaseParentUIComponent extends BaseUIComponent implements ParentUIComponent {
   protected final Observable<VerticalAlignment> verticalAlignment;
   protected final Observable<HorizontalAlignment> horizontalAlignment;
   protected final Observable<Insets> padding;
   protected @Nullable FocusHandler focusHandler;
   protected @Nullable ArrayList<Runnable> taskQueue;
   protected Surface surface;
   protected boolean allowOverflow;

   protected BaseParentUIComponent(Sizing horizontalSizing, Sizing verticalSizing) {
      this.verticalAlignment = Observable.<VerticalAlignment>of(VerticalAlignment.TOP);
      this.horizontalAlignment = Observable.<HorizontalAlignment>of(HorizontalAlignment.LEFT);
      this.padding = Observable.<Insets>of(Insets.none());
      this.focusHandler = null;
      this.taskQueue = null;
      this.surface = Surface.BLANK;
      this.allowOverflow = false;
      this.horizontalSizing.set(horizontalSizing);
      this.verticalSizing.set(verticalSizing);
      Observable.observeAll(this::updateLayout, this.horizontalAlignment, this.verticalAlignment, this.padding);
   }

   public final void carpetGUI$update(float delta, int mouseX, int mouseY) {
      ParentUIComponent.super.carpetGUI$update(delta, mouseX, mouseY);
      super.carpetGUI$update(delta, mouseX, mouseY);
      this.parentUpdate(delta, mouseX, mouseY);
      if (this.taskQueue != null) {
         this.taskQueue.forEach(Runnable::run);
         this.taskQueue.clear();
      }

   }

   protected void parentUpdate(float delta, int mouseX, int mouseY) {
   }

   public void carpetGUI$draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
      this.surface.draw(graphics, this);
   }

   public void queue(Runnable task) {
      if (this.taskQueue == null) {
         this.parent.queue(task);
      } else {
         this.taskQueue.add(task);
      }

   }

   public @Nullable FocusHandler carpetGUI$focusHandler() {
      return this.focusHandler == null ? super.carpetGUI$focusHandler() : this.focusHandler;
   }

   public ParentUIComponent verticalAlignment(VerticalAlignment alignment) {
      this.verticalAlignment.set(alignment);
      return this;
   }

   public VerticalAlignment verticalAlignment() {
      return this.verticalAlignment.get();
   }

   public ParentUIComponent horizontalAlignment(HorizontalAlignment alignment) {
      this.horizontalAlignment.set(alignment);
      return this;
   }

   public HorizontalAlignment horizontalAlignment() {
      return this.horizontalAlignment.get();
   }

   public ParentUIComponent padding(Insets padding) {
      this.padding.set(padding);
      this.updateLayout();
      return this;
   }

   public Observable<Insets> padding() {
      return this.padding;
   }

   public ParentUIComponent allowOverflow(boolean allowOverflow) {
      this.allowOverflow = allowOverflow;
      return this;
   }

   public boolean allowOverflow() {
      return this.allowOverflow;
   }

   public ParentUIComponent surface(Surface surface) {
      this.surface = surface;
      return this;
   }

   public Surface surface() {
      return this.surface;
   }

   public void carpetGUI$mount(ParentUIComponent parent, int x, int y) {
      super.carpetGUI$mount(parent, x, y);
      if (parent == null && this.focusHandler == null) {
         this.focusHandler = new FocusHandler(this);
         this.taskQueue = new ArrayList();
      }

   }

   public void carpetGUI$inflate(Size space) {
      if (!this.space.equals(space) || this.dirty) {
         this.space = space;

         for(UIComponent child : this.children()) {
            child.carpetGUI$dismount(UIComponent.DismountReason.LAYOUT_INFLATION);
         }

         super.carpetGUI$inflate(space);
         this.layout(space);
         super.carpetGUI$inflate(space);
      }
   }

   protected void updateLayout() {
      if (this.mounted) {
         if (this.batchedEvents > 0) {
            ++this.batchedEvents;
         } else {
            Size previousSize = this.fullSize();
            this.dirty = true;
            this.carpetGUI$inflate(this.space);
            if (!previousSize.equals(this.fullSize()) && this.parent != null) {
               this.parent.onChildMutated(this);
            }

         }
      }
   }

   protected void runAndDeferEvents(Runnable action) {
      try {
         this.batchedEvents = 1;
         action.run();
      } finally {
         if (this.batchedEvents > 1) {
            this.batchedEvents = 0;
            this.updateLayout();
         } else {
            this.batchedEvents = 0;
         }

      }

   }

   public void onChildMutated(UIComponent child) {
      this.updateLayout();
   }

   public boolean carpetGUI$onMouseDown(MouseButtonEvent click, boolean doubled) {
      if (this.focusHandler != null) {
         this.focusHandler.updateClickFocus((double)this.x + click.x(), (double)this.y + click.y());
      }

      return ParentUIComponent.super.carpetGUI$onMouseDown(click, doubled) || super.carpetGUI$onMouseDown(click, doubled);
   }

   public boolean carpetGUI$onMouseUp(MouseButtonEvent click) {
      if (this.focusHandler != null && this.focusHandler.focused() != null) {
         UIComponent focused = this.focusHandler.focused();
         return focused.carpetGUI$onMouseUp(new MouseButtonEvent((double)this.x + click.x() - (double)focused.carpetGUI$x(), (double)this.y + click.y() - (double)focused.carpetGUI$y(), click.buttonInfo()));
      } else {
         return super.carpetGUI$onMouseUp(click);
      }
   }

   public boolean carpetGUI$onMouseScroll(double mouseX, double mouseY, double amount) {
      return ParentUIComponent.super.carpetGUI$onMouseScroll(mouseX, mouseY, amount) || super.carpetGUI$onMouseScroll(mouseX, mouseY, amount);
   }

   public boolean carpetGUI$onMouseDrag(MouseButtonEvent click, double deltaX, double deltaY) {
      if (this.focusHandler != null && this.focusHandler.focused() != null) {
         UIComponent focused = this.focusHandler.focused();
         return focused.carpetGUI$onMouseDrag(new MouseButtonEvent((double)this.x + click.x() - (double)focused.carpetGUI$x(), (double)this.y + click.y() - (double)focused.carpetGUI$y(), click.buttonInfo()), deltaX, deltaY);
      } else {
         return super.carpetGUI$onMouseDrag(click, deltaX, deltaY);
      }
   }

   public boolean carpetGUI$onKeyPress(KeyEvent input) {
      if (this.focusHandler == null) {
         return false;
      } else {
         if (input.isCycleFocus()) {
            this.focusHandler.cycle(!input.hasShiftDown());
         } else if ((input.isUp() || input.isDown() || input.isLeft() || input.isRight()) && input.hasAltDown()) {
            this.focusHandler.moveFocus(input.key());
         } else if (this.focusHandler.focused() != null) {
            return this.focusHandler.focused().carpetGUI$onKeyPress(input);
         }

         return super.carpetGUI$onKeyPress(input);
      }
   }

   public boolean carpetGUI$onCharTyped(CharacterEvent input) {
      if (this.focusHandler == null) {
         return false;
      } else {
         return this.focusHandler.focused() != null ? this.focusHandler.focused().carpetGUI$onCharTyped(input) : super.carpetGUI$onCharTyped(input);
      }
   }

   public void carpetGUI$updateX(int x) {
      int offset = x - this.x;
      super.carpetGUI$updateX(x);

      for(UIComponent child : this.children()) {
         child.carpetGUI$updateX(child.carpetGUI$baseX() + offset);
      }

   }

   public void carpetGUI$updateY(int y) {
      int offset = y - this.y;
      super.carpetGUI$updateY(y);

      for(UIComponent child : this.children()) {
         child.carpetGUI$updateY(child.carpetGUI$baseY() + offset);
      }

   }

   protected Size childMountingOffset() {
      Insets padding = (Insets)this.padding.get();
      return Size.of(padding.left(), padding.top());
   }

   protected void mountChild(@Nullable UIComponent child, Consumer<UIComponent> layoutFunc) {
      if (child != null) {
         Positioning positioning = (Positioning)child.carpetGUI$positioning().get();
         Insets componentMargins = (Insets)child.carpetGUI$margins().get();
         Insets padding = (Insets)this.padding.get();
         switch (positioning.type) {
            case LAYOUT -> layoutFunc.accept(child);
            case ABSOLUTE -> child.carpetGUI$mount(this, this.x + positioning.x + componentMargins.left() + padding.left(), this.y + positioning.y + componentMargins.top() + padding.top());
            case RELATIVE -> child.carpetGUI$mount(this, this.x + padding.left() + componentMargins.left() + Math.round((float)positioning.x / 100.0F * (float)(this.carpetGUI$width() - child.carpetGUI$fullSize().width() - padding.horizontal())), this.y + padding.top() + componentMargins.top() + Math.round((float)positioning.y / 100.0F * (float)(this.carpetGUI$height() - child.carpetGUI$fullSize().height() - padding.vertical())));
         }

      }
   }

   protected void drawChildren(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta, List<? extends UIComponent> children) {
      if (!this.allowOverflow) {
         Insets padding = (Insets)this.padding.get();
         context.enableScissor(this.x + padding.left(), this.y + padding.top(), this.x + padding.left() + this.width - padding.horizontal(), this.y + padding.top() + this.height - padding.vertical());
      }

      FocusHandler focusHandler = this.carpetGUI$focusHandler();

      for(int i = 0; i < children.size(); ++i) {
         UIComponent child = (UIComponent)children.get(i);
         if (context.intersectsScissor(child)) {
            child.carpetGUI$draw(context, mouseX, mouseY, partialTicks, delta);
            if (focusHandler.lastFocusSource() == UIComponent.FocusSource.KEYBOARD_CYCLE && focusHandler.focused() == child) {
               child.drawFocusHighlight(context, mouseX, mouseY, partialTicks, delta);
            }
         }
      }

      if (!this.allowOverflow) {
         context.disableScissor();
      }

   }

   private static int lerpInt(float delta, int start, int end) {
      return (int) (start + delta * (end - start));
   }

   protected Size calculateChildSpace(Size thisSpace) {
      Insets padding = (Insets)this.padding.get();
      return Size.of(lerpInt(((Sizing)this.horizontalSizing.get()).contentFactor(), this.width - padding.horizontal(), thisSpace.width() - padding.horizontal()), lerpInt(((Sizing)this.verticalSizing.get()).contentFactor(), this.height - padding.vertical(), thisSpace.height() - padding.vertical()));
   }

   public BaseParentUIComponent carpetGUI$positioning(Positioning positioning) {
      return (BaseParentUIComponent)super.carpetGUI$positioning(positioning);
   }

   public BaseParentUIComponent carpetGUI$margins(Insets margins) {
      return (BaseParentUIComponent)super.carpetGUI$margins(margins);
   }
}
