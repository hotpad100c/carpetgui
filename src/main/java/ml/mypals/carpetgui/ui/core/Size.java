package ml.mypals.carpetgui.ui.core;

import org.jetbrains.annotations.ApiStatus.Internal;

public record Size(int width, int height) {
   private static final Size ZERO = new Size(0, 0);

   /** @deprecated */
   @Deprecated(
      forRemoval = true
   )
   @Internal
   public Size(int width, int height) {
      this.width = width;
      this.height = height;
   }

   public static Size of(int width, int height) {
      return new Size(width, height);
   }

   public static Size square(int sideLength) {
      return new Size(sideLength, sideLength);
   }

   public static Size zero() {
      return ZERO;
   }
}
