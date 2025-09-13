package top.alazeprt.aqqbot.scripts

import com.google.gson.Gson
import com.google.gson.JsonObject
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.profile.ASender
import top.alazeprt.aqqbot.scripts.ScriptLoader.Companion.version
import top.alazeprt.aqqbot.util.ACompressUtil
import top.alazeprt.aqqbot.util.ARemoteUtil
import top.alazeprt.aqqbot.util.LogLevel
import java.util.UUID
import javax.script.Compilable
import kotlin.io.resolve
import kotlin.math.log

class ScriptManager(val plugin: AQQBot) {
    fun list(sender: ASender) {
        plugin.scriptLoader.pluginList.forEach {
            sender.sendMessage(Component.text("- 插件名称: ${it.name}, 作者: ${it.author}\n").color(NamedTextColor.AQUA))
            sender.sendMessage(Component.text("> ${it.version} (模板版本: ${it.schemaVersion})").color(NamedTextColor.GOLD))
        }
    }

    fun info(sender: ASender, name: String) {
        plugin.scriptLoader.pluginList.forEach {
            if (it.name == name) {
                val component = Component.text()
                    .append(Component.text("插件名称: ${it.name}\n版本: ${it.version}\n作者: ${it.author}\n").color(NamedTextColor.AQUA))
                if (it.description != null) {
                    component.append(Component.text("介绍: ${it.description}\n").color(NamedTextColor.GREEN))
                }
                component.append(Component.text("模板版本: ${it.schemaVersion}").color(
                    NamedTextColor.GOLD))
                sender.sendMessage(component.build())
                return
            }
        }
        sender.sendMessage(Component.text("[AQQBot] 没有找到匹配的插件!", NamedTextColor.RED))
    }

    fun load(sender: ASender, dir: String) {
        sender.sendMessage(Component.text("[AQQBot] 尝试加载插件 $dir ...", NamedTextColor.GREEN))
        if (plugin.getDataFolder().resolve("plugins").isDirectory) {
            plugin.getDataFolder().resolve("plugins").listFiles { file -> file.isDirectory && file.name.equals(dir) }.forEach {
                plugin.log(LogLevel.INFO, "Reading the information of plugin directory $dir")
                val manifest = it.resolve("manifest.json")
                if (!manifest.isFile) return@forEach
                val manifestContent = Gson().fromJson(manifest.readText(), JsonObject::class.java)
                val schemaVersion = manifestContent.get("schema_version").asInt
                if (schemaVersion > version) {
                    sender.sendMessage(Component.text("[AQQBot] 你的插件暂不支持该模板版本 ($schemaVersion) 的脚本 (支持: <= $version)").color(NamedTextColor.RED))
                    plugin.log(LogLevel.WARN, "Your plugin doesn't support the script schema version $schemaVersion (supported version is <= $version), please update your plugin!")
                    return@forEach
                }
                val name = if (manifestContent.has("name")) manifestContent.get("name").asString else dir
                val author = if (manifestContent.has("author")) manifestContent.get("author").asString else null
                val version = if (manifestContent.has("version")) manifestContent.get("version").asString else null
                val description = if (manifestContent.has("description")) manifestContent.get("description").asString else null
                val scriptFile = it.resolve(manifestContent.get("entrypoint").asString)
                if (!scriptFile.isFile) {
                    sender.sendMessage(Component.text("[AQQBot] 未找到入口初始化 JavaScript 文件 ${scriptFile.name}!").color(NamedTextColor.RED))
                    return@forEach
                }
                val script = ScriptPlugin(schemaVersion, name, author, version, description, dir, manifestContent.get("entrypoint").asString)
                plugin.scriptLoader.pluginList.add(script)
                plugin.log(LogLevel.INFO, "Loading plugin $name version $version by $author")
                val compiled = (plugin.scriptLoader.engine as Compilable).compile(scriptFile.readText())
                compiled.eval()
            }
        }
    }

    fun reload(sender: ASender) {
        sender.sendMessage(Component.text("[AQQBot] 重载 JavaScript 脚本插件中 ...").color(NamedTextColor.GREEN))
        plugin.scriptLoader.unload(true)
        plugin.scriptLoader.load()
        sender.sendMessage(Component.text("[AQQBot] 重载完成!").color(NamedTextColor.GREEN))
    }

    fun download(sender: ASender, url: String) {
        sender.sendMessage(Component.text("[AQQBot] 尝试从 $url 下载脚本 ...").color(NamedTextColor.GREEN))
        val scriptFolder = plugin.getDataFolder().resolve("plugins")
        val tmpFile = scriptFolder.resolve(UUID.randomUUID().toString() + ".zip")
        plugin.log(LogLevel.DEBUG, "Downloading plugin from $url to $tmpFile")
        ARemoteUtil.downloadToFile(url, tmpFile)
        sender.sendMessage(Component.text("[AQQBot] 解压文件中 ...").color(NamedTextColor.GREEN))
        plugin.log(LogLevel.DEBUG, "Decompressing plugin $tmpFile")
        ACompressUtil.unzip(tmpFile, scriptFolder)
        sender.sendMessage(Component.text("[AQQBot] 尝试加载所有 plugins 文件夹内未加载的插件 ...").color(NamedTextColor.GREEN))
        val originList = plugin.scriptLoader.pluginList.map { it.directory }
        plugin.getDataFolder().resolve("plugins").listFiles { file -> file.isDirectory }.forEach {
            if (!originList.contains(it.name)) {
                load(sender, it.name)
            }
        }
        sender.sendMessage(Component.text("[AQQBot] 加载完成!").color(NamedTextColor.GREEN))
    }

}