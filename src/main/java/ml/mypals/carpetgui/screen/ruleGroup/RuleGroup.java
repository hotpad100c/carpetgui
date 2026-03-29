package ml.mypals.carpetgui.screen.ruleGroup;

import java.nio.file.Path;
import java.util.List;

public record RuleGroup(String name, Path filePath, List<RuleCommand> commands) {
}
