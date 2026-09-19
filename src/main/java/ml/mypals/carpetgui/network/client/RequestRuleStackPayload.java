package ml.mypals.carpetgui.network.client;

import ml.mypals.carpetgui.network.PacketIDs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record RequestRuleStackPayload() implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<RequestRuleStackPayload> ID;
   public static final StreamCodec<FriendlyByteBuf, RequestRuleStackPayload> CODEC;

   public void write(FriendlyByteBuf buf) {
   }

   public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
      return ID;
   }

   static {
      ID = new CustomPacketPayload.Type(PacketIDs.REQUEST_RULE_STACK_ID);
      CODEC = StreamCodec.ofMember(RequestRuleStackPayload::write, (b) -> new RequestRuleStackPayload());
   }
}
