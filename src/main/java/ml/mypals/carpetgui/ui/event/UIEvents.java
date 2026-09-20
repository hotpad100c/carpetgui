package ml.mypals.carpetgui.ui.event;

import ml.mypals.carpetgui.ui.core.UIComponent;
import ml.mypals.carpetgui.ui.util.EventStream;
//? if >=1.21.9 {
/*import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
*///?} else {
import ml.mypals.carpetgui.compat.input.CharacterEvent;
import ml.mypals.carpetgui.compat.input.KeyEvent;
import ml.mypals.carpetgui.compat.input.MouseButtonEvent;
//?}

public final class UIEvents {
   private UIEvents() {
   }

   @FunctionalInterface
   public interface MouseDown {
      boolean onMouseDown(MouseButtonEvent click, boolean doubled);

      static EventStream<MouseDown> newStream() {
         return new EventStream<>((subscribers) -> (click, doubled) -> {
            boolean anyTriggered = false;
            for (MouseDown subscriber : subscribers) {
               anyTriggered |= subscriber.onMouseDown(click, doubled);
            }
            return anyTriggered;
         });
      }
   }

   @FunctionalInterface
   public interface MouseUp {
      boolean onMouseUp(MouseButtonEvent click);

      static EventStream<MouseUp> newStream() {
         return new EventStream<>((subscribers) -> (click) -> {
            boolean anyTriggered = false;
            for (MouseUp subscriber : subscribers) {
               anyTriggered |= subscriber.onMouseUp(click);
            }
            return anyTriggered;
         });
      }
   }

   @FunctionalInterface
   public interface MouseScroll {
      boolean onMouseScroll(double mouseX, double mouseY, double amount);

      static EventStream<MouseScroll> newStream() {
         return new EventStream<>((subscribers) -> (mouseX, mouseY, amount) -> {
            boolean anyTriggered = false;
            for (MouseScroll subscriber : subscribers) {
               anyTriggered |= subscriber.onMouseScroll(mouseX, mouseY, amount);
            }
            return anyTriggered;
         });
      }
   }

   @FunctionalInterface
   public interface MouseDrag {
      boolean onMouseDrag(MouseButtonEvent click, double deltaX, double deltaY);

      static EventStream<MouseDrag> newStream() {
         return new EventStream<>((subscribers) -> (click, deltaX, deltaY) -> {
            boolean anyTriggered = false;
            for (MouseDrag subscriber : subscribers) {
               anyTriggered |= subscriber.onMouseDrag(click, deltaX, deltaY);
            }
            return anyTriggered;
         });
      }
   }

   @FunctionalInterface
   public interface KeyPress {
      boolean onKeyPress(KeyEvent event);

      static EventStream<KeyPress> newStream() {
         return new EventStream<>((subscribers) -> (input) -> {
            boolean anyTriggered = false;
            for (KeyPress subscriber : subscribers) {
               anyTriggered |= subscriber.onKeyPress(input);
            }
            return anyTriggered;
         });
      }
   }

   @FunctionalInterface
   public interface CharTyped {
      boolean onCharTyped(CharacterEvent event);

      static EventStream<CharTyped> newStream() {
         return new EventStream<>((subscribers) -> (input) -> {
            boolean anyTriggered = false;
            for (CharTyped subscriber : subscribers) {
               anyTriggered |= subscriber.onCharTyped(input);
            }
            return anyTriggered;
         });
      }
   }

   @FunctionalInterface
   public interface FocusGained {
      void onFocusGained(UIComponent.FocusSource source);

      static EventStream<FocusGained> newStream() {
         return new EventStream<>((subscribers) -> (source) -> {
            for (FocusGained subscriber : subscribers) {
               subscriber.onFocusGained(source);
            }
         });
      }
   }

   @FunctionalInterface
   public interface FocusLost {
      void onFocusLost();

      static EventStream<FocusLost> newStream() {
         return new EventStream<>((subscribers) -> () -> {
            for (FocusLost subscriber : subscribers) {
               subscriber.onFocusLost();
            }
         });
      }
   }

   @FunctionalInterface
   public interface MouseEnter {
      void onMouseEnter();

      static EventStream<MouseEnter> newStream() {
         return new EventStream<>((subscribers) -> () -> {
            for (MouseEnter subscriber : subscribers) {
               subscriber.onMouseEnter();
            }
         });
      }
   }

   @FunctionalInterface
   public interface MouseLeave {
      void onMouseLeave();

      static EventStream<MouseLeave> newStream() {
         return new EventStream<>((subscribers) -> () -> {
            for (MouseLeave subscriber : subscribers) {
               subscriber.onMouseLeave();
            }
         });
      }
   }
}
