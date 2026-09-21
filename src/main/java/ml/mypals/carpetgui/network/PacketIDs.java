package ml.mypals.carpetgui.network;

import net.minecraft.resources.Identifier;

public class PacketIDs {
   public static final Identifier REQUEST_RULES_ID = Identifier.fromNamespaceAndPath("carpetgui", "request_rules");
   public static final Identifier SYNC_RULES_ID = Identifier.fromNamespaceAndPath("carpetgui", "sync_rules");
   public static final Identifier HELLO_PACKET = Identifier.fromNamespaceAndPath("carpetgui", "hello");
   public static final Identifier REQUEST_RULE_STACK_ID = Identifier.fromNamespaceAndPath("carpetgui", "request_rule_stack");
   public static final Identifier RULE_STACK_SYNC_ID = Identifier.fromNamespaceAndPath("carpetgui", "rule_stack_sync");
}
