/*
 * This file is part of the Yet Another Carpet Addition project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2025  Ryan100c and contributors
 *
 * Yet Another Carpet Addition is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Yet Another Carpet Addition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Yet Another Carpet Addition.  If not, see <https://www.gnu.org/licenses/>.
 */

package ml.mypals.carpetgui.network.server;

import ml.mypals.carpetgui.network.PacketIDs;
import ml.mypals.carpetgui.network.RuleData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;

public record RulesPacketPayload(List<RuleData> rules, String defaults) implements CustomPacketPayload {

    public static final Type<RulesPacketPayload> ID = new Type<>(PacketIDs.SYNC_RULES_ID);
    public static final StreamCodec<FriendlyByteBuf, RulesPacketPayload> CODEC = StreamCodec.ofMember(RulesPacketPayload::write, RulesPacketPayload::new);

    public RulesPacketPayload(FriendlyByteBuf buf) {
        this(buf.readList(RuleData::new), buf.readUtf());
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeCollection(this.rules(), ((buf1, value) -> value.write(buf1)));
        buf.writeUtf(this.defaults);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

}
