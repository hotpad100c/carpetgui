package ml.mypals.carpetgui.compat.input;

//? if <1.21.9 {
/*public record CharacterEvent(int codepoint, int modifiers) {
    public String codepointAsString() {
        return Character.toString(codepoint);
    }
    public boolean isAllowedChatCharacter() {
        return codepoint != 167 && codepoint >= 32 && codepoint != 127;
    }
}
*///?}
