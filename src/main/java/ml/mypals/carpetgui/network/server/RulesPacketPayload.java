package ml.mypals.carpetgui.network.server;

import ml.mypals.carpetgui.network.PacketIDs;
import ml.mypals.carpetgui.network.RuleData;
import net.minecraft.network.FriendlyByteBuf;
import java.util.List;

//? if >= 1.20.5 {

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record RulesPacketPayload(List<RuleData> rules, String defaults, boolean isPartial) implements CustomPacketPayload {

    public static final Type<RulesPacketPayload> ID = new Type<>(PacketIDs.SYNC_RULES_ID);
    public static final StreamCodec<FriendlyByteBuf, RulesPacketPayload> CODEC = StreamCodec.ofMember(RulesPacketPayload::write, RulesPacketPayload::new);

    public RulesPacketPayload(FriendlyByteBuf buf) {
        this(buf.readList(RuleData::new), buf.readUtf(), buf.readBoolean());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(this.rules(), ((buf1, value) -> value.write(buf1)));
        buf.writeUtf(this.defaults);
        buf.writeBoolean(this.isPartial);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
//?} else {
/*import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;

public record RulesPacketPayload(List<RuleData> rules, String defaults, boolean isPartial) implements FabricPacket {

    public static final PacketType<RulesPacketPayload> ID = PacketType.create(
            PacketIDs.SYNC_RULES_ID,
            buf -> new RulesPacketPayload(buf.readList(RuleData::new), buf.readUtf(), buf.readBoolean())
    );

    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(this.rules, (b, v) -> v.write(b));
        buf.writeUtf(this.defaults);
        buf.writeBoolean(this.isPartial);
    }

    @Override
    public PacketType<?> getType() {
        return ID;
    }
}
*///?}