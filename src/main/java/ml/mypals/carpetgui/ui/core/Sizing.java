package ml.mypals.carpetgui.ui.core;

import java.util.Objects;
import java.util.function.Function;

public class Sizing {
   private static final Sizing CONTENT_SIZING = new Sizing(0, Sizing.Method.CONTENT);
   public final Method method;
   public final int value;

   private Sizing(int value, Method method) {
      this.method = method;
      this.value = value;
   }

   public int inflate(int space, Function<Sizing, Integer> contentSizeFunction) {
      return switch (this.method) {
         case FIXED -> this.value;
         case CONTENT -> (Integer)contentSizeFunction.apply(this) + this.value * 2;
         case FILL -> Math.round((float)this.value / 100.0F * (float)space);
      };
   }

   public static Sizing fixed(int value) {
      return new Sizing(value, Sizing.Method.FIXED);
   }

   public static Sizing content() {
      return CONTENT_SIZING;
   }

   public static Sizing content(int padding) {
      return new Sizing(padding, Sizing.Method.CONTENT);
   }

   public static Sizing fill() {
      return fill(100);
   }

   public static Sizing fill(int percent) {
      return new Sizing(percent, Sizing.Method.FILL);
   }

   public boolean isContent() {
      return this.method == Sizing.Method.CONTENT;
   }

   public float contentFactor() {
      return this.isContent() ? 1.0F : 0.0F;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Sizing sizing = (Sizing)o;
         return this.value == sizing.value && this.method == sizing.method;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(this.method, this.value);
   }

   public enum Method {
      FIXED,
      CONTENT,
      FILL
   }
}
