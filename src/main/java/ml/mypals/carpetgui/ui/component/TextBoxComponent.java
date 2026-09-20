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
//? if >=1.21.9 {
/*import net.minecraft.client.input.KeyEvent;
*///?} else {
import ml.mypals.carpetgui.compat.input.KeyEvent;
//?}
import net.minecraft.network.chat.Component;

public class TextBoxComponent extends EditBox {
   protected final Observable<Boolean> showsBackground = Observable.<Boolean>of(((EditBoxAccessor)this).carpetGUI$bordered());
   protected final Observable<String> textValue = Observable.<String>of("");
   protected final EventStream<OnChanged> changedEvents = TextBoxComponent.OnChanged.newStream();
   protected Predicate<String> filter = Objects::nonNull;

   public Observable<String> textValue() {
      return this.textValue;
   }

   //? if <1.19.3 {
   @Override
   public void setFocused(boolean focused) {
      super.setFocused(focused);
   }
   //?}

   protected TextBoxComponent(Sizing horizontalSizing) {
      super(Minecraft.getInstance().font, 0, 0, 0, 0, net.minecraft.network.chat.TextComponent.EMPTY);
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

   //? if <1.21.9 {
   @Override
   public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      boolean result = super.keyPressed(keyCode, scanCode, modifiers);
      if (keyCode == com.mojang.blaze3d.platform.InputConstants.KEY_TAB) {
         this.insertText("    ");
         return true;
      } else {
         return result;
      }
   }

   @Override
   public boolean carpetGUI$onKeyPress(KeyEvent input) {
      return this.keyPressed(input.key(), input.scancode(), input.modifiers());
   }
   //?} else {
   /*public boolean keyPressed(KeyEvent input) {
      boolean result = super.keyPressed(input);
      if (input.isCycleFocus()) {
         this.insertText("    ");
         return true;
      } else {
         return result;
      }
   }
   *///?}

   public void updateX(int x) {
      super.carpetGUI$updateX(x);
      //? if >=1.21.6 {
      /*((EditBoxAccessor)this).carpetGUI$updateTextPosition();
      *///?}
   }

   public void updateY(int y) {
      super.carpetGUI$updateY(y);
      //? if >=1.21.6 {
      /*((EditBoxAccessor)this).carpetGUI$updateTextPosition();
      *///?}
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
      //? if >=1.20.2 {
      /*this.moveCursorToStart(false);
      *///?} else {
      this.moveCursorToStart();
      //?}
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
