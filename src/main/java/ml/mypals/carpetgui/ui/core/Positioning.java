package ml.mypals.carpetgui.ui.core;

import java.util.Objects;
import ml.mypals.carpetgui.ui.UI;
import net.minecraft.util.Mth;
import org.slf4j.Logger;

public class Positioning {
   private static final Positioning LAYOUT_POSITIONING;
   public final Type type;
   public final int x;
   public final int y;

   private Positioning(int x, int y, Type type) {
      this.type = type;
      this.x = x;
      this.y = y;
   }

   public Positioning withX(int x) {
      return new Positioning(x, this.y, this.type);
   }

   public Positioning withY(int y) {
      return new Positioning(this.x, y, this.type);
   }

   public boolean isRelative() {
      return this.type == Positioning.Type.RELATIVE || this.type == Positioning.Type.ACROSS;
   }

   public Positioning interpolate(Positioning next, float delta) {
      if (next.type != this.type) {
         Logger var10000 = UI.LOGGER;
         String var10001 = String.valueOf(this.type);
         var10000.warn("Cannot interpolate between positioning of type " + var10001 + " and " + String.valueOf(next.type));
         return this;
      } else {
         return new Positioning(Mth.lerpInt(delta, this.x, next.x), Mth.lerpInt(delta, this.y, next.y), this.type);
      }
   }

   public static Positioning absolute(int xPixels, int yPixels) {
      return new Positioning(xPixels, yPixels, Positioning.Type.ABSOLUTE);
   }

   public static Positioning relative(int xPercent, int yPercent) {
      return new Positioning(xPercent, yPercent, Positioning.Type.RELATIVE);
   }

   public static Positioning across(int xPercent, int yPercent) {
      return new Positioning(xPercent, yPercent, Positioning.Type.ACROSS);
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
      return Objects.hash(new Object[]{this.type, this.x, this.y});
   }

   static {
      LAYOUT_POSITIONING = new Positioning(0, 0, Positioning.Type.LAYOUT);
   }

   public static enum Type {
      RELATIVE,
      ACROSS,
      ABSOLUTE,
      LAYOUT;

      private static Type[] $values() {
         return new Type[]{RELATIVE, ACROSS, ABSOLUTE, LAYOUT};
      }
   }
}
