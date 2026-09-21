package ml.mypals.carpetgui.network;

import java.util.List;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public class RuleData {
   public String manager;
   public String name;
   public String localName;
   public String defaultValue;
   public String value;
   public String description;
   public String localDescription;
   public Class<?> type;
   public List<String> suggestions;
   public List<Map.Entry<String, String>> categories;
   public boolean isGamerule;

   public RuleData() {
      this.isGamerule = false;
      this.manager = "";
      this.name = "";
      this.localName = "";
      this.defaultValue = "";
      this.value = "";
      this.description = "";
      this.localDescription = "";
      this.type = this.getClass();
      this.suggestions = List.of();
      this.categories = List.of();
   }

   public RuleData(String manager, String name, String localName, Class<?> type, String defaultValue, String value, String description, String localDescription, List<String> suggestions, List<Map.Entry<String, String>> categories) {
      this.isGamerule = false;
      this.manager = manager;
      this.name = name;
      this.localName = localName;
      this.defaultValue = defaultValue;
      this.value = value;
      this.description = description;
      this.localDescription = localDescription;
      this.type = type;
      this.suggestions = suggestions;
      this.categories = categories;
   }

   public void write(FriendlyByteBuf buf) {
      buf.writeUtf(this.manager);
      buf.writeUtf(this.name);
      buf.writeUtf(this.localName);
      buf.writeUtf(this.type.toString());
      buf.writeUtf(this.defaultValue);
      buf.writeUtf(this.value);
      buf.writeUtf(this.description);
      buf.writeUtf(this.localDescription);
      BufUtils.writeCollection(buf, this.suggestions, FriendlyByteBuf::writeUtf);
      BufUtils.writeCollection(buf, this.categories, (bf, entry) -> {
         bf.writeUtf((String)entry.getKey());
         bf.writeUtf((String)entry.getValue());
      });
   }

   public RuleData(FriendlyByteBuf buf) {
      this(buf.readUtf(), buf.readUtf(), buf.readUtf(), getRuleType(buf.readUtf()), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readUtf(), BufUtils.readList(buf, FriendlyByteBuf::readUtf), BufUtils.readList(buf, (bf) -> Map.entry(bf.readUtf(), bf.readUtf())));
      if (((String)((Map.Entry)this.categories.get(0)).getKey()).equals("gamerule")) {
         this.isGamerule = true;
         this.localDescription = Component.translatable(this.localDescription).getString();
         String[] ct = ((String)((Map.Entry)this.categories.get(0)).getValue()).split(" : ");
         this.categories = List.of(Map.entry((String)((Map.Entry)this.categories.get(0)).getKey(), Component.translatable(ct[0]).getString()));
      }

   }

   public static Class<?> getRuleType(String name) {
      Class var10000;
      switch (name) {
         case "Integer" -> var10000 = Integer.class;
         case "Boolean" -> var10000 = Boolean.class;
         case "Float" -> var10000 = Float.class;
         case "Enum" -> var10000 = Enum.class;
         default -> var10000 = String.class;
      }

      return var10000;
   }
}
