package ml.mypals.carpetgui.ruleStack;

import com.google.gson.JsonObject;

public record RuleValueSnapshot(String value, boolean isDefault) {
   public JsonObject toJson() {
      JsonObject o = new JsonObject();
      o.addProperty("value", this.value);
      o.addProperty("isDefault", this.isDefault);
      return o;
   }

   public static RuleValueSnapshot fromJson(JsonObject o) {
      return new RuleValueSnapshot(o.get("value").getAsString(), o.get("isDefault").getAsBoolean());
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof RuleValueSnapshot snapshot)) {
         return false;
      } else {
         return this.value.equals(snapshot.value) && this.isDefault == snapshot.isDefault;
      }
   }
}
