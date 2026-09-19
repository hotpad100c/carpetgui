package ml.mypals.carpetgui.ui.container;

import java.util.function.BiConsumer;
import java.util.function.Function;
import ml.mypals.carpetgui.ui.UI;
import ml.mypals.carpetgui.ui.core.Color;
import ml.mypals.carpetgui.ui.core.Insets;
import ml.mypals.carpetgui.ui.core.OwoUIGraphics;
import ml.mypals.carpetgui.ui.core.Size;
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.core.UIComponent;
import ml.mypals.carpetgui.ui.util.NinePatchTexture;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class ScrollContainer<C extends UIComponent> extends WrappingParentUIComponent<C> {
   public static final Identifier VERTICAL_VANILLA_SCROLLBAR_TEXTURE = UI.id("scrollbar/vanilla_vertical");
   public static final Identifier DISABLED_VERTICAL_VANILLA_SCROLLBAR_TEXTURE = UI.id("scrollbar/vanilla_vertical_disabled");
   public static final Identifier HORIZONTAL_VANILLA_SCROLLBAR_TEXTURE = UI.id("scrollbar/vanilla_horizontal_disabled");
   public static final Identifier DISABLED_HORIZONTAL_VANILLA_SCROLLBAR_TEXTURE = UI.id("scrollbar/vanilla_horizontal_disabled");
   public static final Identifier VANILLA_SCROLLBAR_TRACK_TEXTURE = UI.id("scrollbar/track");
   public static final Identifier FLAT_VANILLA_SCROLLBAR_TEXTURE = UI.id("scrollbar/vanilla_flat");
   protected double scrollOffset = (double)0.0F;
   protected double currentScrollPosition = (double)0.0F;
   protected int lastScrollPosition = -1;
   protected int scrollStep = 0;
   protected int fixedScrollbarLength = 0;
   protected double lastScrollbarLength = (double)0.0F;
   protected Scrollbar scrollbar = ScrollContainer.Scrollbar.flat(Color.ofArgb(-1610612736));
   protected int scrollbarThiccness = 3;
   protected long lastScrollbarInteractTime = 0L;
   protected int scrollbarOffset = 0;
   protected boolean scrollbaring = false;
   protected int maxScroll = 0;
   protected int childSize = 0;
   protected final ScrollDirection direction;

   public double scrollOffset() {
      return this.scrollOffset;
   }

   public int maxScroll() {
      return this.maxScroll;
   }

   protected ScrollContainer(ScrollDirection direction, Sizing horizontalSizing, Sizing verticalSizing, C child) {
      super(horizontalSizing, verticalSizing, child);
      this.direction = direction;
   }

   protected int determineHorizontalContentSize(Sizing sizing) {
      if (this.direction == ScrollContainer.ScrollDirection.VERTICAL) {
         return super.determineHorizontalContentSize(sizing);
      } else {
         throw new UnsupportedOperationException("Horizontal ScrollContainer cannot be horizontally content-sized");
      }
   }

   protected int determineVerticalContentSize(Sizing sizing) {
      if (this.direction == ScrollContainer.ScrollDirection.HORIZONTAL) {
         return super.determineVerticalContentSize(sizing);
      } else {
         throw new UnsupportedOperationException("Vertical ScrollContainer cannot be vertically content-sized");
      }
   }

   public void layout(Size space) {
      super.layout(space);
      this.maxScroll = Math.max(0, (Integer)this.direction.sizeGetter.apply(this.child) - ((Integer)this.direction.sizeGetter.apply(this) - (Integer)this.direction.insetGetter.apply((Insets)this.padding.get())));
      this.scrollOffset = Mth.clamp(this.scrollOffset, (double)0.0F, (double)this.maxScroll + (double)0.5F);
      this.childSize = (Integer)this.direction.sizeGetter.apply(this.child);
      this.lastScrollPosition = -1;
   }

   protected int childMountX() {
      return (int)((double)super.childMountX() - this.direction.choose(this.currentScrollPosition, (double)0.0F));
   }

   protected int childMountY() {
      return (int)((double)super.childMountY() - this.direction.choose((double)0.0F, this.currentScrollPosition));
   }

   protected void parentUpdate(float delta, int mouseX, int mouseY) {
      super.parentUpdate(delta, mouseX, mouseY);
      this.currentScrollPosition += computeDelta(this.currentScrollPosition, this.scrollOffset, (double)delta * (double)0.5F);
   }

   private static double computeDelta(double current, double target, double delta) {
      double diff = target - current;
      delta = diff * delta;
      return Math.abs(delta) > Math.abs(diff) ? diff : delta;
   }

   public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
      super.draw(graphics, mouseX, mouseY, partialTicks, delta);
      int effectiveScrollOffset = this.scrollStep > 0 ? (int)this.scrollOffset / this.scrollStep * this.scrollStep : (int)this.currentScrollPosition;
      if (this.scrollStep > 0 && (double)this.maxScroll - this.scrollOffset == (double)-1.0F) {
         effectiveScrollOffset = (int)((double)effectiveScrollOffset + this.scrollOffset % (double)this.scrollStep);
      }

      int newScrollPosition = (Integer)this.direction.coordinateGetter.apply(this) - effectiveScrollOffset;
      if (newScrollPosition != this.lastScrollPosition) {
         this.direction.coordinateSetter.accept(this.child, newScrollPosition + (this.direction == ScrollContainer.ScrollDirection.VERTICAL ? ((Insets)this.padding.get()).top() + ((Insets)this.child.margins().get()).top() : ((Insets)this.padding.get()).left() + ((Insets)this.child.margins().get()).left()));
         this.lastScrollPosition = newScrollPosition;
      }

      graphics.pose().pushMatrix();
      double visualOffset = -(this.currentScrollPosition % (double)1.0F);
      if (visualOffset > 0.9999999 || visualOffset < 1.0E-7) {
         visualOffset = (double)0.0F;
      }

      graphics.pose().translate((float)this.direction.choose(visualOffset, (double)0.0F), (float)this.direction.choose((double)0.0F, visualOffset));
      this.drawChildren(graphics, mouseX, mouseY, partialTicks, delta, this.childView);
      graphics.pose().popMatrix();
      if (this.isInScrollbar((double)mouseX, (double)mouseY) || this.scrollbaring) {
         this.lastScrollbarInteractTime = System.currentTimeMillis() + 1500L;
      }

      Insets padding = (Insets)this.padding.get();
      int selfSize = (Integer)this.direction.sizeGetter.apply(this);
      int contentSize = (Integer)this.direction.sizeGetter.apply(this) - (Integer)this.direction.insetGetter.apply(padding);
      this.scrollbarOffset = this.direction == ScrollContainer.ScrollDirection.VERTICAL ? this.x + this.width - padding.right() - this.scrollbarThiccness : this.y + this.height - padding.bottom() - this.scrollbarThiccness;
      this.lastScrollbarLength = this.fixedScrollbarLength == 0 ? Math.min(Math.floor((double)((float)selfSize / (float)this.childSize * (float)contentSize)), (double)contentSize) : (double)this.fixedScrollbarLength;
      double scrollbarPosition = this.maxScroll != 0 ? this.currentScrollPosition / (double)this.maxScroll * ((double)contentSize - this.lastScrollbarLength) : (double)0.0F;
      if (this.direction == ScrollContainer.ScrollDirection.VERTICAL) {
         this.scrollbar.draw(graphics, this.scrollbarOffset, (int)((double)this.y + scrollbarPosition + (double)padding.top()), this.scrollbarThiccness, (int)this.lastScrollbarLength, this.scrollbarOffset, this.y + padding.top(), this.scrollbarThiccness, this.height - padding.vertical(), this.lastScrollbarInteractTime, this.direction, this.maxScroll > 0);
      } else {
         this.scrollbar.draw(graphics, (int)((double)this.x + scrollbarPosition + (double)padding.left()), this.scrollbarOffset, (int)this.lastScrollbarLength, this.scrollbarThiccness, this.x + padding.left(), this.scrollbarOffset, this.width - padding.horizontal(), this.scrollbarThiccness, this.lastScrollbarInteractTime, this.direction, this.maxScroll > 0);
      }

   }

   public boolean canFocus(UIComponent.FocusSource source) {
      return true;
   }

   public boolean onMouseScroll(double mouseX, double mouseY, double amount) {
      if (this.child.onMouseScroll((double)this.x + mouseX - (double)this.child.x(), (double)this.y + mouseY - (double)this.child.y(), amount)) {
         return true;
      } else {
         if (this.scrollStep < 1) {
            this.scrollBy(-amount * (double)15.0F, false, true);
         } else {
            this.scrollBy(-amount * (double)this.scrollStep, true, true);
         }

         return true;
      }
   }

   public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
      if (this.isInScrollbar((double)this.x + click.x(), (double)this.y + click.y())) {
         super.onMouseDown(click, doubled);
         return true;
      } else {
         return super.onMouseDown(click, doubled);
      }
   }

   public boolean onMouseDrag(MouseButtonEvent click, double deltaX, double deltaY) {
      if (!this.scrollbaring && !this.isInScrollbar((double)this.x + click.x(), (double)this.y + click.y())) {
         return super.onMouseDrag(click, deltaX, deltaY);
      } else {
         double delta = this.direction.choose(deltaX, deltaY);
         double selfSize = (double)((Integer)this.direction.sizeGetter.apply(this) - (Integer)this.direction.insetGetter.apply((Insets)this.padding.get()));
         double scalar = (double)this.maxScroll / (selfSize - this.lastScrollbarLength);
         if (!Double.isFinite(scalar)) {
            scalar = (double)0.0F;
         }

         this.scrollBy(delta * scalar, true, false);
         this.scrollbaring = true;
         return true;
      }
   }

   public boolean onKeyPress(KeyEvent input) {
      if (input.key() == this.direction.lessKeycode) {
         this.scrollBy((double)-10.0F, false, true);
      } else if (input.key() == this.direction.moreKeycode) {
         this.scrollBy((double)10.0F, false, true);
      } else if (input.key() == 267) {
         this.scrollBy(this.direction.choose((double)this.width, (double)this.height) * 0.8, false, true);
         this.lastScrollbarInteractTime = System.currentTimeMillis() + 1250L;
      } else if (input.key() == 266) {
         this.scrollBy(this.direction.choose((double)this.width, (double)this.height) * -0.8, false, true);
      }

      return false;
   }

   public boolean onMouseUp(MouseButtonEvent click) {
      this.scrollbaring = false;
      return true;
   }

   public @Nullable UIComponent childAt(int x, int y) {
      return (UIComponent)(this.isInScrollbar((double)x, (double)y) ? this : super.childAt(x, y));
   }

   protected void scrollBy(double offset, boolean instant, boolean showScrollbar) {
      this.scrollOffset = Mth.clamp(this.scrollOffset + offset, (double)0.0F, (double)this.maxScroll + (double)0.5F);
      if (instant) {
         this.currentScrollPosition = this.scrollOffset;
      }

      if (showScrollbar) {
         this.lastScrollbarInteractTime = System.currentTimeMillis() + 1250L;
      }

   }

   protected boolean isInScrollbar(double mouseX, double mouseY) {
      return this.isInBoundingBox(mouseX, mouseY) && this.direction.choose(mouseY, mouseX) >= (double)this.scrollbarOffset;
   }

   public ScrollContainer<C> scrollTo(UIComponent component) {
      if (this.direction == ScrollContainer.ScrollDirection.VERTICAL) {
         this.scrollOffset = Mth.clamp(this.scrollOffset - (double)(this.y - component.y() + ((Insets)component.margins().get()).top()), (double)0.0F, (double)this.maxScroll);
      } else {
         this.scrollOffset = Mth.clamp(this.scrollOffset - (double)(this.x - component.x() + ((Insets)component.margins().get()).right()), (double)0.0F, (double)this.maxScroll);
      }

      return this;
   }

   public ScrollContainer<C> scrollTo(@Range(
   from = 0L,
   to = 1L
) double progress) {
      this.scrollOffset = (double)this.maxScroll * progress;
      return this;
   }

   public ScrollContainer<C> scrollbarThiccness(int scrollbarThiccness) {
      this.scrollbarThiccness = scrollbarThiccness;
      return this;
   }

   public int scrollbarThiccness() {
      return this.scrollbarThiccness;
   }

   public ScrollContainer<C> scrollbar(Scrollbar scrollbar) {
      this.scrollbar = scrollbar;
      return this;
   }

   public Scrollbar scrollbar() {
      return this.scrollbar;
   }

   public ScrollContainer<C> scrollStep(int scrollStep) {
      this.scrollStep = scrollStep;
      return this;
   }

   public int scrollStep() {
      return this.scrollStep;
   }

   public ScrollContainer<C> fixedScrollbarLength(int fixedScrollbarLength) {
      this.fixedScrollbarLength = fixedScrollbarLength;
      return this;
   }

   public int fixedScrollbarLength() {
      return this.fixedScrollbarLength;
   }

   @FunctionalInterface
   public interface Scrollbar {
      static Scrollbar flat(Color color) {
         int scrollbarColor = color.argb();
         return (context, x, y, width, height, trackX, trackY, trackWidth, trackHeight, lastInteractTime, direction, active) -> {
            if (active) {
               float ratio = (float)Mth.clamp(lastInteractTime - System.currentTimeMillis(), 0L, 750L) / 750.0F;
               float progress = (float)Math.sin(ratio * Math.PI / 2.0);
               int alpha = (int)(progress * (float)(scrollbarColor >>> 24));
               context.fill(x, y, x + width, y + height, alpha << 24 | scrollbarColor & 16777215);
            }
         };
      }

      static Scrollbar vanilla() {
         return (context, x, y, width, height, trackX, trackY, trackWidth, trackHeight, lastInteractTime, direction, active) -> {
            NinePatchTexture.draw(ScrollContainer.VANILLA_SCROLLBAR_TRACK_TEXTURE, context, trackX, trackY, trackWidth, trackHeight);
            Identifier texture = direction == ScrollContainer.ScrollDirection.VERTICAL ? (active ? ScrollContainer.VERTICAL_VANILLA_SCROLLBAR_TEXTURE : ScrollContainer.DISABLED_VERTICAL_VANILLA_SCROLLBAR_TEXTURE) : (active ? ScrollContainer.HORIZONTAL_VANILLA_SCROLLBAR_TEXTURE : ScrollContainer.DISABLED_HORIZONTAL_VANILLA_SCROLLBAR_TEXTURE);
            NinePatchTexture.draw(texture, context, x + 1, y + 1, width - 2, height - 2);
         };
      }

      static Scrollbar vanillaFlat() {
         return (context, x, y, width, height, trackX, trackY, trackWidth, trackHeight, lastInteractTime, direction, active) -> {
            context.fill(trackX, trackY, trackX + trackWidth, trackY + trackHeight, Color.BLACK.argb());
            NinePatchTexture.draw(ScrollContainer.FLAT_VANILLA_SCROLLBAR_TEXTURE, context, x, y, width, height);
         };
      }

      void draw(OwoUIGraphics var1, int var2, int var3, int var4, int var5, int var6, int var7, int var8, int var9, long var10, ScrollDirection var12, boolean var13);
   }

   public static enum ScrollDirection {
      VERTICAL(UIComponent::height, UIComponent::updateY, UIComponent::y, Insets::vertical, 265, 264),
      HORIZONTAL(UIComponent::width, UIComponent::updateX, UIComponent::x, Insets::horizontal, 263, 262);

      public final Function<UIComponent, Integer> sizeGetter;
      public final BiConsumer<UIComponent, Integer> coordinateSetter;
      public final Function<ScrollContainer<?>, Integer> coordinateGetter;
      public final Function<Insets, Integer> insetGetter;
      public final int lessKeycode;
      public final int moreKeycode;

      private ScrollDirection(Function<UIComponent, Integer> sizeGetter, BiConsumer<UIComponent, Integer> coordinateSetter, Function<ScrollContainer<?>, Integer> coordinateGetter, Function<Insets, Integer> insetGetter, int lessKeycode, int moreKeycode) {
         this.sizeGetter = sizeGetter;
         this.coordinateSetter = coordinateSetter;
         this.coordinateGetter = coordinateGetter;
         this.insetGetter = insetGetter;
         this.lessKeycode = lessKeycode;
         this.moreKeycode = moreKeycode;
      }

      public double choose(double horizontal, double vertical) {
         double var10000;
         switch (this.ordinal()) {
            case 0 -> var10000 = vertical;
            case 1 -> var10000 = horizontal;
            default -> throw new MatchException((String)null, (Throwable)null);
         }

         return var10000;
      }

      private static ScrollDirection[] $values() {
         return new ScrollDirection[]{VERTICAL, HORIZONTAL};
      }
   }
}
