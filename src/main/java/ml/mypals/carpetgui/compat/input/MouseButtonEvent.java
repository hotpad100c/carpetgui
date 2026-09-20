package ml.mypals.carpetgui.compat.input;

//? if <1.21.9 {
/*public record MouseButtonEvent(double x, double y, MouseButtonInfo buttonInfo) {
    public MouseButtonEvent(double x, double y, int button) {
        this(x, y, new MouseButtonInfo(button, 0));
    }
    public int button() { return buttonInfo.button(); }
    public int input() { return button(); }
    public int modifiers() { return buttonInfo.modifiers(); }
}
*///?}
