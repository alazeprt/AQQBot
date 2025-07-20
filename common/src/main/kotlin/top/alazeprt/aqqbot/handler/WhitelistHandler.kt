package top.alazeprt.aqqbot.handler

import top.alazeprt.aonebot.action.GetGroupMemberInfo
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aonebot.action.SetGroupCard
import top.alazeprt.aonebot.event.message.GroupMessageEvent
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.api.AQQBotAPI
import top.alazeprt.aqqbot.api.event.qq.reason.BindCancelReason
import top.alazeprt.aqqbot.api.event.qq.PostBindEvent
import top.alazeprt.aqqbot.api.event.qq.PostUnbindEvent
import top.alazeprt.aqqbot.api.event.qq.PreBindEvent
import top.alazeprt.aqqbot.api.event.qq.PreUnbindEvent
import top.alazeprt.aqqbot.api.event.qq.reason.UnbindCancelReason
import top.alazeprt.aqqbot.bot.BotProvider
import top.alazeprt.aqqbot.util.AFormatter.Companion.validateName

class WhitelistHandler(val plugin: AQQBot) {
    
    private val config = plugin.generalConfig
    
    private fun bind(userId: String, groupId: Long, data: String): Boolean {
        val playerName: String
        if (plugin.getPlayerByQQ(userId.toLong()).size >= config.getLong("whitelist.max_bind_count", groupId)) {
            BotProvider.getBot()?.action(
                SendGroupMessage(groupId, plugin.messageManager.get("qq.whitelist.already_bind", groupId), true))
            AQQBotAPI.fireEvent(PostBindEvent(groupId, userId.toLong(), userId.toLong(), "", true, BindCancelReason.ALREADY_BIND))
            return false
        }
        if (config.getString("whitelist.verify_method", groupId).uppercase() == "VERIFY_CODE") {
            var name: String? = null
            plugin.verifyCodeMap.forEach { (k, v) ->
                if (v.first == data) {
                    name = k
                    return@forEach
                }
            }
            if (name == null) {
                BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.messageManager.get("qq.whitelist.verify_code_not_exist", groupId), true))
                AQQBotAPI.fireEvent(PostBindEvent(groupId, userId.toLong(), userId.toLong(), "", true, BindCancelReason.VERIFY_CODE_NOT_EXISTS))
                return false
            }
            playerName = name!!
        } else {
            playerName = data
            if (!validateName(plugin, playerName, groupId)) {
                BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.messageManager.get("qq.whitelist.invalid_name", groupId), true))
                AQQBotAPI.fireEvent(PostBindEvent(groupId, userId.toLong(), userId.toLong(), playerName, true, BindCancelReason.INVALID_NAME))
                return false
            }
        }
        if (plugin.hasPlayer(plugin.adapter.getOfflinePlayer(playerName))) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.messageManager.get("qq.whitelist.already_exist", groupId), true))
            AQQBotAPI.fireEvent(PostBindEvent(groupId, userId.toLong(), userId.toLong(), playerName, true, BindCancelReason.ALREADY_EXISTS_NAME))
            return false
        }
        val event = PreBindEvent(groupId, userId.toLong(), userId.toLong(), playerName)
        AQQBotAPI.fireEvent(event)
        if (event.isCanceled()) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.messageManager.get("qq.cancel_by_plugin", groupId), true))
            AQQBotAPI.fireEvent(PostBindEvent(groupId, userId.toLong(), userId.toLong(), playerName, true, BindCancelReason.CANCEL_BY_PLUGIN))
            return false
        }
        plugin.addPlayer(userId.toLong(), plugin.adapter.getOfflinePlayer(playerName))
        BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.messageManager.get("qq.whitelist.bind_successful", groupId), true))
        plugin.debugModule?.debugLogger?.log("$userId bind $userId to account $playerName")
        if (config.getString("whitelist.verify_method", groupId)?.uppercase() == "VERIFY_CODE") {
            plugin.verifyCodeMap.remove(playerName)
        }
        if (config.getBoolean("whitelist.change_nickname_on_bind.enable", groupId)) {
            BotProvider.getBot()?.action(GetGroupMemberInfo(groupId, userId.toLong())) {
                val newName = config.getString("whitelist.change_nickname_on_bind.format", groupId)
                    .replace("\${playerName}", playerName)
                    .replace("\${qq}", userId)
                    .replace("\${nickName}", it.member.nickname)
                BotProvider.getBot()?.action(SetGroupCard(groupId, userId.toLong(), newName))
            }
        }
        AQQBotAPI.fireEvent(PostBindEvent(groupId, userId.toLong(), userId.toLong(), playerName, false, null))
        return true
    }
    
    private fun unbind(userId: String, groupId: Long, playerName: String): Boolean {
        if (!plugin.hasQQ(userId.toLong())) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.messageManager.get("qq.whitelist.not_bind", groupId), true))
            AQQBotAPI.fireEvent(PostUnbindEvent(groupId, userId.toLong(), userId.toLong(), playerName, true, UnbindCancelReason.NOT_BIND))
            return false
        }
        if (!plugin.getPlayerByQQ(userId.toLong()).map { it.getName() }.toList().contains(playerName)) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.messageManager.get("qq.whitelist.bind_by_other", mutableMapOf(Pair("name",
                plugin.getPlayerByQQ(userId.toLong()).joinToString(", ") { it.getName() })), groupId), true))
            AQQBotAPI.fireEvent(PostUnbindEvent(groupId, userId.toLong(), userId.toLong(), playerName, true, UnbindCancelReason.BIND_BY_OTHER))
            return false
        }
        val event = PreUnbindEvent(groupId, userId.toLong(), userId.toLong(), playerName)
        AQQBotAPI.fireEvent(event)
        if (event.isCanceled()) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.messageManager.get("qq.cancel_by_plugin", groupId), true))
            AQQBotAPI.fireEvent(PostUnbindEvent(groupId, userId.toLong(), userId.toLong(), playerName, true, UnbindCancelReason.CANCEL_BY_PLUGIN))
            return false
        }
        plugin.removePlayer(userId.toLong(), plugin.adapter!!.getOfflinePlayer(playerName))
        BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.messageManager.get("qq.whitelist.unbind_successful", groupId), true))
        plugin.debugModule?.debugLogger?.log("$userId unbind $userId to account $playerName")
        plugin.submit {
            plugin.adapter!!.getPlayerList().forEach {
                if (it.getName() == playerName) {
                    it.kick(plugin.messageManager.get("game.kick_when_unbind", groupId))
                }
            }
        }
        AQQBotAPI.fireEvent(PostUnbindEvent(groupId, userId.toLong(), userId.toLong(), playerName, false, null))
        return true
    }

    fun handle(message: String, event: GroupMessageEvent): Boolean {
        if (!config.getBoolean("whitelist.enable", event.groupId)) {
            return false
        }
        if (message.split(" ").size != 2) return false
        config.getStringList("whitelist.prefix.bind", event.groupId).forEach {
            if (message.lowercase().startsWith(it.lowercase())) {
                val playerName = message.split(" ")[1]
                if (plugin.bindCooldownMap.containsKey(playerName)) {
                    BotProvider.getBot()?.action(SendGroupMessage(event.groupId,
                        plugin.messageManager.get("qq.whitelist.in_cooldown",
                            mutableMapOf("name" to playerName, "cooldown_time" to plugin.bindCooldownMap[playerName]!!.toString()), event.groupId)))
                } else {
                    plugin.bindCooldownMap[playerName] = config.getLong("whitelist.cooldown.bind", event.groupId)
                    bind(event.senderId.toString(), event.groupId, playerName)
                }
                return true
            }
        }
        config.getStringList("whitelist.prefix.unbind", event.groupId).forEach {
            if (message.lowercase().startsWith(it.lowercase())) {
                val playerName = message.substring(it.length + 1)
                if (plugin.unbindCooldownMap.containsKey(playerName)) {
                    BotProvider.getBot()?.action(SendGroupMessage(event.groupId,
                        plugin.messageManager.get("qq.whitelist.in_cooldown",
                            mutableMapOf("name" to playerName, "cooldown_time" to plugin.unbindCooldownMap[playerName]!!.toString()), event.groupId)))
                } else {
                    plugin.unbindCooldownMap[playerName] = config.getLong("whitelist.cooldown.unbind", event.groupId)
                    unbind(event.senderId.toString(), event.groupId, playerName)
                }
                return true
            }
        }
        return false
    }
}