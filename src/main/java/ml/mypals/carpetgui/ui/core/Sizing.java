package ml.mypals.carpetgui.ui.core;

import java.util.Objects;
import java.util.function.Function;
import net.minecraft.util.Mth;

public class Sizing {
   private static final Sizing CONTENT_SIZING;
   public final Method method;
   public final int value;

   private Sizing(int value, Method method) {
      this.method = method;
      this.value = value;
   }

   public int inflate(int space, Function<Sizing, Integer> contentSizeFunction) {
      int var10000;
      switch (this.method.ordinal()) {
         case 0:
            var10000 = this.value;
            break;
         case 1:
            var10000 = (Integer)contentSizeFunction.apply(this) + this.value * 2;
            break;
         case 2:
         case 3:
            var10000 = Math.round((float)this.value / 100.0F * (float)space);
            break;
         default:
            throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
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

   public static Sizing expand() {
      return expand(100);
   }

   public static Sizing expand(int percent) {
      return new Sizing(percent, Sizing.Method.EXPAND);
   }

   public boolean isContent() {
      return this.method == Sizing.Method.CONTENT;
   }

   public boolean isExpand() {
      return this.method == Sizing.Method.EXPAND;
   }

   public float contentFactor() {
      return this.isContent() ? 1.0F : 0.0F;
   }

   public Sizing interpolate(Sizing next, float delta) {
      return (Sizing)(next.method != this.method ? new MergedSizing(this, next, delta) : new Sizing(Mth.lerpInt(delta, this.value, next.value), this.method));
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
      return Objects.hash(new Object[]{this.method, this.value});
   }

   static {
      CONTENT_SIZING = new Sizing(0, Sizing.Method.CONTENT);
   }

   public static class Random {
      private static final java.util.Random SIZING_RANDOM = new java.util.Random();

      public static Sizing fill(int min, int max) {
         return Sizing.fill(SIZING_RANDOM.nextInt(min, max));
      }

      public static Sizing fill(int max) {
         return Sizing.fill(SIZING_RANDOM.nextInt(0, max));
      }

      public static Sizing fill() {
         return Sizing.fill(SIZING_RANDOM.nextInt(0, 100));
      }

      public static Sizing expand(int min, int max) {
         return Sizing.expand(SIZING_RANDOM.nextInt(min, max));
      }

      public static Sizing expand(int max) {
         return Sizing.expand(SIZING_RANDOM.nextInt(0, max));
      }

      public static Sizing expand() {
         return Sizing.expand(SIZING_RANDOM.nextInt(0, 100));
      }

      public static Sizing fixed(int min, int max) {
         return Sizing.fixed(SIZING_RANDOM.nextInt(min, max));
      }

      public static Sizing fixed(int max) {
         return Sizing.fixed(SIZING_RANDOM.nextInt(0, max));
      }

      public static Sizing fixed() {
         return Sizing.fixed(SIZING_RANDOM.nextInt(0, 100));
      }

      public static Sizing content(int min, int max) {
         return Sizing.content(SIZING_RANDOM.nextInt(min, max));
      }

      public static Sizing content(int max) {
         return Sizing.content(SIZING_RANDOM.nextInt(0, max));
      }

      public static Sizing content() {
         return Sizing.content(SIZING_RANDOM.nextInt(0, 100));
      }

      public static Sizing random(int min, int max) {
         Sizing var10000;
         switch (SIZING_RANDOM.nextInt(0, 4)) {
            case 0 -> var10000 = fill(min, max);
            case 1 -> var10000 = expand(min, max);
            case 2 -> var10000 = fixed(min, max);
            case 3 -> var10000 = content(min, max);
            default -> throw new IllegalStateException("Unexpected value: " + SIZING_RANDOM.nextInt(0, 4));
         }

         return var10000;
      }

      public static Sizing random(int max) {
         return random(0, max);
      }

      public static Sizing random() {
         return random(0, 100);
      }

      public static Sizing noContent(int min, int max) {
         Sizing var10000;
         switch (SIZING_RANDOM.nextInt(0, 3)) {
            case 0 -> var10000 = fill(min, max);
            case 1 -> var10000 = expand(min, max);
            case 2 -> var10000 = fixed(min, max);
            default -> throw new IllegalStateException("Unexpected value: " + SIZING_RANDOM.nextInt(0, 3));
         }

         return var10000;
      }

      public static Sizing noContent(int max) {
         return noContent(0, max);
      }

      public static Sizing noContent() {
         return noContent(0, 100);
      }
   }

   public static enum Method {
      FIXED,
      CONTENT,
      FILL,
      EXPAND;

      private static Method[] $values() {
         return new Method[]{FIXED, CONTENT, FILL, EXPAND};
      }
   }

   private static final class MergedSizing extends Sizing {
      private final Sizing first;
      private final Sizing second;
      private final float delta;

      private MergedSizing(Sizing first, Sizing second, float delta) {
         super(first.value, first.method);
         this.first = first;
         this.second = second;
         this.delta = delta;
      }

      public int inflate(int space, Function<Sizing, Integer> contentSizeFunction) {
         return Mth.lerpInt(this.delta, this.first.inflate(space, contentSizeFunction), this.second.inflate(space, contentSizeFunction));
      }

      public Sizing interpolate(Sizing next, float delta) {
         return this.first.interpolate(next, delta);
      }

      public boolean isContent() {
         return this.first.isContent() || this.second.isContent();
      }

      public float contentFactor() {
         if (this.first.isContent() && this.second.isContent()) {
            return super.contentFactor();
         } else if (this.first.isContent()) {
            return 1.0F - this.delta;
         } else {
            return this.second.isContent() ? this.delta : 0.0F;
         }
      }

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            if (!super.equals(o)) {
               return false;
            } else {
               MergedSizing that = (MergedSizing)o;
               return Float.compare(this.delta, that.delta) == 0 && Objects.equals(this.first, that.first) && Objects.equals(this.second, that.second);
            }
         } else {
            return false;
         }
      }

      public int hashCode() {
         return Objects.hash(new Object[]{super.hashCode(), this.first, this.second, this.delta});
      }
   }
}
