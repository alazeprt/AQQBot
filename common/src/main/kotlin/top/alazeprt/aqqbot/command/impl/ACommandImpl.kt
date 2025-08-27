package top.alazeprt.aqqbot.command.impl

import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.profile.ASender
import top.alazeprt.aqqbot.util.AFormatter
import top.alazeprt.aqqbot.util.APluginInformation
import top.alazeprt.aqqbot.util.LogLevel

class ACommandImpl(val plugin: AQQBot) {
    fun startReload(): Long {
        val s = System.currentTimeMillis()
        plugin.log(LogLevel.INFO, "Reloading AQQBot...")
        plugin.reload(null)
        val time = System.currentTimeMillis() - s
        plugin.log(LogLevel.INFO, "Reloaded AQQBot in $time ms")
        return time
    }

    fun addBind(sender: ASender, userId: String, playerName: String): String {
        if (!AFormatter.validateName(plugin, playerName, null)) {
            return AFormatter.pluginToChat(plugin.messageManager.get("game.invalid_arguments", null))
        }
        if (plugin.getPlayerByQQ(userId.toLong()).size >= plugin.generalConfig.getLong("whitelist.max_bind_count", null)) {
            return AFormatter.pluginToChat(plugin.messageManager.get("game.bind_too_many_accounts",
                mutableMapOf("userId" to userId, "playerName" to playerName), null))
        }
        if (plugin.hasPlayer(plugin.adapter!!.getOfflinePlayer(playerName))) {
            val exists = plugin.getQQByPlayer(plugin.adapter!!.getOfflinePlayer(playerName))
            return AFormatter.pluginToChat(plugin.messageManager.get("game.already_exists",
                mutableMapOf("userId" to userId, "playerName" to playerName, "anotherUserId" to exists.toString()), null))
        }
        plugin.addPlayer(userId.toLong(), plugin.adapter!!.getOfflinePlayer(playerName))
        plugin.debugModule?.debugLogger?.log("(in game) ${sender.getName()} bind $userId to account $playerName")
        return AFormatter.pluginToChat(plugin.messageManager.get("game.successfully_bind", null))
    }

    fun removeBind(sender: ASender, mode: String, data: String): String {
        if (mode.contains("qq")) {
            if (!plugin.hasQQ(data.toLong())) {
                return AFormatter.pluginToChat(plugin.messageManager.get("game.invalid_arguments", null))
            }
            plugin.removePlayer(data.toLong())
            plugin.debugModule?.debugLogger?.log("(in game) ${sender.getName()} unbind $data (qq)")
        } else {
            if (!plugin.hasPlayer(plugin.adapter!!.getOfflinePlayer(data))) {
                return AFormatter.pluginToChat(plugin.messageManager.get("game.invalid_arguments", null))
            }
            plugin.removePlayer(plugin.adapter!!.getOfflinePlayer(data))
            plugin.debugModule?.debugLogger?.log("(in game) ${sender.getName()} unbind $data (player)")
        }
        plugin.submit {
            plugin.adapter!!.getPlayerList().forEach {
                if (!plugin.hasPlayer(plugin.adapter!!.getOfflinePlayer(it.getName()))) {
                    it.kick(plugin.messageManager.get("game.kick_when_unbind", null))
                }
            }
        }
        return AFormatter.pluginToChat(plugin.messageManager.get("game.successfully_unbind", null))
    }

    fun query(mode: String, data: String): String {
        var userId = "未知"
        var playerName = "未知"
        if (mode.contains("qq")) {
            userId = data
            playerName = plugin.getPlayerByQQ(data.toLong()).joinToString(", ") { it.getName() }
        } else {
            playerName = data
            userId = plugin.getQQByPlayer(plugin.adapter!!.getOfflinePlayer(data)).toString()
        }
        return AFormatter.pluginToChat(plugin.messageManager
            .getList("game.query_result", mutableMapOf("userId" to userId, "playerName" to playerName), null))
    }

    fun status(sender: ASender) {
        sender.sendMessage(AFormatter.pluginToChat(plugin.messageManager.get("game.getting_status_data", null)))
        plugin.submitAsync {
            val pluginInfo = APluginInformation(plugin)
            val version = pluginInfo.getVersion()
            val author = pluginInfo.getAuthor()
            val website = pluginInfo.getWebsite()
            val latestVersion = pluginInfo.getLatestVersion()
            val latestCommit = pluginInfo.getLatestCommit()
            val currentConfigVersion = pluginInfo.getCurrentConfigVersion()
            val pluginConfigVersion = pluginInfo.getPluginConfigVersion()
            val latestConfigVersion = pluginInfo.getLatestConfigVersion()
            val websocketStatus = pluginInfo.getWebsocketStatus()
            sender.sendMessage(AFormatter.pluginToChat(plugin.messageManager.getList("game.status_result", mutableMapOf(
                "author" to author,
                "version" to version,
                "website" to website,
                "latest_version" to latestVersion,
                "latest_commit" to latestCommit,
                "config_version" to currentConfigVersion,
                "plugin_config_version" to pluginConfigVersion,
                "latest_config_version" to latestConfigVersion,
                "websocket_status" to websocketStatus
            ), null)))
        }
    }

    fun reset(sender: ASender, mode: String, data: String): String {
        if (mode.contains("qq")) {
            if (!plugin.hasQQ(data.toLong())) {
                return AFormatter.pluginToChat(plugin.messageManager.get("game.invalid_arguments", null))
            }
            plugin.removePlayer(data.toLong())
            plugin.debugModule?.debugLogger?.log("(in game) ${sender.getName()} reset $data (qq)")
        } else {
            if (!plugin.hasPlayer(plugin.adapter!!.getOfflinePlayer(data))) {
                return AFormatter.pluginToChat(plugin.messageManager.get("game.invalid_arguments", null))
            }
            val qq = plugin.getQQByPlayer(plugin.adapter!!.getOfflinePlayer(data))
            plugin.removePlayer(qq!!.toLong())
            plugin.debugModule?.debugLogger?.log("(in game) ${sender.getName()} reset $data (player) & $qq (qq)")
        }
        plugin.submit {
            plugin.adapter!!.getPlayerList().forEach {
                if (!plugin.hasPlayer(plugin.adapter!!.getOfflinePlayer(it.getName()))) {
                    it.kick(plugin.messageManager.get("game.kick_when_unbind", null))
                }
            }
        }
        return AFormatter.pluginToChat(plugin.messageManager.get("game.successfully_reset", null))
    }
}