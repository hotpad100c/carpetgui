package ml.mypals.carpetgui.mixin.accessors;

import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRules.IntegerValue.class)
public interface IntegerValueAccessor {
    @Accessor("value")
    int carpetGUI$getValue();
}