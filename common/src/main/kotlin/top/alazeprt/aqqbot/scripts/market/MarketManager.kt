package top.alazeprt.aqqbot.scripts.market

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.profile.ASender
import top.alazeprt.aqqbot.scripts.ScriptLoader
import top.alazeprt.aqqbot.scripts.ScriptPlugin
import top.alazeprt.aqqbot.util.LogLevel
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.max

class MarketManager(val plugin: AQQBot) {
    fun info(sender: ASender, findName: String, findVersion: String?) {
        sender.sendMessage(Component.text("[AQQBot] 正在获取市场信息...", NamedTextColor.GREEN))
        try {
            val pluginsArray = getPluginList()
            for (plugin in pluginsArray) {
                val pluginObj = plugin.asJsonObject
                val schemaVersion = pluginObj.get("schema_version").asInt
                val name = pluginObj.get("name").asString
                val author = pluginObj.get("author").asString
                val version = pluginObj.get("version").asString
                val description = if (pluginObj.has("description")) null else pluginObj.get("description").asString
                if (findName != name) continue
                if (findVersion != null && findVersion != version) continue
                val component = Component.text()
                    .append(Component.text("插件名称: $name\n版本: $version\n作者: $author\n").color(NamedTextColor.AQUA))
                if (description != null) {
                    component.append(Component.text("介绍: $description\n").color(NamedTextColor.GREEN))
                }
                component.append(Component.text("模板版本: $schemaVersion").color(
                    NamedTextColor.GOLD))
                if (schemaVersion > ScriptLoader.version) {
                    component.append(Component.text("(不支持, 请升级插件!)").style(Style.style(NamedTextColor.RED)))
                } else component.append(Component.text("(支持)").style(Style.style(NamedTextColor.GREEN)))
                sender.sendMessage(component.build())
                return
            }
            sender.sendMessage(Component.text("[AQQBot] 没有找到匹配的插件!", NamedTextColor.RED))
        } catch (e: Exception) {
            sender.sendMessage(Component.text("[AQQBot] 获取市场信息失败, 原因: ${e.message}", NamedTextColor.RED))
            plugin.log(LogLevel.WARN, "Failed to get market info: $e")
            return
        }
    }

    fun list(sender: ASender) {
        sender.sendMessage(Component.text("[AQQBot] 当前插件支持的模板版本: <= ${ScriptLoader.version}").color(
            NamedTextColor.LIGHT_PURPLE))
        sender.sendMessage(Component.text("[AQQBot] 正在获取市场信息...", NamedTextColor.GREEN))
        try {
            val pluginsArray = getPluginList()
            val map = mutableMapOf<String, MutableList<String>>()
            for (plugin in pluginsArray) {
                val pluginObj = plugin.asJsonObject
                val schemaVersion = pluginObj.get("schema_version").asInt
                val name = pluginObj.get("name").asString
                val author = pluginObj.get("author").asString
                val version = pluginObj.get("version").asString
                val mapLeft = "- 插件名称: $name, 作者: $author\n"
                if (map.containsKey(mapLeft)) {
                    map[mapLeft]!!.add("> $version (模板版本: $schemaVersion)")
                } else {
                    map[mapLeft] = mutableListOf("> $version (模板版本: $schemaVersion)")
                }
            }
            for ((left, right) in map.entries) {
                sender.sendMessage(Component.text(left).color(NamedTextColor.AQUA))
                for (version in right) {
                    sender.sendMessage(Component.text(version).color(NamedTextColor.GOLD))
                }
            }
        } catch (e: Exception) {
            sender.sendMessage(Component.text("[AQQBot] 获取市场信息失败, 原因: ${e.message}", NamedTextColor.RED))
            plugin.log(LogLevel.WARN, "Failed to get market info: $e")
            return
        }
    }

    fun install(sender: ASender, findName: String, findVersion: String?) {
        sender.sendMessage(Component.text("[AQQBot] 正在获取市场信息...", NamedTextColor.GREEN))
        try {
            val pluginsArray = getPluginList()
            for (plugin in pluginsArray) {
                val pluginObj = plugin.asJsonObject
                val schemaVersion = pluginObj.get("schema_version").asInt
                val name = pluginObj.get("name").asString
                val version = pluginObj.get("version").asString
                if (findName != name) continue
                if (findVersion != null && findVersion != version) continue
                if (schemaVersion > ScriptLoader.version) continue
                val download = pluginObj.get("download").asString
                sender.sendMessage(Component.text("[AQQBot] 找到匹配的插件, 开始下载 ...", NamedTextColor.GREEN))
                this.plugin.scriptLoader.scriptManager.download(sender, download)
                return
            }
            sender.sendMessage(Component.text("[AQQBot] 没有找到匹配的插件!", NamedTextColor.RED))
        } catch (e: Exception) {
            sender.sendMessage(Component.text("[AQQBot] 获取市场信息失败, 原因: ${e.message}", NamedTextColor.RED))
            plugin.log(LogLevel.WARN, "Failed to get market info: $e")
            return
        }
    }

    fun update(sender: ASender, findName: String) {
        var currentPlugin: ScriptPlugin? = null
        plugin.scriptLoader.pluginList.forEach {
            if (it.name == findName) {
                currentPlugin = it
                return@forEach
            }
        }
        if (currentPlugin == null) {
            sender.sendMessage(Component.text("[AQQBot] 未找到已安装的插件!").color(NamedTextColor.RED))
        }
        sender.sendMessage(Component.text("[AQQBot] 正在获取市场信息...", NamedTextColor.GREEN))
        try {
            val pluginsArray = getPluginList()
            for (plugin in pluginsArray) {
                val pluginObj = plugin.asJsonObject
                val schemaVersion = pluginObj.get("schema_version").asInt
                val name = pluginObj.get("name").asString
                val version = pluginObj.get("version").asString
                if (findName != name) continue
                if (schemaVersion > ScriptLoader.version) continue
                if (compareVersionNumbers(version, currentPlugin?.version ?: "0") > 0) {
                    val download = pluginObj.get("download").asString
                    sender.sendMessage(Component.text("[AQQBot] 找到匹配的插件, 开始下载 ...", NamedTextColor.GREEN))
                    this.plugin.scriptLoader.scriptManager.download(sender, download)
                    return
                }
            }
            sender.sendMessage(Component.text("[AQQBot] 没有找到匹配的插件!", NamedTextColor.RED))
        } catch (e: Exception) {
            sender.sendMessage(Component.text("[AQQBot] 获取市场信息失败, 原因: ${e.message}", NamedTextColor.RED))
            plugin.log(LogLevel.WARN, "Failed to get market info: $e")
            return
        }
    }

    fun getPluginList(): JsonArray {
        val url = URL("https://raw.githubusercontent.com/alazeprt/AQQBot-Plugins/master/repository.json")
        val connection = url.openConnection() as HttpURLConnection
        connection.apply {
            requestMethod = "GET"
            setRequestProperty("Accept", "application/vnd.github.v3+json")
            setRequestProperty("User-Agent", "AQQBot")
            connectTimeout = 5000
            readTimeout = 5000
        }
        if (connection.responseCode != HttpURLConnection.HTTP_OK) {
            "Unknown"
        }
        val reader = InputStreamReader(connection.inputStream)
        val jsonObj = Gson().fromJson(reader, JsonObject::class.java)
        val pluginsArray = jsonObj.getAsJsonArray("plugins")
        return pluginsArray
    }

    fun compareVersionNumbers(v1: String, v2: String): Int {
        val parts1: Array<String?> = v1.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val parts2: Array<String?> = v2.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        val maxLength = max(parts1.size.toDouble(), parts2.size.toDouble()).toInt()

        for (i in 0..<maxLength) {
            val num1 = if (i < parts1.size) parts1[i]!!.toInt() else 0
            val num2 = if (i < parts2.size) parts2[i]!!.toInt() else 0

            if (num1 != num2) {
                return num1 - num2
            }
        }

        return 0
    }
}