package ml.mypals.carpetgui.ui.base;

import java.util.List;
import java.util.function.Consumer;
import ml.mypals.carpetgui.ui.core.AnimatableProperty;
import ml.mypals.carpetgui.ui.core.CursorStyle;
import ml.mypals.carpetgui.ui.core.Insets;
import ml.mypals.carpetgui.ui.core.ParentUIComponent;
import ml.mypals.carpetgui.ui.core.Positioning;
import ml.mypals.carpetgui.ui.core.Size;
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.core.UIComponent;
import ml.mypals.carpetgui.ui.event.UIEvents.*;
import ml.mypals.carpetgui.ui.util.EventSource;
import ml.mypals.carpetgui.ui.util.EventStream;
import ml.mypals.carpetgui.ui.util.FocusHandler;
import ml.mypals.carpetgui.ui.util.Observable;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.Nullable;

public abstract class BaseUIComponent implements UIComponent {
   protected @Nullable ParentUIComponent parent = null;
   protected @Nullable String id = null;
   protected boolean mounted = false;
   protected int batchedEvents = 0;
   protected final AnimatableProperty<Insets> margins = AnimatableProperty.<Insets>of(Insets.none());
   protected final AnimatableProperty<Positioning> positioning = AnimatableProperty.<Positioning>of(Positioning.layout());
   protected final AnimatableProperty<Sizing> horizontalSizing = AnimatableProperty.<Sizing>of(Sizing.content());
   protected final AnimatableProperty<Sizing> verticalSizing = AnimatableProperty.<Sizing>of(Sizing.content());
   protected final EventStream<MouseDown> mouseDownEvents = MouseDown.newStream();
   protected final EventStream<MouseUp> mouseUpEvents = MouseUp.newStream();
   protected final EventStream<MouseScroll> mouseScrollEvents = MouseScroll.newStream();
   protected final EventStream<MouseDrag> mouseDragEvents = MouseDrag.newStream();
   protected final EventStream<KeyPress> keyPressEvents = KeyPress.newStream();
   protected final EventStream<CharTyped> charTypedEvents = CharTyped.newStream();
   protected final EventStream<FocusGained> focusGainedEvents = FocusGained.newStream();
   protected final EventStream<FocusLost> focusLostEvents = FocusLost.newStream();
   protected final EventStream<MouseEnter> mouseEnterEvents = MouseEnter.newStream();
   protected final EventStream<MouseLeave> mouseLeaveEvents = MouseLeave.newStream();
   protected boolean hovered = false;
   protected boolean dirty = false;
   protected CursorStyle cursorStyle;
   protected List<ClientTooltipComponent> tooltip;
   protected int x;
   protected int y;
   protected int width;
   protected int height;
   protected Size space;

   protected BaseUIComponent() {
      this.cursorStyle = CursorStyle.NONE;
      this.tooltip = List.of();
      this.space = Size.zero();
      Observable.observeAll(this::notifyParentIfMounted, this.margins, this.positioning, this.horizontalSizing, this.verticalSizing);
   }

   protected int determineHorizontalContentSize(Sizing sizing) {
      throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not support Sizing.content() on the horizontal axis");
   }

   protected int determineVerticalContentSize(Sizing sizing) {
      throw new UnsupportedOperationException(this.getClass().getSimpleName() + " does not support Sizing.content() on the vertical axis");
   }

   public void inflate(Size space) {
      this.space = space;
      this.applySizing();
      this.dirty = false;
   }

   protected void applySizing() {
      Sizing horizontalSizing = (Sizing)this.horizontalSizing.get();
      Sizing verticalSizing = (Sizing)this.verticalSizing.get();
      Insets margins = (Insets)this.margins.get();
      this.width = horizontalSizing.inflate(this.space.width() - margins.horizontal(), this::determineHorizontalContentSize);
      this.height = verticalSizing.inflate(this.space.height() - margins.vertical(), this::determineVerticalContentSize);
   }

   protected void notifyParentIfMounted() {
      if (this.hasParent()) {
         if (this.batchedEvents > 0) {
            ++this.batchedEvents;
         } else {
            this.dirty = true;
            this.parent.onChildMutated(this);
         }
      }
   }

   public <C extends UIComponent> C configure(Consumer<C> closure) {
      try {
         this.runAndDeferEvents(() -> closure.accept((C) this));
         return (C)this;
      } catch (ClassCastException theUserDidBadItWasNotMyFault) {
         throw new IllegalArgumentException("Invalid target class passed when configuring component of type " + this.getClass().getSimpleName(), theUserDidBadItWasNotMyFault);
      }
   }

   protected void runAndDeferEvents(Runnable action) {
      try {
         this.batchedEvents = 1;
         action.run();
      } finally {
         if (this.batchedEvents > 1) {
            this.batchedEvents = 0;
            this.notifyParentIfMounted();
         } else {
            this.batchedEvents = 0;
         }

      }

   }

   public void update(float delta, int mouseX, int mouseY) {
      UIComponent.super.update(delta, mouseX, mouseY);
      boolean nowHovered = this.isInBoundingBox((double)mouseX, (double)mouseY);
      if (this.hovered != nowHovered) {
         this.updateHoveredState(mouseX, mouseY, nowHovered);
      }

   }

   protected void updateHoveredState(int mouseX, int mouseY, boolean nowHovered) {
      this.hovered = nowHovered;
      if (nowHovered) {
         if (this.root() == null || this.root().childAt(mouseX, mouseY) != this) {
            this.hovered = false;
            return;
         }

         ((MouseEnter)this.mouseEnterEvents.sink()).onMouseEnter();
      } else {
         ((MouseLeave)this.mouseLeaveEvents.sink()).onMouseLeave();
      }

   }

   public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
      return ((MouseDown)this.mouseDownEvents.sink()).onMouseDown(click, doubled);
   }

   public EventSource<MouseDown> mouseDown() {
      return this.mouseDownEvents.source();
   }

   public boolean onMouseUp(MouseButtonEvent click) {
      return ((MouseUp)this.mouseUpEvents.sink()).onMouseUp(click);
   }

   public EventSource<MouseUp> mouseUp() {
      return this.mouseUpEvents.source();
   }

   public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
      return ((MouseScroll)this.mouseScrollEvents.sink()).onMouseScroll(mouseX, mouseY, amount);
   }

   public EventSource<MouseScroll> mouseScroll() {
      return this.mouseScrollEvents.source();
   }

   public boolean onMouseDrag(MouseButtonEvent click, double deltaX, double deltaY) {
      return ((MouseDrag)this.mouseDragEvents.sink()).onMouseDrag(click, deltaX, deltaY);
   }

   public EventSource<MouseDrag> mouseDrag() {
      return this.mouseDragEvents.source();
   }

   public boolean onKeyPress(KeyEvent input) {
      return ((KeyPress)this.keyPressEvents.sink()).onKeyPress(input);
   }

   public EventSource<KeyPress> keyPress() {
      return this.keyPressEvents.source();
   }

   public boolean onCharTyped(CharacterEvent input) {
      return ((CharTyped)this.charTypedEvents.sink()).onCharTyped(input);
   }

   public EventSource<CharTyped> charTyped() {
      return this.charTypedEvents.source();
   }

   public void onFocusGained(UIComponent.FocusSource source) {
      ((FocusGained)this.focusGainedEvents.sink()).onFocusGained(source);
   }

   public EventSource<FocusGained> focusGained() {
      return this.focusGainedEvents.source();
   }

   public void onFocusLost() {
      ((FocusLost)this.focusLostEvents.sink()).onFocusLost();
   }

   public EventSource<FocusLost> focusLost() {
      return this.focusLostEvents.source();
   }

   public EventSource<MouseEnter> mouseEnter() {
      return this.mouseEnterEvents.source();
   }

   public EventSource<MouseLeave> mouseLeave() {
      return this.mouseLeaveEvents.source();
   }

   public CursorStyle cursorStyle() {
      return this.cursorStyle;
   }

   public BaseUIComponent cursorStyle(CursorStyle style) {
      this.cursorStyle = style;
      return this;
   }

   public UIComponent tooltip(List<ClientTooltipComponent> tooltip) {
      this.tooltip = tooltip;
      return this;
   }

   public List<ClientTooltipComponent> tooltip() {
      return this.tooltip;
   }

   public void mount(ParentUIComponent parent, int x, int y) {
      this.parent = parent;
      this.mounted = true;
      this.moveTo(x, y);
   }

   public void dismount(UIComponent.DismountReason reason) {
      this.parent = null;
      this.mounted = false;
   }

   public ParentUIComponent parent() {
      return this.parent;
   }

   public @Nullable FocusHandler focusHandler() {
      return this.hasParent() ? this.parent.focusHandler() : null;
   }

   public BaseUIComponent positioning(Positioning positioning) {
      this.positioning.set(positioning);
      return this;
   }

   public AnimatableProperty<Positioning> positioning() {
      return this.positioning;
   }

   public BaseUIComponent margins(Insets margins) {
      this.margins.set(margins);
      return this;
   }

   public AnimatableProperty<Insets> margins() {
      return this.margins;
   }

   public UIComponent horizontalSizing(Sizing horizontalSizing) {
      this.horizontalSizing.set(horizontalSizing);
      return this;
   }

   public AnimatableProperty<Sizing> horizontalSizing() {
      return this.horizontalSizing;
   }

   public UIComponent verticalSizing(Sizing verticalSizing) {
      this.verticalSizing.set(verticalSizing);
      return this;
   }

   public AnimatableProperty<Sizing> verticalSizing() {
      return this.verticalSizing;
   }

   public UIComponent id(@Nullable String id) {
      this.id = id;
      return this;
   }

   public @Nullable String id() {
      return this.id;
   }

   public int x() {
      return this.x;
   }

   public void updateX(int x) {
      this.x = x;
   }

   public int y() {
      return this.y;
   }

   public void updateY(int y) {
      this.y = y;
   }

   public int width() {
      return this.width;
   }

   public int height() {
      return this.height;
   }
}
