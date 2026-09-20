package ml.mypals.carpetgui.network.client;

import ml.mypals.carpetgui.network.PacketIDs;
import net.minecraft.network.FriendlyByteBuf;

//? if >= 1.20.5 {
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record RequestRuleStackPayload() implements CustomPacketPayload {

    public static final Type<RequestRuleStackPayload> ID =
            new Type<>(PacketIDs.REQUEST_RULE_STACK_ID);

    public static final StreamCodec<FriendlyByteBuf, RequestRuleStackPayload> CODEC =
            StreamCodec.ofMember(RequestRuleStackPayload::write, b -> new RequestRuleStackPayload());

    public void write(FriendlyByteBuf buf) {}

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }

}
//?} else {
/*import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;

public record RequestRuleStackPayload() implements FabricPacket {

    public static final PacketType<RequestRuleStackPayload> ID = PacketType.create(PacketIDs.REQUEST_RULE_STACK_ID,
            byteBuf -> new RequestRuleStackPayload());

    public void write(FriendlyByteBuf buf) { }

    @Override
    public PacketType<?> getType() {
        return ID;
    }
}
*///?}
