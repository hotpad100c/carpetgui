package ml.mypals.carpetgui.compat.input;

//? if <1.21.9 {
import org.lwjgl.glfw.GLFW;

public record KeyEvent(int key, int scancode, int modifiers) {
    public int input() { return key; }
    public boolean isEscape() { return key == GLFW.GLFW_KEY_ESCAPE; }
    public boolean hasControlDown() { return (modifiers & 2) != 0; }
    public boolean hasShiftDown() { return (modifiers & 1) != 0; }
    public boolean hasAltDown() { return (modifiers & 4) != 0; }
    public boolean isCycleFocus() { return key == GLFW.GLFW_KEY_TAB; }
    public boolean isUp() { return key == GLFW.GLFW_KEY_UP; }
    public boolean isDown() { return key == GLFW.GLFW_KEY_DOWN; }
    public boolean isLeft() { return key == GLFW.GLFW_KEY_LEFT; }
    public boolean isRight() { return key == GLFW.GLFW_KEY_RIGHT; }
}
//?}
