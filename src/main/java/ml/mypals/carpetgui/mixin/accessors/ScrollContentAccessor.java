package ml.mypals.carpetgui.mixin.accessors;

import io.wispforest.owo.ui.container.ScrollContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ScrollContainer.class)
public interface ScrollContentAccessor {
    @Accessor("scrollOffset")
    double getScrollOffset();
    @Accessor("maxScroll")
    int getMaxScroll();
}
