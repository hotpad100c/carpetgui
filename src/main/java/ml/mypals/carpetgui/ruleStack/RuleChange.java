package ml.mypals.carpetgui.ruleStack;

import com.google.gson.JsonObject;

public record RuleChange(String ruleKey, RuleValueSnapshot previousSnapshot, RuleValueSnapshot newSnapshot) {
   public JsonObject toJson() {
      JsonObject o = new JsonObject();
      o.addProperty("ruleKey", this.ruleKey);
      o.add("prev", this.previousSnapshot.toJson());
      o.add("next", this.newSnapshot.toJson());
      return o;
   }

   public static RuleChange fromJson(JsonObject o) {
      return new RuleChange(o.get("ruleKey").getAsString(), RuleValueSnapshot.fromJson(o.getAsJsonObject("prev")), RuleValueSnapshot.fromJson(o.getAsJsonObject("next")));
   }
}
