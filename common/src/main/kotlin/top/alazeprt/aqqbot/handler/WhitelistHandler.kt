package top.alazeprt.aqqbot.handler

import top.alazeprt.aonebot.action.GetGroupMemberInfo
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aonebot.action.SetGroupCard
import top.alazeprt.aonebot.event.message.GroupMessageEvent
import top.alazeprt.aonebot.util.GroupRole
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.bot.BotProvider
import top.alazeprt.aqqbot.util.AFormatter.Companion.validateName

class WhitelistHandler(val plugin: AQQBot) {
    
    private val config = plugin.generalConfig
    
    private fun bind(userId: String, groupId: Long, data: String): Boolean {
        val playerName: String
        if (plugin.getPlayerByQQ(userId.toLong()).size >= config.getLong("whitelist.max_bind_count")) {
            BotProvider.getBot()?.action(
                SendGroupMessage(groupId, plugin.getMessageManager().get("qq.whitelist.already_bind"), true))
            return false
        }
        if (config.getString("whitelist.verify_method")?.uppercase() == "VERIFY_CODE") {
            var name: String? = null
            plugin.verifyCodeMap.forEach { (k, v) ->
                if (v.first == data) {
                    name = k
                    return@forEach
                }
            }
            if (name == null) {
                BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.getMessageManager().get("qq.whitelist.verify_code_not_exist"), true))
                return false
            }
            playerName = name!!
        } else {
            playerName = data
            if (!validateName(playerName)) {
                BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.getMessageManager().get("qq.whitelist.invalid_name"), true))
                return false
            }
        }
        if (plugin.hasPlayer(plugin.adapter!!.getOfflinePlayer(playerName))) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.getMessageManager().get("qq.whitelist.already_exist"), true))
            return false
        }
        plugin.addPlayer(userId.toLong(), plugin.adapter!!.getOfflinePlayer(playerName))
        BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.getMessageManager().get("qq.whitelist.bind_successful"), true))
        plugin.debugModule?.debugLogger?.log("$userId bind $userId to account $playerName")
        if (config.getString("whitelist.verify_method")?.uppercase() == "VERIFY_CODE") {
            plugin.verifyCodeMap.remove(playerName)
        }
        if (config.getBoolean("whitelist.change_nickname_on_bind.enable")) {
            BotProvider.getBot()?.action(GetGroupMemberInfo(groupId, userId.toLong())) {
                val newName = config.getString("whitelist.change_nickname_on_bind.format")!!
                    .replace("\${playerName}", playerName)
                    .replace("\${qq}", userId)
                    .replace("\${nickName}", it.member.nickname)
                BotProvider.getBot()?.action(SetGroupCard(groupId, userId.toLong(), newName))
            }
        }
        return true
    }
    
    private fun unbind(userId: String, groupId: Long, playerName: String): Boolean {
        if (!plugin.hasQQ(userId.toLong())) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.getMessageManager().get("qq.whitelist.not_bind"), true))
            return false
        }
        if (!plugin.getPlayerByQQ(userId.toLong()).map { it.getName() }.toList().contains(playerName)) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.getMessageManager().get("qq.whitelist.bind_by_other", mutableMapOf(Pair("name",
                plugin.getPlayerByQQ(userId.toLong()).joinToString(", ") { it.getName() }))), true))
            return false
        }
        plugin.removePlayer(userId.toLong(), plugin.adapter!!.getOfflinePlayer(playerName))
        BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.getMessageManager().get("qq.whitelist.unbind_successful"), true))
        plugin.debugModule?.debugLogger?.log("$userId unbind $userId to account $playerName")
        plugin.submit {
            plugin.adapter!!.getPlayerList().forEach {
                if (it.getName() == playerName) {
                    it.kick(plugin.getMessageManager().get("game.kick_when_unbind"))
                }
            }
        }
        return true
    }

    fun handle(message: String, event: GroupMessageEvent): Boolean {
        if (!config.getBoolean("whitelist.enable")) {
            return false
        }
        if (message.split(" ").size != 2) return false
        config.getStringList("whitelist.prefix.bind").forEach {
            if (message.lowercase().startsWith(it.lowercase())) {
                val playerName = message.split(" ")[1]
                bind(event.senderId.toString(), event.groupId, playerName)
                return true
            }
        }
        config.getStringList("whitelist.prefix.unbind").forEach {
            if (message.lowercase().startsWith(it.lowercase())) {
                val playerName = message.substring(it.length + 1)
                unbind(event.senderId.toString(), event.groupId, playerName)
                return true
            }
        }
        return false
    }
}