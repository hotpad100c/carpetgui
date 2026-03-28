package ml.mypals.carpetgui.mixin.accessors;

import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRules.BooleanValue.class)
public interface BooleanValueAccessor{
    @Accessor("value")
    boolean carpetGUI$getValue();
}