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
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.NotNull;


public record HelloPacketPayload() implements CustomPacketPayload {
    public static final Type<HelloPacketPayload> ID = new Type<>(PacketIDs.HELLO_PACKET);
    public static final StreamCodec<FriendlyByteBuf, HelloPacketPayload> CODEC =
            StreamCodec.ofMember(HelloPacketPayload::write, HelloPacketPayload::new);

    public HelloPacketPayload(FriendlyByteBuf buf) {
        this();
    }

    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return ID;
    }

}