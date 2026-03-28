package ml.mypals.carpetgui.screen.ruleGroup;  // 根据你的包路径调整

import static ml.mypals.carpetgui.CarpetGUIClient.cachedManagers;

public record RuleCommand(
        int id,
        String prefix,
        String ruleName,
        String value,
        boolean locked,
        boolean understandable
) {

    public String toCommand(String overrideValue) {
        String finalValue = (overrideValue != null && !overrideValue.isBlank())
                ? overrideValue.trim()
                : (this.value != null ? this.value.trim() : "");

        if (!understandable) {
            return finalValue;
        }

        if (finalValue.isEmpty()) {
            finalValue = this.value != null ? this.value.trim() : "";
        }

        String setDefault = !this.prefix().equals("gamerule") && this.locked ? " setDefault" :"";

        return "/" + prefix + setDefault + " " + ruleName + " " + finalValue;
    }

    public static RuleCommand tryParse(int linecode, String raw) {
        if (raw == null) return null;

        String line = raw.strip();
        if (line.isEmpty()) return null;

        if (line.startsWith("#")) {
            return null;
        }

        if (line.startsWith("/")) {
            line = line.substring(1).strip();
        }

        String[] parts = line.split("\\s+");

        if (parts.length < 3) {
            return createCustomCommand(linecode, raw);
        }

        String prefix = parts[0].toLowerCase();
        if (!cachedManagers.contains(prefix) && !prefix.equals("gamerule")) {
            return createCustomCommand(linecode, raw);
        }

        boolean isLocked = parts[1].equals("setDefault");
        String name = isLocked? parts[2]:parts[1];
        String value = isLocked? parts[3]:parts[2];

        return new RuleCommand(
                linecode,
                prefix,
                name,
                value,
                isLocked,
                true
        );
    }


    private static RuleCommand createCustomCommand(int linecode, String originalLine) {
        String cleaned = originalLine.strip();

        return new RuleCommand(
                linecode,
                null,
                null,
                cleaned,
                false,
                false
        );
    }

    public boolean isCustomCommand() {
        return !understandable;
    }
}