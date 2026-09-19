package ml.mypals.carpetgui.network.server;

import ml.mypals.carpetgui.network.PacketIDs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record HelloPacketPayload() implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<HelloPacketPayload> ID;
   public static final StreamCodec<FriendlyByteBuf, HelloPacketPayload> CODEC;

   public void write(FriendlyByteBuf buf) {
   }

   public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
      return ID;
   }

   static {
      ID = new CustomPacketPayload.Type(PacketIDs.HELLO_PACKET);
      CODEC = StreamCodec.ofMember(HelloPacketPayload::write, (buf) -> new HelloPacketPayload());
   }
}
