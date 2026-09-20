plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom") version "1.15.3" apply false
    id("net.fabricmc.fabric-loom-remap") version "1.15.3" apply false
    // id("me.modmuss50.mod-publish-plugin") version "1.0.+" apply false
}

stonecutter active "26.2"

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
    }
}
