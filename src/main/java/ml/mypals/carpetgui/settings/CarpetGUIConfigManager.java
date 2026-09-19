package ml.mypals.carpetgui.settings;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import net.fabricmc.loader.api.FabricLoader;

public class CarpetGUIConfigManager {
   private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
   public static final Path CarpetGUI_DIR;
   private static final Path FAVORITE_RULES_FILE;

   public static void initializeConfig() {
      try {
         if (!Files.exists(CONFIG_DIR, new LinkOption[0])) {
            Files.createDirectories(CONFIG_DIR);
         }

         if (!Files.exists(CarpetGUI_DIR, new LinkOption[0])) {
            Files.createDirectories(CarpetGUI_DIR);
         }

         if (!Files.exists(FAVORITE_RULES_FILE, new LinkOption[0])) {
            Files.createFile(FAVORITE_RULES_FILE);
            Files.writeString(FAVORITE_RULES_FILE, "# CarpetGUI Favorite Rules Configuration\n");
         }
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   public static Path getFavoriteRulesConfigPath() {
      return FAVORITE_RULES_FILE;
   }

   public static List<String> readFavoriteRules() {
      try {
         if (Files.exists(FAVORITE_RULES_FILE, new LinkOption[0])) {
            return (List)Files.readAllLines(FAVORITE_RULES_FILE).stream().filter((line) -> !line.trim().isEmpty() && !line.trim().startsWith("#")).collect(Collectors.toList());
         }
      } catch (IOException e) {
         e.printStackTrace();
      }

      return new ArrayList();
   }

   public static void writeFavoriteRules(List<String> rules) {
      try {
         if (!Files.exists(CarpetGUI_DIR, new LinkOption[0])) {
            Files.createDirectories(CarpetGUI_DIR);
         }

         List<String> content = new ArrayList();
         content.add("# CarpetGUI Favorite Rules Configuration\n");
         if (rules != null) {
            content.addAll(rules);
         }

         Files.write(FAVORITE_RULES_FILE, content);
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   public static void addFavoriteRule(String rule) {
      if (rule != null && !rule.trim().isEmpty()) {
         List<String> rules = readFavoriteRules();
         if (!rules.contains(rule)) {
            rules.add(rule);
            writeFavoriteRules(rules);
         }

      }
   }

   public static void removeFavoriteRule(String rule) {
      if (rule != null && !rule.trim().isEmpty()) {
         List<String> rules = readFavoriteRules();
         rules.removeIf((r) -> r.equalsIgnoreCase(rule));
         writeFavoriteRules(rules);
      }
   }

   static {
      CarpetGUI_DIR = CONFIG_DIR.resolve("carpetgui");
      FAVORITE_RULES_FILE = CarpetGUI_DIR.resolve("favoriteRules.conf");
   }
}
