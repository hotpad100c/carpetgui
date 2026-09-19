package ml.mypals.carpetgui.ui.core;

public enum CursorStyle {
   NONE(0),
   POINTER(221185),
   TEXT(221186),
   HAND(221188),
   CROSSHAIR(221187),
   MOVE(221193),
   HORIZONTAL_RESIZE(221189),
   VERTICAL_RESIZE(221190),
   NWSE_RESIZE(221191),
   NESW_RESIZE(221192),
   NOT_ALLOWED(221194);

   public final int glfw;

   private CursorStyle(int glfw) {
      this.glfw = glfw;
   }

   private static CursorStyle[] $values() {
      return new CursorStyle[]{NONE, POINTER, TEXT, HAND, CROSSHAIR, MOVE, HORIZONTAL_RESIZE, VERTICAL_RESIZE, NWSE_RESIZE, NESW_RESIZE, NOT_ALLOWED};
   }
}
