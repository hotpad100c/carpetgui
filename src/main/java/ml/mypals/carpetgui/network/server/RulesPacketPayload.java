package ml.mypals.carpetgui.network.server;

import java.util.List;
import ml.mypals.carpetgui.network.PacketIDs;
import ml.mypals.carpetgui.network.RuleData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record RulesPacketPayload(List<RuleData> rules, String defaults, boolean isPartial) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<RulesPacketPayload> ID;
   public static final StreamCodec<FriendlyByteBuf, RulesPacketPayload> CODEC;

   public RulesPacketPayload(FriendlyByteBuf buf) {
      this(buf.readList(RuleData::new), buf.readUtf(), buf.readBoolean());
   }

   public void write(FriendlyByteBuf buf) {
      buf.writeCollection(this.rules(), (buf1, value) -> value.write(buf1));
      buf.writeUtf(this.defaults);
      buf.writeBoolean(this.isPartial);
   }

   public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
      return ID;
   }

   static {
      ID = new CustomPacketPayload.Type(PacketIDs.SYNC_RULES_ID);
      CODEC = StreamCodec.ofMember(RulesPacketPayload::write, RulesPacketPayload::new);
   }
}
