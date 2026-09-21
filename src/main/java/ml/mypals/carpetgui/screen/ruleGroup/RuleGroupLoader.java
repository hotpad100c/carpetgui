package ml.mypals.carpetgui.screen.ruleGroup;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public final class RuleGroupLoader {
   private static final Logger LOGGER = LogManager.getLogger("carpetgui-groups");
   public static final Path GROUPS_DIR = FabricLoader.getInstance().getConfigDir().resolve("carpetgui").resolve("groups");

   private RuleGroupLoader() {
   }

   public static List<RuleGroup> loadAll() {
      try {
         Files.createDirectories(GROUPS_DIR);
      } catch (IOException e) {
         LOGGER.error("Cannot create groups directory", e);
         return List.of();
      }

      List<RuleGroup> groups = new ArrayList();

      try {
         Stream<Path> files = Files.list(GROUPS_DIR);

         try {
            files.filter((p) -> p.getFileName().toString().endsWith(".txt")).sorted(Comparator.comparing((p) -> p.getFileName().toString())).forEach((path) -> {
               RuleGroup group = loadFile(path);
               if (group != null) {
                  groups.add(group);
               }

            });
         } catch (Throwable var6) {
            if (files != null) {
               try {
                  files.close();
               } catch (Throwable var4) {
                  var6.addSuppressed(var4);
               }
            }

            throw var6;
         }

         if (files != null) {
            files.close();
         }
      } catch (IOException e) {
         LOGGER.error("Failed to list groups directory", e);
      }

      return groups;
   }

   private static RuleGroup loadFile(Path path) {
      String fileName = path.getFileName().toString();
      String groupName = fileName.substring(0, fileName.length() - 4);

      try {
         List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
         List<RuleCommand> commands = new ArrayList();

         for(String line : lines) {
            RuleCommand cmd = RuleCommand.tryParse(lines.indexOf(line), line);
            if (cmd != null) {
               commands.add(cmd);
            }
         }

         return new RuleGroup(groupName, path, commands);
      } catch (IOException e) {
         LOGGER.error("Failed to read group file '{}'", fileName, e);
         return null;
      }
   }

   public static void delete(RuleGroup group) {
      Path file = GROUPS_DIR.resolve(group.name() + ".txt");

      try {
         Files.deleteIfExists(file);
      } catch (Exception var3) {
      }

   }

   public static boolean save(RuleGroup group) {
      if (group != null && group.filePath() != null) {
         Path path = group.filePath();
         List<RuleCommand> commands = group.commands();

         try {
            List<String> lines = getStrings(commands);
            Files.write(path, lines, StandardCharsets.UTF_8);
            return true;
         } catch (IOException e) {
            LOGGER.error("Failed to save group '{}' to file '{}'", new Object[]{group.name(), path.getFileName(), e});
            return false;
         }
      } else {
         return false;
      }
   }

   private static @NotNull List<String> getStrings(List<RuleCommand> commands) {
      List<String> lines = new ArrayList();

      for(RuleCommand cmd : commands) {
         if (cmd != null) {
            lines.add(cmd.toCommand(cmd.value()));
         }
      }

      return lines;
   }
}
