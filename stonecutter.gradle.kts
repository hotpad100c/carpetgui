plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom") version "1.15.3" apply false
    id("net.fabricmc.fabric-loom-remap") version "1.15.3" apply false
    // id("me.modmuss50.mod-publish-plugin") version "1.0.+" apply false
}

stonecutter active "1.16.5"

stonecutter tasks {
    order("runClient")
}

// See https://stonecutter.kikugie.dev/wiki/config/params
stonecutter parameters {
    swaps["mod_version"] = "\"" + property("mod.version") + "\";"
    swaps["minecraft"] = "\"" + node.metadata.version + "\";"
    constants["release"] = property("mod.id") != "template"
    dependencies["fapi"] = node.project.property("deps.fabric_api") as String
    replacements {
        string(eval(current.version, "<1.21.11")) {
            replace("Identifier.fromNamespaceAndPath", "ResourceLocation.fromNamespaceAndPath")
        }

        string(eval(current.version, "<1.21.1")) {
            replace("ResourceLocation.fromNamespaceAndPath", "ResourceLocation.tryBuild")
        }
        string(eval(current.version, "<1.19.4")) {
            replace("ResourceLocation.tryBuild", "new ResourceLocation")
        }

        string(eval(current.version, "<1.21.11")) {
            replace("Identifier", "ResourceLocation")
        }

        string(eval(current.version, "<1.21.9")) {
            replace("setScreenAndShow(", "setScreen(")
        }
        string(eval(current.version, "<1.21.9")) {
            replace(".getWindow().handle()", ".getWindow().getWindow()")
        }
        string(eval(current.version, "<1.21.9")) {
            replace("window.handle()", "window.getWindow()")
        }
        string(eval(current.version, "<1.21.9")) {
            replace("InputConstants.isKeyDown(this.minecraft.getWindow(),", "InputConstants.isKeyDown(this.minecraft.getWindow().getWindow(),")
        }

        string(eval(current.version, "<1.18")) {
            replace("org.slf4j.LoggerFactory", "org.apache.logging.log4j.LogManager")
        }
        string(eval(current.version, "<1.18")) {
            replace("org.slf4j.Logger", "org.apache.logging.log4j.Logger")
        }
        string(eval(current.version, "<1.18")) {
            replace("LoggerFactory.getLogger", "LogManager.getLogger")
        }

        string(eval(current.version, "<1.17")) {
            replace("net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent", "ml.mypals.carpetgui.compat.tooltip.ClientTooltipComponent")
        }

        string(eval(current.version, "<1.17")) {
            replace("buf.writeCollection(", "ml.mypals.carpetgui.network.BufUtils.writeList(buf, ")
        }
        string(eval(current.version, "<1.17")) {
            replace("buf.readList(", "ml.mypals.carpetgui.network.BufUtils.readListSafe(buf, ")
        }


        /*
        string(eval(current.version, "<1.21.11")) {
            replace("UIContainers.horizontalFlow", "Containers.horizontalFlow")
        }
        string(eval(current.version, "<1.21.11")) {
            replace("UIContainers.verticalFlow", "Containers.verticalFlow")
        }
        string(eval(current.version, "<1.21.11")) {
            replace("UIContainers::horizontalFlow", "Containers::horizontalFlow")
        }
        string(eval(current.version, "<1.21.11")) {
            replace("UIContainers::verticalFlow", "Containers::verticalFlow")
        }
        string(eval(current.version, "<1.21.11")) {
            replace("UIComponents.label", "Components.label")
        }
        string(eval(current.version, "<1.21.11")) {
            replace("UIComponents.textBox", "Components.textBox")
        }*/

        string(eval(current.version, "<1.21.11")) {
            replace("import net.minecraft.util.Util", "import net.minecraft.Util")
        }
        string(eval(current.version, "<26.1")) {
            replace("PayloadTypeRegistry.serverboundPlay()", "PayloadTypeRegistry.playC2S()")
        }
        string(eval(current.version, "<26.1")) {
            replace("PayloadTypeRegistry.clientboundPlay()", "PayloadTypeRegistry.playS2C()")
        }
        string(eval(current.version, ">=26.3")) {
            replace("buf.writeCollection(", "ml.mypals.carpetgui.network.BufUtils.writeCollection(buf, ")
        }
        string(eval(current.version, ">=26.3")) {
            replace("buf.readList(", "ml.mypals.carpetgui.network.BufUtils.readList(buf, ")
        }
        string(eval(current.version, ">=26.3")) {
            replace("com.mojang.blaze3d.pipeline.RenderPipeline", "com.mojang.renderpearl.api.pipeline.RenderPipeline")
        }
        string(eval(current.version, "<1.19")) {
            replace("Component.translatable(", "new net.minecraft.network.chat.TranslatableComponent(")
        }
        string(eval(current.version, "<1.19")) {
            replace("Component.literal(", "new net.minecraft.network.chat.TextComponent(")
        }
        string(eval(current.version, "<1.19")) {
            replace("Component.empty()", "net.minecraft.network.chat.TextComponent.EMPTY")
        }
        string(eval(current.version, ">=26.3")) {
            replace("Util.getPlatform().openFile(RuleGroupLoader.GROUPS_DIR.toFile())", "com.mojang.blaze3d.Blaze3D.openPath(RuleGroupLoader.GROUPS_DIR)")
        }
    }
}
