package ml.mypals.carpetgui.screen.rulesEditScreen;

import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import ml.mypals.carpetgui.network.RuleData;
import ml.mypals.carpetgui.settings.CarpetGUIConfigManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
//? if >1.20.1 {
import net.minecraft.network.chat.contents.PlainTextContents;
//?}
import net.minecraft.sounds.SoundEvents;

import java.util.Map;
import java.util.Objects;

import static ml.mypals.carpetgui.CarpetGUIClient.defaultRules;
import static ml.mypals.carpetgui.CarpetGUIClient.favoriteRules;
import static ml.mypals.carpetgui.screen.ScreenUtils.*;


public class RuleWidget {


    private final RuleData ruleData;
    private final RulesEditScreen screen;
    private final String orgName;
    private final boolean isTrueFalseRule;
    private String query = "";
    private boolean isGamerule;
    private boolean isLocked;
    private boolean isFavorited;
    private boolean currentBoolValue;

    public RuleWidget(RuleData ruleData, RulesEditScreen screen, String query) {
        this(ruleData, screen);
        this.query = query;
    }

    public RuleWidget(RuleData ruleData, RulesEditScreen screen) {
        this.ruleData = ruleData;
        this.screen = screen;
        this.orgName = ruleData.name;
        isGamerule = ruleData.isGamerule;
        isLocked = defaultRules.contains(orgName);
        isFavorited = favoriteRules.contains(orgName);
        currentBoolValue = ruleData.value.equalsIgnoreCase("true");

        isTrueFalseRule = ruleData.suggestions.size() == 2
                && ruleData.suggestions.stream()
                .map(String::toLowerCase)
                .collect(java.util.stream.Collectors.toSet())
                .equals(java.util.Set.of("true", "false"));
    }

    public FlowLayout buildComponent() {
        var row = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.horizontalFlow(Sizing.fill(100), Sizing.fixed(25));
        row.surface(Surface.flat(0x99030303).and(Surface.outline(0x11FFFFFF)));
        row.padding(Insets.of(2, 2, 5, 5));
        row.verticalAlignment(VerticalAlignment.CENTER);

        var leftCol = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalFlow(Sizing.fill(50), Sizing.fill(100));
        leftCol.verticalAlignment(VerticalAlignment.CENTER);

        String displayName = ruleData.localName;
        var nameLabel = /*? if <1.21.11 {*//*Components*//*?} else {*/UIComponents/*?}*/.label(highlight(displayName + " : ", query));
        nameLabel.color(Color.WHITE);
        nameLabel.tooltip(buildTooltip(ruleData, query));
        leftCol.child(nameLabel);

        StringBuilder cats = new StringBuilder();
        for (String c : ruleData.categories.stream().map(Map.Entry::getValue).toList()) cats.append(c).append(" | ");
        if (cats.length() > 3) cats.setLength(cats.length() - 3);
        var catsLabel = /*? if <1.21.11 {*//*Components*//*?} else {*/UIComponents/*?}*/.label(highlight(cats.toString(), query).copy().withStyle(ChatFormatting.BLUE));
        catsLabel.color(Color.ofArgb(0xFFAAAAAA));
        leftCol.child(catsLabel);

        row.child(leftCol);

        var rightCol = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.horizontalFlow(Sizing.fill(50), Sizing.fill(100));
        rightCol.horizontalAlignment(HorizontalAlignment.RIGHT);
        rightCol.verticalAlignment(VerticalAlignment.TOP);
        rightCol.gap(4);

        if (isTrueFalseRule) {
            rightCol.child(buildBoolToggle());
        } else {
            buildTextInput(rightCol);
        }

        if (!Objects.equals(ruleData.value, ruleData.defaultValue)) {

            rightCol.child(buildSpriteToggle(
                    RESET, 10, 11,
                    (wrapper) -> {
                        /*if(isLocked){
                            sendCommand(ruleData.manager + " " + "removeDefault " + orgName);
                        }*/
                        String cmd =
                                !isGamerule && isLocked ?
                                        ruleData.manager + " " + "setDefault " : ruleData.manager + " ";
                        sendCommand(cmd + orgName + " " + ruleData.defaultValue);
                        ruleData.value = ruleData.defaultValue;
                        screen.refreshScreen();
                    }
            ));
        }

        if (!isGamerule) {

            rightCol.child(buildSpriteToggle(
                    isLocked ? LOCK_ON : LOCK_OFF, 10, 11,
                    (wrapper) -> {
                        isLocked = !isLocked;

                        if(!isLocked){
                            sendCommand(ruleData.manager + " " + "removeDefault " + orgName);
                        }
                        String cmd = isLocked ? ruleData.manager + " " + "setDefault " : ruleData.manager + " ";
                        sendCommand(cmd + orgName + " " + ruleData.value);

                        if (isLocked) defaultRules.add(orgName);
                        else defaultRules.remove(orgName);
                        if (Objects.equals(screen.currentCategory, RulesEditScreen.DefaultCategory.DEFAULT.getName()))
                            screen.setCurrentCategory(RulesEditScreen.DefaultCategory.DEFAULT.getName());
                        swapTexture(wrapper, isLocked ? LOCK_ON : LOCK_OFF, 10, 11);
                        screen.refreshScreen();
                    }
            ));
        }

        rightCol.child(buildSpriteToggle(
                isFavorited ? LOVE_ON : LOVE_OFF, 10, 11,
                (wrapper) -> {
                    isFavorited = !isFavorited;
                    if (isFavorited) {
                        CarpetGUIConfigManager.addFavoriteRule(orgName);
                        favoriteRules.add(orgName);
                    } else {
                        CarpetGUIConfigManager.removeFavoriteRule(orgName);
                        favoriteRules.remove(orgName);
                    }
                    if (Objects.equals(screen.currentCategory, RulesEditScreen.DefaultCategory.FAVORITE.getName()))
                        screen.setCurrentCategory(RulesEditScreen.DefaultCategory.FAVORITE.getName());
                    swapTexture(wrapper, isFavorited ? LOVE_ON : LOVE_OFF, 10, 11);
                }
        ));

        row.child(rightCol);
        return row;
    }


    private FlowLayout buildBoolToggle() {
        var wrapper = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.horizontalFlow(Sizing.fixed(30), Sizing.fixed(13));
        wrapper.cursorStyle(CursorStyle.HAND);
        wrapper.child(makeTexture(currentBoolValue ? TRUE_TEX : FALSE_TEX, 30, 13));
        //? if <1.21.9 {
        /*wrapper.mouseDown().subscribe((x, y, btn) -> {
        *///?} else {
        wrapper.mouseDown().subscribe((mouseButtonEvent, btn) -> {
        //?}

            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
            currentBoolValue = !currentBoolValue;

            sendCommand(ruleData.manager + " " + orgName + " " + currentBoolValue);

            ruleData.value = currentBoolValue ? "true" : "false";
            swapTexture(wrapper, currentBoolValue ? TRUE_TEX : FALSE_TEX, 30, 13);
            screen.refreshScreen();
            return true;
        });
        return wrapper;
    }

    private void buildTextInput(FlowLayout rightCol) {

        var content = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalFlow(Sizing.fill(100), Sizing.content());

        var box = /*? if <1.21.11 {*//*Components*//*?} else {*/UIComponents/*?}*/.textBox(Sizing.fill(100));
        box.setMaxLength(114514);

        var dropdown = /*? if <1.21.11 {*//*Components*//*?} else {*/UIComponents/*?}*/.dropdown(Sizing.fill(100));
        dropdown.closeWhenNotHovered(false);
        box.setSuggestion(ruleData.value);
        for (String suggestion : ruleData.suggestions) {
            dropdown.button(
                    Component.literal(suggestion),
                    d -> {
                        box.setSuggestion("");
                        box.setValue(suggestion);

                        sendCommand(ruleData.manager + " " + orgName + " " + suggestion);
                        ruleData.value = suggestion;
                        screen.refreshScreen();
                    }
            );
        }
        box.focusGained().subscribe((focusSource) -> {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1));
            box.setSuggestion("");
        });
        box.focusLost().subscribe(() -> {
            String val = box.getValue();
            if (!val.isEmpty()) {
                sendCommand(ruleData.manager + " " + orgName + " " + val);
                ruleData.value = val;
                box.setValue("");
                box.setSuggestion(ruleData.value);
                screen.refreshScreen();
            }
        });

        content.child(box);

        if (!ruleData.suggestions.isEmpty()) {
            content.child(dropdown);
        }

        var wrapper = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalFlow(
                Sizing.fixed(70),
                Sizing.fill(100)
        );

        wrapper.verticalAlignment(VerticalAlignment.CENTER);

        var scroll = /*? if <1.21.11 {*//*Containers*//*?} else {*/UIContainers/*?}*/.verticalScroll(
                Sizing.fill(100),
                Sizing.fill(100),
                content
        );

        wrapper.child(scroll);

        rightCol.child(wrapper);
    }

    private void sendCommand(String cmd) {
        var conn = Minecraft.getInstance().getConnection();
        if (this.screen.instantAffect && conn != null) conn.sendCommand(cmd);
    }
}