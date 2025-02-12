package top.alazeprt.aqqbot.command.impl

import top.alazeprt.aqqbot.util.LogLevel
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.util.AFormatter

class ACommandImpl(val plugin: AQQBot) {
    fun startReload(): Long {
        val s = System.currentTimeMillis()
        plugin.log(LogLevel.INFO, "Reloading AQQBot...")
        plugin.reload()
        val time = System.currentTimeMillis() - s
        plugin.log(LogLevel.INFO, "Reloaded AQQBot in $time ms")
        return time
    }

    fun forceBind(userId: String, playerName: String): String {
        plugin.removePlayer(userId.toLong())
        if (!AFormatter.validateName(playerName)) {
            return AFormatter.pluginToChat(plugin.getMessageManager().get("game.invalid_arguments"))
        }
        plugin.removePlayer(plugin.adapter!!.getOfflinePlayer(playerName))
        plugin.addPlayer(userId.toLong(), plugin.adapter!!.getOfflinePlayer(playerName))
        return AFormatter.pluginToChat(plugin.getMessageManager().get("game.successfully_bind"))
    }

    fun forceUnbind(mode: String, data: String): String {
        if (mode.contains("qq")) {
            if (!plugin.hasQQ(data.toLong())) {
                return AFormatter.pluginToChat(plugin.getMessageManager().get("game.invalid_arguments"))
            }
            plugin.removePlayer(data.toLong())
            return AFormatter.pluginToChat(plugin.getMessageManager().get("game.successfully_unbind"))
        } else {
            if (!plugin.hasPlayer(plugin.adapter!!.getOfflinePlayer(data))) {
                return AFormatter.pluginToChat(plugin.getMessageManager().get("game.invalid_arguments"))
            }
            plugin.removePlayer(plugin.adapter!!.getOfflinePlayer(data))
            return AFormatter.pluginToChat(plugin.getMessageManager().get("game.successfully_unbind"))
        }
    }

    fun query(mode: String, data: String): String {
        var userId = "未知"
        var playerName = "未知"
        if (mode.contains("qq")) {
            userId = data
            playerName = plugin.getPlayerByQQ(data.toLong()).joinToString(", ")
        } else {
            playerName = data
            userId = plugin.getQQByPlayer(plugin.adapter!!.getOfflinePlayer(data)).toString()
        }
        return AFormatter.pluginToChat(plugin.getMessageManager()
            .getList("game.query_result", mutableMapOf("userId" to userId, "playerName" to playerName)))
    }
}