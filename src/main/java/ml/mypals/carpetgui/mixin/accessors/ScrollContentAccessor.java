package ml.mypals.carpetgui.mixin.accessors;

import io.wispforest.owo.ui.container.ScrollContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ScrollContainer.class)
public interface ScrollContentAccessor {
    @Accessor("scrollOffset")
    double carpetGUI$getScrollOffset();
    @Accessor("scrollOffset")
    void carpetGUI$setScrollOffset(double scrollOffset);
    @Accessor("maxScroll")
    int carpetGUI$getMaxScroll();
    @Accessor("maxScroll")
    void carpetGUI$setMaxScroll(int maxScroll);
}
