package ml.mypals.carpetgui.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;

public final class BufUtils {
    private BufUtils() {}

    public static <T, B extends FriendlyByteBuf> void writeCollection(B buf, Collection<T> collection, BiConsumer<B, T> encoder) {
        buf.writeVarInt(collection.size());
        for (T item : collection) {
            encoder.accept(buf, item);
        }
    }

    public static <T, B extends FriendlyByteBuf> List<T> readList(B buf, Function<B, T> decoder) {
        int size = buf.readVarInt();
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(decoder.apply(buf));
        }
        return list;
    }
}
