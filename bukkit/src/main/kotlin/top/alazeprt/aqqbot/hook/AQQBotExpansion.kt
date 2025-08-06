package top.alazeprt.aqqbot.hook

import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import top.alazeprt.aqqbot.AQQBot

class AQQBotExpansion(val plugin: AQQBot): PlaceholderExpansion() {
    override fun getIdentifier(): String {
        return "aqqbot"
    }

    override fun getAuthor(): String {
        return "alazeprt"
    }

    override fun getVersion(): String {
        return "2.0-alpha.12"
    }

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        plugin.debugModule?.debugLogger?.log("PlaceholderAPI request with params: $params")
        if (params.contentEquals("qq")) {
            return (plugin.getQQByPlayer(plugin.adapter!!.getOfflinePlayer(player?.name?: ""))?: -1).toString()
        }
        if (params.contains("qq_")) {
            val qq = params.split("_")[1]
            return plugin.getPlayerByQQ(qq.toLong()).firstOrNull()?.getName() ?: ""
        }
        if (params.contains("player_")) {
            val playerName = params.split("_")[1]
            return (plugin.getQQByPlayer(plugin.adapter!!.getOfflinePlayer(playerName))?: -1).toString()
        }
        return ""
    }
}