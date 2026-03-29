package ml.mypals.carpetgui.mixin;

import ml.mypals.carpetgui.settings.GamerulesDefaultValueSorter;
import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRules.class)
public class GamerulesMixin {
    @Unique
    private static GameRules.GameRuleTypeVisitor DETECTOR;

    @Inject(method = "register", at = @At("RETURN"))
    private static <T extends GameRules.Value<T>> void register(String string, GameRules.Category category, GameRules.Type<T> type, CallbackInfoReturnable<GameRules.Key<T>> cir) {
        if (DETECTOR == null) {
            DETECTOR = new GameRules.GameRuleTypeVisitor() {
                @Override
                public <T extends GameRules.Value<T>> void visit(GameRules.Key<T> key, GameRules.Type<T> type) {
                    GameRules.GameRuleTypeVisitor.super.visit(key, type);
                }

                @Override
                public void visitBoolean(GameRules.Key<GameRules.BooleanValue> key, GameRules.Type<GameRules.BooleanValue> type) {
                    GamerulesDefaultValueSorter.gamerulesDefaultValues.put(
                            key,
                            type.createRule().get() ? "true" : "false"
                    );
                    GameRules.GameRuleTypeVisitor.super.visitBoolean(key, type);
                }

                @Override
                public void visitInteger(GameRules.Key<GameRules.IntegerValue> key, GameRules.Type<GameRules.IntegerValue> type) {
                    GamerulesDefaultValueSorter.gamerulesDefaultValues.put(
                            key,
                            type.createRule().serialize()
                    );
                    GameRules.GameRuleTypeVisitor.super.visitInteger(key, type);
                }
            };
        }
        type.callVisitor(DETECTOR, cir.getReturnValue());
    }
}
