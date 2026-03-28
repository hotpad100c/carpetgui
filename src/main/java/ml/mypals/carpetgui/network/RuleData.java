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

package ml.mypals.carpetgui.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
//#endif

public class RuleData {
    public String manager;
    public String name;
    public String localName;
    public String defaultValue;
    public String value;
    public String description;
    public String localDescription;
    public Class<?> type;
    public List<String> suggestions;
    public List<Map.Entry<String,String>> categories;
    public boolean isGamerule = false;
    public RuleData(){
        this.manager = "";
        this.name = "";
        this.localName = "";
        this.defaultValue = "";
        this.value = "";
        this.description = "";
        this.localDescription = "";
        this.type = getClass();
        this.suggestions = List.of();
        this.categories = List.of();
    }
    public RuleData(String manager, String name,String localName, Class<?> type, String defaultValue, String value, String description, String localDescription, List<String> suggestions, List<Map.Entry<String,String>>  categories) {
        this.manager = manager;
        this.name = name;
        this.localName = localName;
        this.defaultValue = defaultValue;
        this.value = value;
        this.description = description;
        this.localDescription = localDescription;
        this.type = type;
        this.suggestions = suggestions;
        this.categories = categories;
    }

    public static final StreamCodec<FriendlyByteBuf, RuleData> CODEC = StreamCodec.ofMember(RuleData::write, RuleData::new);


    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(this.manager);

        buf.writeUtf(this.name);
        buf.writeUtf(this.localName);

        buf.writeUtf(this.type.toString());

        buf.writeUtf(this.defaultValue);
        buf.writeUtf(this.value);

        buf.writeUtf(this.description);
        buf.writeUtf(this.localDescription);

        buf.writeCollection(suggestions, FriendlyByteBuf::writeUtf);
        buf.writeCollection(categories, (bf,entry)->{
            bf.writeUtf(entry.getKey());
            bf.writeUtf(entry.getValue());
        });
    }

    public RuleData(FriendlyByteBuf buf) {
        this(
                buf.readUtf(),
                buf.readUtf(), // name
                buf.readUtf(), // localName
                getRuleType(buf.readUtf()), // type
                buf.readUtf(), // defaultValue
                buf.readUtf(), // value
                buf.readUtf(), //desc
                buf.readUtf(), //localDesc
                buf.readList(FriendlyByteBuf::readUtf), //suggestions
                buf.readList((bf)-> Map.entry(bf.readUtf(), bf.readUtf())) //categories
        );
        if(this.categories.getFirst().getKey().equals("gamerule")){
            isGamerule = true;
            this.localDescription = Component.translatable(localDescription).getString();
            String[] ct = this.categories.getFirst().getValue().split(" : ");
            this.categories = List.of(
                    Map.entry(
                            this.categories.getFirst().getKey(),
                            Component.translatable(ct[0]).getString()
                    ));
        }
    }

    public static Class<?> getRuleType(String name) {
        return switch (name) {
            case "Integer" -> Integer.class;
            case "Boolean" -> Boolean.class;
            case "Float" -> Float.class;
            case "Enum" -> Enum.class;
            default -> String.class;
        };
    }

}
