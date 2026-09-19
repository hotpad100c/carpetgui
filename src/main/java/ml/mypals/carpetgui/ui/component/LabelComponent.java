package ml.mypals.carpetgui.ui.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import ml.mypals.carpetgui.mixin.ui.ClickableStyleFinderAccessor;
import ml.mypals.carpetgui.ui.base.BaseUIComponent;
import ml.mypals.carpetgui.ui.core.AnimatableProperty;
import ml.mypals.carpetgui.ui.core.Color;
import ml.mypals.carpetgui.ui.core.HorizontalAlignment;
import ml.mypals.carpetgui.ui.core.OwoUIGraphics;
import ml.mypals.carpetgui.ui.core.Size;
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.core.VerticalAlignment;
import ml.mypals.carpetgui.ui.util.Observable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ActiveTextCollector;
import net.minecraft.client.gui.Font;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

public class LabelComponent extends BaseUIComponent {
   protected final Font textRenderer;
   protected Component text;
   protected List<FormattedCharSequence> wrappedText;
   protected VerticalAlignment verticalTextAlignment;
   protected HorizontalAlignment horizontalTextAlignment;
   protected final AnimatableProperty<Color> color;
   protected final Observable<Integer> lineHeight;
   protected final Observable<Integer> lineSpacing;
   protected boolean shadow;
   protected int maxWidth;
   protected Function<@Nullable Style, Boolean> textClickHandler;

   protected LabelComponent(Component text) {
      this.textRenderer = Minecraft.getInstance().font;
      this.verticalTextAlignment = VerticalAlignment.TOP;
      this.horizontalTextAlignment = HorizontalAlignment.LEFT;
      this.color = AnimatableProperty.<Color>of(Color.WHITE);
      Objects.requireNonNull(this.textRenderer);
      this.lineHeight = Observable.<Integer>of(9);
      this.lineSpacing = Observable.<Integer>of(2);
      this.textClickHandler = (style) -> style != null && OwoUIGraphics.utilityScreen().handleTextClick(style, Minecraft.getInstance().gui.screen());
      this.text = text;
      this.wrappedText = new ArrayList();
      this.shadow = false;
      this.maxWidth = Integer.MAX_VALUE;
      Observable.observeAll((Runnable)(() -> this.notifyParentIfMounted()), this.lineHeight, this.lineSpacing);
   }

   public LabelComponent text(Component text) {
      this.text = text;
      this.notifyParentIfMounted();
      return this;
   }

   public Component text() {
      return this.text;
   }

   public LabelComponent maxWidth(int maxWidth) {
      this.maxWidth = maxWidth;
      this.notifyParentIfMounted();
      return this;
   }

   public int maxWidth() {
      return this.maxWidth;
   }

   public LabelComponent shadow(boolean shadow) {
      this.shadow = shadow;
      return this;
   }

   public boolean shadow() {
      return this.shadow;
   }

   public LabelComponent color(Color color) {
      this.color.set(color);
      return this;
   }

   public AnimatableProperty<Color> color() {
      return this.color;
   }

   public LabelComponent verticalTextAlignment(VerticalAlignment verticalAlignment) {
      this.verticalTextAlignment = verticalAlignment;
      return this;
   }

   public VerticalAlignment verticalTextAlignment() {
      return this.verticalTextAlignment;
   }

   public LabelComponent horizontalTextAlignment(HorizontalAlignment horizontalAlignment) {
      this.horizontalTextAlignment = horizontalAlignment;
      return this;
   }

   public HorizontalAlignment horizontalTextAlignment() {
      return this.horizontalTextAlignment;
   }

   public LabelComponent lineHeight(int lineHeight) {
      this.lineHeight.set(lineHeight);
      return this;
   }

   public int lineHeight() {
      return (Integer)this.lineHeight.get();
   }

   public LabelComponent lineSpacing(int lineSpacing) {
      this.lineSpacing.set(lineSpacing);
      return this;
   }

   public int lineSpacing() {
      return (Integer)this.lineSpacing.get();
   }

   public LabelComponent textClickHandler(Function<@Nullable Style, Boolean> textClickHandler) {
      this.textClickHandler = textClickHandler;
      return this;
   }

   public Function<Style, Boolean> textClickHandler() {
      return this.textClickHandler;
   }

   protected int determineHorizontalContentSize(Sizing sizing) {
      int widestText = 0;

      for(FormattedCharSequence line : this.wrappedText) {
         int width = this.textRenderer.width(line);
         if (width > widestText) {
            widestText = width;
         }
      }

      if (widestText > this.maxWidth) {
         this.wrapLines();
         return this.determineHorizontalContentSize(sizing);
      } else {
         return widestText;
      }
   }

   protected int determineVerticalContentSize(Sizing sizing) {
      this.wrapLines();
      return this.textHeight();
   }

   public void inflate(Size space) {
      this.wrapLines();
      super.inflate(space);
   }

   private void wrapLines() {
      this.wrappedText = this.textRenderer.split(this.text, ((Sizing)this.horizontalSizing.get()).isContent() ? this.maxWidth : this.width);
   }

   protected int textHeight() {
      return this.wrappedText.size() * (this.lineHeight() + this.lineSpacing()) - this.lineSpacing();
   }

   public void update(float delta, int mouseX, int mouseY) {
      super.update(delta, mouseX, mouseY);
      this.color.update(delta);
   }

   public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
      graphics.push().translate(0.0F, 1.0F / (float)Minecraft.getInstance().getWindow().getGuiScale());
      this.drawText((renderX, renderY, text, shadow, color) -> graphics.text(Minecraft.getInstance().font, text, renderX, renderY, color.argb(), shadow));
      graphics.pop();
   }

   protected void drawText(LabelDrawFunction goodFunction) {
      int x = this.x;
      int y = this.y;
      if (((Sizing)this.horizontalSizing.get()).isContent()) {
         x += ((Sizing)this.horizontalSizing.get()).value;
      }

      if (((Sizing)this.verticalSizing.get()).isContent()) {
         y += ((Sizing)this.verticalSizing.get()).value;
      }

      switch (this.verticalTextAlignment) {
         case CENTER -> y += (this.height - this.textHeight()) / 2;
         case BOTTOM -> y += this.height - this.textHeight();
      }

      int lambdaX = x;
      int lambdaY = y;

      for(int i = 0; i < this.wrappedText.size(); ++i) {
         FormattedCharSequence renderText = (FormattedCharSequence)this.wrappedText.get(i);
         int renderX = lambdaX;
         switch (this.horizontalTextAlignment) {
            case CENTER -> renderX = lambdaX + (this.width - this.textRenderer.width(renderText)) / 2;
            case RIGHT -> renderX = lambdaX + (this.width - this.textRenderer.width(renderText));
         }

         int renderY = lambdaY + i * (this.lineHeight() + this.lineSpacing());
         int var10001 = this.lineHeight();
         Objects.requireNonNull(this.textRenderer);
         renderY += var10001 - 9;
         goodFunction.draw(renderX, renderY, renderText, this.shadow, (Color)this.color.get());
      }

   }

   public void drawTooltip(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
      Style style = this.styleAt(mouseX - this.x, mouseY - this.y);
      if (style != null) {
         super.drawTooltip(context, mouseX, mouseY, partialTicks, delta);
         context.componentHoverEffect(this.textRenderer, style, mouseX, mouseY);
      }
   }

   public boolean shouldDrawTooltip(double mouseX, double mouseY) {
      Style hoveredStyle = this.styleAt((int)(mouseX - (double)this.x), (int)(mouseY - (double)this.y));
      return super.shouldDrawTooltip(mouseX, mouseY) || hoveredStyle != null && hoveredStyle.getHoverEvent() != null && this.isInBoundingBox(mouseX, mouseY);
   }

   public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
      return (Boolean)this.textClickHandler.apply(this.styleAt((int)click.x(), (int)click.y())) | super.onMouseDown(click, doubled);
   }

   protected @Nullable Style styleAt(int mouseX, int mouseY) {
      StyleCollector clickHandler = new StyleCollector(this.textRenderer, this.x + mouseX, this.y + mouseY);
      this.drawText((renderX, renderY, text, $, $$) -> clickHandler.accept(renderX, renderY, text));
      return clickHandler.result();
   }

   @FunctionalInterface
   protected interface LabelDrawFunction {
      void draw(int var1, int var2, FormattedCharSequence var3, boolean var4, Color var5);
   }

   private static class StyleCollector extends ActiveTextCollector.ClickableStyleFinder {
      public StyleCollector(Font font, int clickX, int clickY) {
         super(font, clickX, clickY);
         ClickableStyleFinderAccessor accessor = (ClickableStyleFinderAccessor) this;
         accessor.carpetGUI$setStyleScanner(accessor::carpetGUI$setResult);
      }
   }
}
