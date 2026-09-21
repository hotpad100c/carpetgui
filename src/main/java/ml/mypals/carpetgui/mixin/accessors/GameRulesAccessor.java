package ml.mypals.carpetgui.mixin.accessors;
//? if <1.21.11 {
/*import net.minecraft.world.level.GameRules;
 *///?} else {
import net.minecraft.world.level.gamerules.GameRules;
//?}
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(GameRules.class)
public interface GameRulesAccessor {
    //? if <1.21.11 {
    /*@Accessor("rules")
    Map<GameRules.Key<?>, GameRules.Value<?>> carpetGUI$getRules();
    *///?}
}
