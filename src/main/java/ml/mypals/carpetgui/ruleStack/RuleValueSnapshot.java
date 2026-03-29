package ml.mypals.carpetgui.ruleStack;

import com.google.gson.JsonObject;

public record RuleValueSnapshot(String value, boolean isDefault) {

    public JsonObject toJson() {
        JsonObject o = new JsonObject();
        o.addProperty("value", value);
        o.addProperty("isDefault", isDefault);
        return o;
    }

    public static RuleValueSnapshot fromJson(JsonObject o) {
        return new RuleValueSnapshot(
                o.get("value").getAsString(),
                o.get("isDefault").getAsBoolean()
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RuleValueSnapshot(String value1, boolean aDefault))) return false;
        return value.equals(value1) && isDefault == aDefault;
    }
}