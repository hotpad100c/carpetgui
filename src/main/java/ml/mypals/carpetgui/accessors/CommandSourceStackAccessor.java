package ml.mypals.carpetgui.accessors;

public interface CommandSourceStackAccessor {
   default void carpetGUI$setSilent(boolean silent) {
   }

   default boolean carpetGUI$getSilent() {
      return false;
   }
}
