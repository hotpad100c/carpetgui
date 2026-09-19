package ml.mypals.carpetgui.network.server;

import java.util.List;
import ml.mypals.carpetgui.network.PacketIDs;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;

public record RuleStackSyncPayload(String activePrefabName, List<String> allPrefabNames, List<LayerInfo> layers, List<ChangeInfo> pendingChanges, List<LayerInfo> futureLayers) implements CustomPacketPayload {
   public static final CustomPacketPayload.Type<RuleStackSyncPayload> ID;
   public static final StreamCodec<FriendlyByteBuf, RuleStackSyncPayload> CODEC;

   public RuleStackSyncPayload(FriendlyByteBuf buf) {
      this(buf.readUtf(), buf.readList(FriendlyByteBuf::readUtf), buf.readList(LayerInfo::read), buf.readList(ChangeInfo::read), buf.readList(LayerInfo::read));
   }

   public void write(FriendlyByteBuf buf) {
      buf.writeUtf(this.activePrefabName);
      buf.writeCollection(this.allPrefabNames, FriendlyByteBuf::writeUtf);
      buf.writeCollection(this.layers, (b, l) -> l.write(b));
      buf.writeCollection(this.pendingChanges, (b, c) -> c.write(b));
      buf.writeCollection(this.futureLayers, (b, l) -> l.write(b));
   }

   public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
      return ID;
   }

   static {
      ID = new CustomPacketPayload.Type(PacketIDs.RULE_STACK_SYNC_ID);
      CODEC = StreamCodec.ofMember(RuleStackSyncPayload::write, RuleStackSyncPayload::new);
   }

   public static record ChangeInfo(String ruleKey, String prevValue, boolean prevIsDefault, String newValue, boolean newIsDefault) {
      public void write(FriendlyByteBuf buf) {
         buf.writeUtf(this.ruleKey);
         buf.writeUtf(this.prevValue);
         buf.writeBoolean(this.prevIsDefault);
         buf.writeUtf(this.newValue);
         buf.writeBoolean(this.newIsDefault);
      }

      public static ChangeInfo read(FriendlyByteBuf buf) {
         return new ChangeInfo(buf.readUtf(), buf.readUtf(), buf.readBoolean(), buf.readUtf(), buf.readBoolean());
      }

      public String ruleName() {
         int i = this.ruleKey.indexOf(58);
         return i >= 0 ? this.ruleKey.substring(i + 1) : this.ruleKey;
      }

      public String managerId() {
         int i = this.ruleKey.indexOf(58);
         return i >= 0 ? this.ruleKey.substring(0, i) : this.ruleKey;
      }
   }

   public static record LayerInfo(int id, String message, long timestamp, List<ChangeInfo> changes) {
      public void write(FriendlyByteBuf buf) {
         buf.writeInt(this.id);
         buf.writeUtf(this.message);
         buf.writeLong(this.timestamp);
         buf.writeCollection(this.changes, (b, c) -> c.write(b));
      }

      public static LayerInfo read(FriendlyByteBuf buf) {
         return new LayerInfo(buf.readInt(), buf.readUtf(), buf.readLong(), buf.readList(ChangeInfo::read));
      }
   }
}
