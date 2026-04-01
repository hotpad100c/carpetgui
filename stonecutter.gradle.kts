plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom") version "1.15.3" apply false
    id("net.fabricmc.fabric-loom-remap") version "1.15.3" apply false
    // id("me.modmuss50.mod-publish-plugin") version "1.0.+" apply false
}

stonecutter active "1.21.11"

/*
// Make newer versions be published last
stonecutter tasks {
    order("publishModrinth")
    order("publishCurseforge")
}
 */

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

        string(eval(current.version, "<1.21.11")) {
            replace("component.UIComponents", "component.Components")
        }
        string(eval(current.version, "<1.21.11")) {
            replace("container.UIContainers", "container.Containers")
        }
        string(eval(current.version, "<1.21.11")) {
            replace("BaseUIComponent", "BaseComponent")
        }
        string(eval(current.version, "<1.21.11")) {
            replace("OwoUIGraphics", "OwoUIDrawContext")
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
    }
}
