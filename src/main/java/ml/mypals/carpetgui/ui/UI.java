package ml.mypals.carpetgui.ui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UI {
   public static final String NAMESPACE = "carpetgui";
   public static final Logger LOGGER = LoggerFactory.getLogger("carpetgui/ui");
   public static final boolean DEBUG;

   private UI() {
   }

   public static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath("carpetgui", path);
   }

   public static void debugWarn(Logger logger, String message) {
      if (DEBUG) {
         logger.warn(message);
      }
   }

   public static void debugWarn(Logger logger, String message, Object... params) {
      if (DEBUG) {
         logger.warn(message, params);
      }
   }

   public static final class Sounds {
      //? if <1.19 {
      /*public static final SoundEvent UI_INTERACTION = new SoundEvent(UI.id("ui.carpetgui.interaction"));
      *///?} else {
      public static final SoundEvent UI_INTERACTION = SoundEvent.createVariableRangeEvent(UI.id("ui.carpetgui.interaction"));
      //?}

      private Sounds() {
      }

      @Environment(EnvType.CLIENT)
      public static void play(SoundEvent event) {
         Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, 1.0F));
      }

      @Environment(EnvType.CLIENT)
      public static void playButtonSound() {
         //? if <1.19 {
         /*play(SoundEvents.UI_BUTTON_CLICK);
         *///?} else {
         play((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value());
         //?}
      }

      @Environment(EnvType.CLIENT)
      public static void playInteractionSound() {
         play(UI_INTERACTION);
      }
   }

   static {
      boolean debug = FabricLoader.getInstance().isDevelopmentEnvironment();
      if (System.getProperty("carpetgui.ui.debug") != null) {
         debug = Boolean.getBoolean("carpetgui.ui.debug");
      }

      DEBUG = debug;
   }
}
