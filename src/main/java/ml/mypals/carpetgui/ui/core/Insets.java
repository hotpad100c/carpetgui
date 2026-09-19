package ml.mypals.carpetgui.ui.core;

public record Insets(int top, int bottom, int left, int right) {
   private static final Insets NONE = new Insets(0, 0, 0, 0);

   public Insets inverted() {
      return new Insets(-this.top, -this.bottom, -this.left, -this.right);
   }

   public Insets add(int top, int bottom, int left, int right) {
      return new Insets(this.top + top, this.bottom + bottom, this.left + left, this.right + right);
   }

   public int horizontal() {
      return this.left + this.right;
   }

   public int vertical() {
      return this.top + this.bottom;
   }

   public static Insets top(int top) {
      return new Insets(top, 0, 0, 0);
   }

   public static Insets bottom(int bottom) {
      return new Insets(0, bottom, 0, 0);
   }

   public static Insets left(int left) {
      return new Insets(0, 0, left, 0);
   }

   public static Insets right(int right) {
      return new Insets(0, 0, 0, right);
   }

   public static Insets of(int top, int bottom, int left, int right) {
      return new Insets(top, bottom, left, right);
   }

   public static Insets of(int inset) {
      return new Insets(inset, inset, inset, inset);
   }

   public static Insets vertical(int inset) {
      return new Insets(inset, inset, 0, 0);
   }

   public static Insets horizontal(int inset) {
      return new Insets(0, 0, inset, inset);
   }

   public static Insets none() {
      return NONE;
   }
}
