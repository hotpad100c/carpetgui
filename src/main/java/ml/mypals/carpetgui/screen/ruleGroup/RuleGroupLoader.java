package ml.mypals.carpetgui.screen.ruleGroup;

import net.fabricmc.loader.api.FabricLoader;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public final class RuleGroupLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger("carpetgui-groups");

    public static final Path GROUPS_DIR = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("carpetgui")
            .resolve("groups");

    private RuleGroupLoader() {}

    public static List<RuleGroup> loadAll() {
        try {
            Files.createDirectories(GROUPS_DIR);
        } catch (IOException e) {
            LOGGER.error("Cannot create groups directory", e);
            return List.of();
        }

        List<RuleGroup> groups = new ArrayList<>();

        try (Stream<Path> files = Files.list(GROUPS_DIR)) {
            files.filter(p -> p.getFileName().toString().endsWith(".txt"))
                 .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                 .forEach(path -> {
                     RuleGroup group = loadFile(path);
                     if (group != null) {
                         groups.add(group);
                     }
                 });
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
            List<RuleCommand> commands = new ArrayList<>();

            for (String line : lines) {
                RuleCommand cmd = RuleCommand.tryParse(lines.indexOf(line), line);
                if (cmd != null) commands.add(cmd);
            }

            return new RuleGroup(groupName, path, commands);

        } catch (IOException e) {
            LOGGER.error("Failed to read group file '{}'", fileName, e);
            return null;
        }
    }
    public static void delete(RuleGroup group) {
        Path file = GROUPS_DIR.resolve(group.name() + ".txt");
        try{
            Files.deleteIfExists(file);
        }catch (Exception ignored){
        }

    }
    public static boolean save(RuleGroup group) {
        if (group == null || group.filePath() == null) {
            return false;
        }

        Path path = group.filePath();
        List<RuleCommand> commands = group.commands();

        try {
            List<String> lines = getStrings(commands);

            Files.write(path, lines, StandardCharsets.UTF_8);
            return true;

        } catch (IOException e) {
            LOGGER.error("Failed to save group '{}' to file '{}'", group.name(), path.getFileName(), e);
            return false;
        }
    }

    private static @NotNull List<String> getStrings(List<RuleCommand> commands) {
        List<String> lines = new ArrayList<>();

        for (RuleCommand cmd : commands) {
            if (cmd == null) continue;
            lines.add(cmd.toCommand(cmd.value()));
        }
        return lines;
    }
}
