package ml.mypals.carpetgui.network.client;

import java.util.List;
import ml.mypals.carpetgui.network.PacketIDs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record RequestRulesPayload(String lang, List<String> knownRuleNames) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<RequestRulesPayload> ID;
   public static final StreamCodec<FriendlyByteBuf, RequestRulesPayload> CODEC;

   public RequestRulesPayload(FriendlyByteBuf buf) {
      this(buf.readUtf(), buf.readList(FriendlyByteBuf::readUtf));
   }

   public void write(FriendlyByteBuf buf) {
      buf.writeUtf(this.lang);
      buf.writeCollection(this.knownRuleNames, FriendlyByteBuf::writeUtf);
   }

   public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
      return ID;
   }

   static {
      ID = new CustomPacketPayload.Type(PacketIDs.REQUEST_RULES_ID);
      CODEC = StreamCodec.ofMember(RequestRulesPayload::write, RequestRulesPayload::new);
   }
}
