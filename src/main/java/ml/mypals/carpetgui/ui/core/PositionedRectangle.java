package ml.mypals.carpetgui.ui.core;

import net.minecraft.util.Mth;
import org.joml.Matrix3x2f;
import org.joml.Vector2f;

public interface PositionedRectangle {
   int x();

   int y();

   int width();

   int height();

   default boolean isInBoundingBox(double x, double y) {
      return x >= (double)this.x() && x < (double)(this.x() + this.width()) && y >= (double)this.y() && y < (double)(this.y() + this.height());
   }

   default boolean intersects(PositionedRectangle other) {
      return other.x() < this.x() + this.width() && other.x() + other.width() >= this.x() && other.y() < this.y() + this.height() && other.y() + other.height() >= this.y();
   }

   default PositionedRectangle intersection(PositionedRectangle other) {
      int leftEdge = Math.max(this.x(), other.x());
      int topEdge = Math.max(this.y(), other.y());
      int rightEdge = Math.min(this.x() + this.width(), other.x() + other.width());
      int bottomEdge = Math.min(this.y() + this.height(), other.y() + other.height());
      return of(leftEdge, topEdge, Math.max(rightEdge - leftEdge, 0), Math.max(bottomEdge - topEdge, 0));
   }

   default PositionedRectangle interpolate(PositionedRectangle next, float delta) {
      return of(Mth.lerpInt(delta, this.x(), next.x()), Mth.lerpInt(delta, this.y(), next.y()), Mth.lerpInt(delta, this.width(), next.width()), Mth.lerpInt(delta, this.height(), next.height()));
   }

   default PositionedRectangle transform(Matrix3x2f matrix) {
      Vector2f pos1 = matrix.transformPosition((float)this.x(), (float)this.y(), new Vector2f());
      Vector2f pos2 = matrix.transformPosition((float)(this.x() + this.width()), (float)(this.y() + this.height()), new Vector2f());
      return of((int)pos1.x, (int)pos1.y, (int)(pos2.x - pos1.x), (int)(pos2.y - pos1.y));
   }

   static PositionedRectangle of(int x, int y, Size size) {
      return of(x, y, size.width(), size.height());
   }

   static PositionedRectangle of(final int x, final int y, final int width, final int height) {
      return new PositionedRectangle() {
         public int x() {
            return x;
         }

         public int y() {
            return y;
         }

         public int width() {
            return width;
         }

         public int height() {
            return height;
         }
      };
   }
}
