package ml.mypals.carpetgui.ui.core;

public enum HorizontalAlignment {
   LEFT,
   CENTER,
   RIGHT;

   public int align(int componentWidth, int span) {
      int var10000;
      switch (this.ordinal()) {
         case 0 -> var10000 = 0;
         case 1 -> var10000 = span / 2 - componentWidth / 2;
         case 2 -> var10000 = span - componentWidth;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   private static HorizontalAlignment[] $values() {
      return new HorizontalAlignment[]{LEFT, CENTER, RIGHT};
   }
}
