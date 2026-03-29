package ml.mypals.carpetgui.network.server;

import ml.mypals.carpetgui.network.PacketIDs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

/**
 * Sent server→client in response to {@link ml.mypals.carpetgui.network.client.RequestRuleStackPayload}.
 * Contains the full active-prefab state required to render {@link ml.mypals.carpetgui.screen.ruleStackScreen.RuleStackScreen}.
 *
 * <p>Register in PacketIDs:
 * <pre>
 *   public static final ResourceLocation RULE_STACK_SYNC_ID =
 *       ResourceLocation.fromNamespaceAndPath(MOD_ID, "rule_stack_sync");
 * </pre>
 *
 * <p>Server-side handler (register in your server networking init):
 * <pre>{@code
 * ServerPlayNetworking.receive(RequestRuleStackPayload.ID, (payload, ctx) -> {
 *     PrefabManager mgr = CarpetGUI.getPrefabManager();
 *     if (mgr == null) return;
 *     Prefab active = mgr.getActivePrefab();
 *
 *     List<RuleStackSyncPayload.LayerInfo> layerInfos = active.getLayers().stream()
 *         .map(layer -> new RuleStackSyncPayload.LayerInfo(
 *             layer.getId(), layer.getMessage(), layer.getTimestamp(),
 *             layer.getChanges().stream()
 *                 .map(c -> new RuleStackSyncPayload.ChangeInfo(
 *                     c.ruleKey(),
 *                     c.previousSnapshot().getValue(), c.previousSnapshot().isDefault(),
 *                     c.newSnapshot().getValue(),      c.newSnapshot().isDefault()))
 *                 .toList()))
 *         .toList();
 *
 *     List<RuleStackSyncPayload.ChangeInfo> pending = mgr.getPendingChanges().stream()
 *         .map(c -> new RuleStackSyncPayload.ChangeInfo(
 *             c.ruleKey(),
 *             c.previousSnapshot().getValue(), c.previousSnapshot().isDefault(),
 *             c.newSnapshot().getValue(),      c.newSnapshot().isDefault()))
 *         .toList();
 *
 *     ctx.responseSender().sendPacket(new RuleStackSyncPayload(
 *         mgr.getActiveName(),
 *         mgr.getAllPrefabs().stream().map(Prefab::getName).toList(),
 *         layerInfos, pending));
 * });
 * }</pre>
 *
 * <p>Client-side handler:
 * <pre>{@code
 * ClientPlayNetworking.receive(RuleStackSyncPayload.ID, (payload, ctx) -> {
 *     CarpetGUIClient.cachedRuleStackData = payload;
 *     if (RuleStackScreen.instance != null) RuleStackScreen.instance.onDataReceived();
 * });
 * }</pre>
 *
 * <p>Also add to CarpetGUIClient:
 * <pre>
 *   public static RuleStackSyncPayload cachedRuleStackData = null;
 * </pre>
 */
public record RuleStackSyncPayload(
        String activePrefabName,
        List<String> allPrefabNames,
        List<RuleStackSyncPayload.LayerInfo> layers,
        List<RuleStackSyncPayload.ChangeInfo> pendingChanges
) implements CustomPacketPayload {

    public static final Type<RuleStackSyncPayload> ID =
            new Type<>(PacketIDs.RULE_STACK_SYNC_ID);

    public static final StreamCodec<FriendlyByteBuf, RuleStackSyncPayload> CODEC =
            StreamCodec.ofMember(RuleStackSyncPayload::write, RuleStackSyncPayload::new);

    /** Deserialization constructor used by {@link #CODEC}. */
    public RuleStackSyncPayload(FriendlyByteBuf buf) {
        this(
                buf.readUtf(),
                buf.readList(FriendlyByteBuf::readUtf),
                buf.readList(LayerInfo::read),
                buf.readList(ChangeInfo::read)
        );
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(activePrefabName);
        buf.writeCollection(allPrefabNames, FriendlyByteBuf::writeUtf);
        buf.writeCollection(layers, (b, l) -> l.write(b));
        buf.writeCollection(pendingChanges, (b, c) -> c.write(b));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    // ── Nested transfer objects ────────────────────────────────────────────────

    /**
     * A single rule change: ruleKey, previous state (value + isDefault), new state.
     * Mirrors {@code RuleChange} but decoupled from server-side types for safe packet transfer.
     */
    public record ChangeInfo(
            String ruleKey,
            String prevValue,  boolean prevIsDefault,
            String newValue,   boolean newIsDefault
    ) {
        /** Derived: the part after ':' in the ruleKey. */
        public String ruleName() {
            int i = ruleKey.indexOf(':');
            return i >= 0 ? ruleKey.substring(i + 1) : ruleKey;
        }

        /** Derived: the part before ':' in the ruleKey. */
        public String managerId() {
            int i = ruleKey.indexOf(':');
            return i >= 0 ? ruleKey.substring(0, i) : ruleKey;
        }

        public void write(FriendlyByteBuf buf) {
            buf.writeUtf(ruleKey);
            buf.writeUtf(prevValue);
            buf.writeBoolean(prevIsDefault);
            buf.writeUtf(newValue);
            buf.writeBoolean(newIsDefault);
        }

        public static ChangeInfo read(FriendlyByteBuf buf) {
            return new ChangeInfo(
                    buf.readUtf(),
                    buf.readUtf(), buf.readBoolean(),
                    buf.readUtf(), buf.readBoolean()
            );
        }
    }

    /** A committed layer: id, optional message, timestamp, and its list of changes. */
    public record LayerInfo(int id, String message, long timestamp, List<ChangeInfo> changes) {

        public void write(FriendlyByteBuf buf) {
            buf.writeInt(id);
            buf.writeUtf(message);
            buf.writeLong(timestamp);
            buf.writeCollection(changes, (b, c) -> c.write(b));
        }

        public static LayerInfo read(FriendlyByteBuf buf) {
            return new LayerInfo(
                    buf.readInt(),
                    buf.readUtf(),
                    buf.readLong(),
                    buf.readList(ChangeInfo::read)
            );
        }
    }
}