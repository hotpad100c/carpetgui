package ml.mypals.carpetgui.network.client;

import ml.mypals.carpetgui.network.PacketIDs;
import net.minecraft.network.FriendlyByteBuf;
import java.util.List;

//? if >= 1.20.5 {
/*import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record RequestRulesPayload(String lang, List<String> knownRuleNames) implements CustomPacketPayload {

    public static final Type<RequestRulesPayload> ID = new Type<>(PacketIDs.REQUEST_RULES_ID);
    public static final StreamCodec<FriendlyByteBuf, RequestRulesPayload> CODEC
            = StreamCodec.ofMember(RequestRulesPayload::write, RequestRulesPayload::new);

    public RequestRulesPayload(FriendlyByteBuf buf) {
        this(buf.readUtf(), buf.readList(FriendlyByteBuf::readUtf));
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.lang);
        buf.writeCollection(this.knownRuleNames, FriendlyByteBuf::writeUtf);
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
*///?} elif >= 1.19.4 {
/*import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;

public record RequestRulesPayload(String lang, List<String> knownRuleNames) implements FabricPacket {

    public static final PacketType<RequestRulesPayload> ID = PacketType.create(
            PacketIDs.REQUEST_RULES_ID,
            buf -> new RequestRulesPayload(buf.readUtf(), buf.readList(FriendlyByteBuf::readUtf))
    );

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.lang);
        buf.writeCollection(this.knownRuleNames, FriendlyByteBuf::writeUtf);
    }

    @Override
    public PacketType<?> getType() {
        return ID;
    }
}
*///?} else {
import net.minecraft.resources.ResourceLocation;

public record RequestRulesPayload(String lang, List<String> knownRuleNames) {
    public static final ResourceLocation ID = PacketIDs.REQUEST_RULES_ID;

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.lang);
        ml.mypals.carpetgui.network.BufUtils.writeCollection(buf, this.knownRuleNames, FriendlyByteBuf::writeUtf);
    }

    public static RequestRulesPayload read(FriendlyByteBuf buf) {
        return new RequestRulesPayload(buf.readUtf(), ml.mypals.carpetgui.network.BufUtils.readList(buf, FriendlyByteBuf::readUtf));
    }
}
//?}
