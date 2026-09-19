package ml.mypals.carpetgui.ui.event;

import com.mojang.blaze3d.platform.Window;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.Minecraft;

public interface WindowResizeCallback {
   Event<WindowResizeCallback> EVENT = EventFactory.createArrayBacked(WindowResizeCallback.class, (callbacks) -> (client, window) -> {
         for(WindowResizeCallback callback : callbacks) {
            callback.onResized(client, window);
         }

      });

   void onResized(Minecraft var1, Window var2);
}
