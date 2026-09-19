package ml.mypals.carpetgui.ui.component;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import ml.mypals.carpetgui.mixin.ui.EditBoxAccessor;
import ml.mypals.carpetgui.ui.core.CursorStyle;
import ml.mypals.carpetgui.ui.core.OwoUIGraphics;
import ml.mypals.carpetgui.ui.core.Sizing;
import ml.mypals.carpetgui.ui.util.EventStream;
import ml.mypals.carpetgui.ui.util.Observable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

public class TextBoxComponent extends EditBox {
   protected final Observable<Boolean> showsBackground = Observable.<Boolean>of(((EditBoxAccessor)this).carpetGUI$bordered());
   protected final Observable<String> textValue = Observable.<String>of("");
   protected final EventStream<OnChanged> changedEvents = TextBoxComponent.OnChanged.newStream();
   protected Predicate<String> filter = Objects::nonNull;

   public Observable<String> textValue() {
      return this.textValue;
   }

   protected TextBoxComponent(Sizing horizontalSizing) {
      super(Minecraft.getInstance().font, 0, 0, 0, 0, Component.empty());
      this.textValue.observe(str -> this.changedEvents.sink().onChanged(str));
      this.sizing(horizontalSizing, Sizing.content());
      this.showsBackground.observe((a) -> this.carpetGUI$widgetWrapper().notifyParentIfMounted());
   }

   /** @deprecated */
   @Deprecated(
      forRemoval = true
   )
   public void setResponder(Consumer<String> changedListener) {
      super.setResponder(changedListener);
   }

   public void drawFocusHighlight(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
   }

   public boolean keyPressed(KeyEvent input) {
      boolean result = super.keyPressed(input);
      if (input.isCycleFocus()) {
         this.insertText("    ");
         return true;
      } else {
         return result;
      }
   }

   public void updateX(int x) {
      super.carpetGUI$updateX(x);
      ((EditBoxAccessor)this).carpetGUI$updateTextPosition();
   }

   public void updateY(int y) {
      super.carpetGUI$updateY(y);
      ((EditBoxAccessor)this).carpetGUI$updateTextPosition();
   }

   public void setBordered(boolean drawsBackground) {
      super.setBordered(drawsBackground);
      this.showsBackground.set(drawsBackground);
   }

   public EventStream<OnChanged> onChanged() {
      return this.changedEvents;
   }

   public TextBoxComponent text(String text) {
      this.setValue(text);
      this.moveCursorToStart(false);
      return this;
   }

   public void setFilter(Predicate<String> predicate) {
      this.filter = predicate;
   }

   public Predicate<String> getFilter() {
      return this.filter;
   }

   public void setValue(String value) {
      if (this.filter.test(value)) {
         super.setValue(value);
      }
   }

   protected CursorStyle carpetGUI$preferredCursorStyle() {
      return CursorStyle.TEXT;
   }

   public interface OnChanged {
      void onChanged(String var1);

      static EventStream<OnChanged> newStream() {
         return new EventStream<OnChanged>((subscribers) -> (value) -> {
               for(OnChanged subscriber : subscribers) {
                  subscriber.onChanged(value);
               }

            });
      }
   }
}
