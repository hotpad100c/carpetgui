package ml.mypals.carpetgui.network.client;

import ml.mypals.carpetgui.network.PacketIDs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

public record RequestRuleStackPayload() implements CustomPacketPayload {

    public static final Type<RequestRuleStackPayload> ID =
            new Type<>(PacketIDs.REQUEST_RULE_STACK_ID);

    public static final StreamCodec<FriendlyByteBuf, RequestRuleStackPayload> CODEC =
            StreamCodec.ofMember(RequestRuleStackPayload::write, b -> new RequestRuleStackPayload());

    public void write(FriendlyByteBuf buf) { /* no payload */ }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}