package ml.mypals.carpetgui.ui.core;


public interface PositionedRectangle {
   int carpetGUI$x();

   int carpetGUI$y();

   int carpetGUI$width();

   int carpetGUI$height();

   default boolean isInBoundingBox(double x, double y) {
      return x >= (double)this.carpetGUI$x() && x < (double)(this.carpetGUI$x() + this.carpetGUI$width()) && y >= (double)this.carpetGUI$y() && y < (double)(this.carpetGUI$y() + this.carpetGUI$height());
   }

   default boolean intersects(PositionedRectangle other) {
      return other.carpetGUI$x() < this.carpetGUI$x() + this.carpetGUI$width() && other.carpetGUI$x() + other.carpetGUI$width() >= this.carpetGUI$x() && other.carpetGUI$y() < this.carpetGUI$y() + this.carpetGUI$height() && other.carpetGUI$y() + other.carpetGUI$height() >= this.carpetGUI$y();
   }

   default PositionedRectangle intersection(PositionedRectangle other) {
      int leftEdge = Math.max(this.carpetGUI$x(), other.carpetGUI$x());
      int topEdge = Math.max(this.carpetGUI$y(), other.carpetGUI$y());
      int rightEdge = Math.min(this.carpetGUI$x() + this.carpetGUI$width(), other.carpetGUI$x() + other.carpetGUI$width());
      int bottomEdge = Math.min(this.carpetGUI$y() + this.carpetGUI$height(), other.carpetGUI$y() + other.carpetGUI$height());
      return of(leftEdge, topEdge, Math.max(rightEdge - leftEdge, 0), Math.max(bottomEdge - topEdge, 0));
   }

   default PositionedRectangle interpolate(PositionedRectangle next, float delta) {
      return of(
         (int) (this.carpetGUI$x() + delta * (next.carpetGUI$x() - this.carpetGUI$x())),
         (int) (this.carpetGUI$y() + delta * (next.carpetGUI$y() - this.carpetGUI$y())),
         (int) (this.carpetGUI$width() + delta * (next.carpetGUI$width() - this.carpetGUI$width())),
         (int) (this.carpetGUI$height() + delta * (next.carpetGUI$height() - this.carpetGUI$height()))
      );
   }

   //? if >=1.20 {
   /*default PositionedRectangle transform(org.joml.Matrix3x2f matrix) {
      org.joml.Vector2f pos1 = matrix.transformPosition((float)this.carpetGUI$x(), (float)this.carpetGUI$y(), new org.joml.Vector2f());
      org.joml.Vector2f pos2 = matrix.transformPosition((float)(this.carpetGUI$x() + this.carpetGUI$width()), (float)(this.carpetGUI$y() + this.carpetGUI$height()), new org.joml.Vector2f());
      return of((int)pos1.x, (int)pos1.y, (int)(pos2.x - pos1.x), (int)(pos2.y - pos1.y));
   }
   *///?}

   static PositionedRectangle of(int x, int y, Size size) {
      return of(x, y, size.width(), size.height());
   }

   static PositionedRectangle of(final int x, final int y, final int width, final int height) {
      return new PositionedRectangle() {
         public int carpetGUI$x() {
            return x;
         }

         public int carpetGUI$y() {
            return y;
         }

         public int carpetGUI$width() {
            return width;
         }

         public int carpetGUI$height() {
            return height;
         }
      };
   }
}
