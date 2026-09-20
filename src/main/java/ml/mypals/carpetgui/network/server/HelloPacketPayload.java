package ml.mypals.carpetgui.network.server;

import ml.mypals.carpetgui.network.PacketIDs;
import net.minecraft.network.FriendlyByteBuf;

//? if >= 1.20.5 {
/*import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record HelloPacketPayload() implements CustomPacketPayload {
    public static final Type<HelloPacketPayload> ID = new Type<>(PacketIDs.HELLO_PACKET);
    public static final StreamCodec<FriendlyByteBuf, HelloPacketPayload> CODEC = StreamCodec.ofMember(HelloPacketPayload::write, buf -> new HelloPacketPayload());

    public void write(FriendlyByteBuf buf) {}

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
*///?} elif >= 1.19.4 {
/*import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;

public record HelloPacketPayload() implements FabricPacket {
    public static final PacketType<HelloPacketPayload> ID = PacketType.create(PacketIDs.HELLO_PACKET, buf -> new HelloPacketPayload());

    public void write(FriendlyByteBuf buf) {}

    @Override
    public PacketType<?> getType() {
        return ID;
    }
}
*///?} else {
import net.minecraft.resources.ResourceLocation;

public record HelloPacketPayload() {
    public static final ResourceLocation ID = PacketIDs.HELLO_PACKET;

    public void write(FriendlyByteBuf buf) {}

    public static HelloPacketPayload read(FriendlyByteBuf buf) {
        return new HelloPacketPayload();
    }
}
//?}
