package ml.mypals.carpetgui.network.server;

import ml.mypals.carpetgui.network.PacketIDs;
import net.minecraft.network.FriendlyByteBuf;
import java.util.List;

//? if >= 1.20.5 {
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record RuleStackSyncPayload(
        String activePrefabName,
        List<String> allPrefabNames,
        List<LayerInfo> layers,
        List<ChangeInfo> pendingChanges,
        List<LayerInfo> futureLayers
) implements CustomPacketPayload {

    public static final Type<RuleStackSyncPayload> ID = new Type<>(PacketIDs.RULE_STACK_SYNC_ID);
    public static final StreamCodec<FriendlyByteBuf, RuleStackSyncPayload> CODEC = StreamCodec.ofMember(RuleStackSyncPayload::write, RuleStackSyncPayload::new);

    public RuleStackSyncPayload(FriendlyByteBuf buf) {
        this(buf.readUtf(), ml.mypals.carpetgui.network.BufUtils.readList(buf, FriendlyByteBuf::readUtf), ml.mypals.carpetgui.network.BufUtils.readList(buf, LayerInfo::read), ml.mypals.carpetgui.network.BufUtils.readList(buf, ChangeInfo::read), ml.mypals.carpetgui.network.BufUtils.readList(buf, LayerInfo::read));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(activePrefabName);
        ml.mypals.carpetgui.network.BufUtils.writeCollection(buf, allPrefabNames, FriendlyByteBuf::writeUtf);
        ml.mypals.carpetgui.network.BufUtils.writeCollection(buf, layers, (b, l) -> l.write(b));
        ml.mypals.carpetgui.network.BufUtils.writeCollection(buf, pendingChanges, (b, c) -> c.write(b));
        ml.mypals.carpetgui.network.BufUtils.writeCollection(buf, futureLayers, (b, l) -> l.write(b));
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() { return ID; }
//?} elif >= 1.19.4 {
/*import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;

public record RuleStackSyncPayload(
        String activePrefabName,
        List<String> allPrefabNames,
        List<LayerInfo> layers,
        List<ChangeInfo> pendingChanges,
        List<LayerInfo> futureLayers
) implements FabricPacket {

    public static final PacketType<RuleStackSyncPayload> ID = PacketType.create(
            PacketIDs.RULE_STACK_SYNC_ID,
            buf -> new RuleStackSyncPayload(buf.readUtf(), ml.mypals.carpetgui.network.BufUtils.readList(buf, FriendlyByteBuf::readUtf), ml.mypals.carpetgui.network.BufUtils.readList(buf, LayerInfo::read), ml.mypals.carpetgui.network.BufUtils.readList(buf, ChangeInfo::read), ml.mypals.carpetgui.network.BufUtils.readList(buf, LayerInfo::read))
    );

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(activePrefabName);
        ml.mypals.carpetgui.network.BufUtils.writeCollection(buf, allPrefabNames, FriendlyByteBuf::writeUtf);
        ml.mypals.carpetgui.network.BufUtils.writeCollection(buf, layers, (b, l) -> l.write(b));
        ml.mypals.carpetgui.network.BufUtils.writeCollection(buf, pendingChanges, (b, c) -> c.write(b));
        ml.mypals.carpetgui.network.BufUtils.writeCollection(buf, futureLayers, (b, l) -> l.write(b));
    }

    @Override
    public PacketType<?> getType() { return ID; }
*///?} else {
/*import net.minecraft.resources.Identifier;

public record RuleStackSyncPayload(
        String activePrefabName,
        List<String> allPrefabNames,
        List<LayerInfo> layers,
        List<ChangeInfo> pendingChanges,
        List<LayerInfo> futureLayers
) {
    public static final Identifier ID = PacketIDs.RULE_STACK_SYNC_ID;

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(activePrefabName);
        ml.mypals.carpetgui.network.BufUtils.writeCollection(buf, allPrefabNames, FriendlyByteBuf::writeUtf);
        ml.mypals.carpetgui.network.BufUtils.writeCollection(buf, layers, (b, l) -> l.write(b));
        ml.mypals.carpetgui.network.BufUtils.writeCollection(buf, pendingChanges, (b, c) -> c.write(b));
        ml.mypals.carpetgui.network.BufUtils.writeCollection(buf, futureLayers, (b, l) -> l.write(b));
    }

    public static RuleStackSyncPayload read(FriendlyByteBuf buf) {
        return new RuleStackSyncPayload(buf.readUtf(), ml.mypals.carpetgui.network.BufUtils.readList(buf, FriendlyByteBuf::readUtf), ml.mypals.carpetgui.network.BufUtils.readList(buf, LayerInfo::read), ml.mypals.carpetgui.network.BufUtils.readList(buf, ChangeInfo::read), ml.mypals.carpetgui.network.BufUtils.readList(buf, LayerInfo::read));
    }
*///?}

    public record ChangeInfo(String ruleKey, String prevValue, boolean prevIsDefault, String newValue, boolean newIsDefault) {
        public void write(FriendlyByteBuf buf) {
            buf.writeUtf(ruleKey); buf.writeUtf(prevValue); buf.writeBoolean(prevIsDefault);
            buf.writeUtf(newValue); buf.writeBoolean(newIsDefault);
        }
        public static ChangeInfo read(FriendlyByteBuf buf) {
            return new ChangeInfo(buf.readUtf(), buf.readUtf(), buf.readBoolean(), buf.readUtf(), buf.readBoolean());
        }
        public String ruleName() {
            int i = ruleKey.indexOf(':');
            return i >= 0 ? ruleKey.substring(i + 1) : ruleKey;
        }

        public String managerId() {
            int i = ruleKey.indexOf(':');
            return i >= 0 ? ruleKey.substring(0, i) : ruleKey;
        }
    }

    public record LayerInfo(int id, String message, long timestamp, List<ChangeInfo> changes) {
        public void write(FriendlyByteBuf buf) {
            buf.writeInt(id); buf.writeUtf(message); buf.writeLong(timestamp);
            ml.mypals.carpetgui.network.BufUtils.writeCollection(buf, changes, (b, c) -> c.write(b));
        }
        public static LayerInfo read(FriendlyByteBuf buf) {
            return new LayerInfo(buf.readInt(), buf.readUtf(), buf.readLong(), ml.mypals.carpetgui.network.BufUtils.readList(buf, ChangeInfo::read));
        }
    }
}
