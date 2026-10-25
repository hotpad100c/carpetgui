package ml.mypals.carpetgui.settings;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import ml.mypals.carpetgui.ui.core.OwoUIAdapter;
import net.fabricmc.loader.api.FabricLoader;

public class CarpetGUIConfigManager {
   private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
   public static final Path CarpetGUI_DIR;
   private static final Path FAVORITE_RULES_FILE;
   private static final Path UI_CONFIG_FILE;
   public static final int DEFAULT_MENU_SIZE = 100;
   public static final int MIN_MENU_SIZE = 50;
   public static final int DEFAULT_CATEGORY_PANEL_WIDTH = 34;
   public static final int MIN_CATEGORY_PANEL_WIDTH = 20;
   public static final int MAX_CATEGORY_PANEL_WIDTH = 50;
   public static int menuWidth = DEFAULT_MENU_SIZE;
   public static int menuHeight = DEFAULT_MENU_SIZE;
   public static int categoryPanelWidth = DEFAULT_CATEGORY_PANEL_WIDTH;
   public static final int DEFAULT_UI_SCALE = 100;
   public static final int MIN_UI_SCALE = 50;
   public static final int MAX_UI_SCALE = 150;
   public static int uiScale = DEFAULT_UI_SCALE;

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

      loadUiConfig();
   }

   public static void loadUiConfig() {
      Properties props = new Properties();
      if (Files.exists(UI_CONFIG_FILE, new LinkOption[0])) {
         try (Reader reader = Files.newBufferedReader(UI_CONFIG_FILE)) {
            props.load(reader);
         } catch (IOException e) {
            e.printStackTrace();
         }
      }

      menuWidth = clamp(readInt(props, "menuWidth", DEFAULT_MENU_SIZE), MIN_MENU_SIZE, DEFAULT_MENU_SIZE);
      menuHeight = clamp(readInt(props, "menuHeight", DEFAULT_MENU_SIZE), MIN_MENU_SIZE, DEFAULT_MENU_SIZE);
      categoryPanelWidth = clamp(readInt(props, "categoryPanelWidth", DEFAULT_CATEGORY_PANEL_WIDTH), MIN_CATEGORY_PANEL_WIDTH, MAX_CATEGORY_PANEL_WIDTH);
      setUiScale(readInt(props, "uiScale", DEFAULT_UI_SCALE));
   }

   public static void saveUiConfig() {
      Properties props = new Properties();
      props.setProperty("menuWidth", String.valueOf(menuWidth));
      props.setProperty("menuHeight", String.valueOf(menuHeight));
      props.setProperty("categoryPanelWidth", String.valueOf(categoryPanelWidth));
      props.setProperty("uiScale", String.valueOf(uiScale));

      try {
         if (!Files.exists(CarpetGUI_DIR, new LinkOption[0])) {
            Files.createDirectories(CarpetGUI_DIR);
         }

         try (Writer writer = Files.newBufferedWriter(UI_CONFIG_FILE)) {
            props.store(writer, "CarpetGUI UI Configuration (sizes are percentages)");
         }
      } catch (IOException e) {
         e.printStackTrace();
      }

   }

   // Setters only update memory; callers save once the user is done (e.g. slider released).
   public static void setMenuWidth(int value) {
      menuWidth = clamp(value, MIN_MENU_SIZE, DEFAULT_MENU_SIZE);
   }

   public static void setMenuHeight(int value) {
      menuHeight = clamp(value, MIN_MENU_SIZE, DEFAULT_MENU_SIZE);
   }

   public static void setCategoryPanelWidth(int value) {
      categoryPanelWidth = clamp(value, MIN_CATEGORY_PANEL_WIDTH, MAX_CATEGORY_PANEL_WIDTH);
   }

   public static void setUiScale(int value) {
      uiScale = clamp(value, MIN_UI_SCALE, MAX_UI_SCALE);
      OwoUIAdapter.setGlobalScale((float)uiScale / 100.0F);
   }

   public static void resetUiConfig() {
      menuWidth = DEFAULT_MENU_SIZE;
      menuHeight = DEFAULT_MENU_SIZE;
      categoryPanelWidth = DEFAULT_CATEGORY_PANEL_WIDTH;
      setUiScale(DEFAULT_UI_SCALE);
      saveUiConfig();
   }

   private static int readInt(Properties props, String key, int fallback) {
      try {
         return Integer.parseInt(props.getProperty(key, String.valueOf(fallback)).trim());
      } catch (NumberFormatException e) {
         return fallback;
      }
   }

   private static int clamp(int value, int min, int max) {
      return Math.max(min, Math.min(max, value));
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
      UI_CONFIG_FILE = CarpetGUI_DIR.resolve("ui.properties");
   }
}
