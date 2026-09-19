package ml.mypals.carpetgui.ui.core;

import java.util.Objects;

public class Positioning {
   private static final Positioning LAYOUT_POSITIONING = new Positioning(0, 0, Positioning.Type.LAYOUT);
   public final Type type;
   public final int x;
   public final int y;

   private Positioning(int x, int y, Type type) {
      this.type = type;
      this.x = x;
      this.y = y;
   }

   public boolean isRelative() {
      return this.type == Positioning.Type.RELATIVE;
   }

   public static Positioning absolute(int xPixels, int yPixels) {
      return new Positioning(xPixels, yPixels, Positioning.Type.ABSOLUTE);
   }

   public static Positioning relative(int xPercent, int yPercent) {
      return new Positioning(xPercent, yPercent, Positioning.Type.RELATIVE);
   }

   public static Positioning layout() {
      return LAYOUT_POSITIONING;
   }

   public boolean equals(Object o) {
      if (this == o) {
         return true;
      } else if (o != null && this.getClass() == o.getClass()) {
         Positioning that = (Positioning)o;
         return this.x == that.x && this.y == that.y && this.type == that.type;
      } else {
         return false;
      }
   }

   public int hashCode() {
      return Objects.hash(this.type, this.x, this.y);
   }

   public static enum Type {
      RELATIVE,
      ABSOLUTE,
      LAYOUT;
   }
}
