package ml.mypals.carpetgui.mixin.accessors;

import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GameRules.Value.class)
public interface TypeAccessor {
    @Accessor("type")
    GameRules.Type<?> carpetGUI$getType();
}