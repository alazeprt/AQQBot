package top.alazeprt.aqqbot.handler

import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aonebot.event.message.GroupMessageEvent
import top.alazeprt.aonebot.result.GroupMemberList
import top.alazeprt.aonebot.util.GroupRole
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.api.AQQBotAPI
import top.alazeprt.aqqbot.api.event.qq.PostBindEvent
import top.alazeprt.aqqbot.api.event.qq.PostUnbindEvent
import top.alazeprt.aqqbot.api.event.qq.PreBindEvent
import top.alazeprt.aqqbot.api.event.qq.PreUnbindEvent
import top.alazeprt.aqqbot.api.event.qq.reason.BindCancelReason
import top.alazeprt.aqqbot.api.event.qq.reason.UnbindCancelReason
import top.alazeprt.aqqbot.bot.BotProvider
import top.alazeprt.aqqbot.util.AFormatter
import top.alazeprt.aqqbot.util.AFormatter.Companion.validateName

class WhitelistAdminHandler(val plugin: AQQBot) {

    private val config = plugin.generalConfig

    private fun bind(operatorId: String, userId: String, groupId: Long, playerName: String): Boolean {
        if (plugin.getPlayerByQQ(userId.toLong()).size >= plugin.generalConfig.getLong("whitelist.max_bind_count", null)) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.messageManager.get("qq.whitelist.admin.already_bind",
                mutableMapOf("userId" to userId, "playerName" to playerName), null)))
            return false
        }
        if (!validateName(plugin, playerName, groupId)) {
            AQQBotAPI.fireEvent(PostBindEvent(groupId, operatorId.toLong(), userId.toLong(), playerName, true, BindCancelReason.INVALID_NAME))
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.messageManager.get("qq.whitelist.invalid_name", groupId), true))
            return false
        }
        if (plugin.hasPlayer(plugin.adapter.getOfflinePlayer(playerName))) {
            val exists = plugin.getQQByPlayer(plugin.adapter.getOfflinePlayer(playerName))
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.messageManager.get("qq.whitelist.admin.bind_by_other",
                mutableMapOf("userId" to userId, "playerName" to playerName, "anotherUserId" to exists.toString()), null)))
            return false
        }
        val event = PreBindEvent(groupId, operatorId.toLong(), userId.toLong(), playerName)
        AQQBotAPI.fireEvent(event)
        if (event.isCanceled()) {
            AQQBotAPI.fireEvent(PostBindEvent(groupId, operatorId.toLong(), userId.toLong(), playerName, true, BindCancelReason.CANCEL_BY_PLUGIN))
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.messageManager.get("qq.cancel_by_plugin", groupId), true))
            return false
        }
        plugin.addPlayer(userId.toLong(), plugin.adapter.getOfflinePlayer(playerName))
        BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.messageManager.get("qq.whitelist.bind_successful", groupId), true))
        plugin.debugModule?.debugLogger?.log("$operatorId bind $userId to account $playerName")
        AQQBotAPI.fireEvent(PostBindEvent(groupId, operatorId.toLong(), userId.toLong(), playerName, false, null))
        return true
    }

    private fun unbind(operatorId: String, userId: String, groupId: Long, playerName: String): Boolean {
        if (!plugin.hasQQ(userId.toLong())) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.messageManager.get("qq.whitelist.admin.not_bind", mutableMapOf(Pair("userId", userId)), groupId), true))
            AQQBotAPI.fireEvent(PostUnbindEvent(groupId, operatorId.toLong(), userId.toLong(), playerName, true, UnbindCancelReason.NOT_BIND))
            return false
        }
        if ((plugin.getQQByPlayer(plugin.adapter!!.getOfflinePlayer(playerName))?: -1L) != userId.toLong()) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.messageManager.get("qq.whitelist.admin.bind_by_other", mutableMapOf(Pair("name",
                plugin.getPlayerByQQ(userId.toLong()).joinToString(", ") { it.getName() })), groupId), true))
            AQQBotAPI.fireEvent(PostUnbindEvent(groupId, operatorId.toLong(), userId.toLong(), playerName, true, UnbindCancelReason.BIND_BY_OTHER))
            return false
        }
        val event = PreUnbindEvent(groupId, operatorId.toLong(), userId.toLong(), playerName)
        AQQBotAPI.fireEvent(event)
        if (event.isCanceled()) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.messageManager.get("qq.cancel_by_plugin", groupId), true))
            AQQBotAPI.fireEvent(PostUnbindEvent(groupId, operatorId.toLong(), userId.toLong(), playerName, true, UnbindCancelReason.CANCEL_BY_PLUGIN))
            return false
        }
        plugin.removePlayer(userId.toLong(), plugin.adapter!!.getOfflinePlayer(playerName))
        BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.messageManager.get("qq.whitelist.unbind_successful", groupId), true))
        plugin.debugModule?.debugLogger?.log("$operatorId unbind $userId to account $playerName")
        plugin.submit {
            plugin.adapter.getPlayerList().forEach {
                if (it.getName() == playerName) {
                    it.kick(plugin.messageManager.get("game.kick_when_unbind", groupId))
                }
            }
        }
        AQQBotAPI.fireEvent(PostUnbindEvent(groupId, operatorId.toLong(), userId.toLong(), playerName, false, null))
        return true

    }

    fun handle(message: String, event: GroupMessageEvent, memberList: GroupMemberList): Boolean {
        var bind = false
        var unbind = false
        config.getStringList("whitelist.admin.bind", event.groupId).forEach {
            if (message.lowercase().startsWith(it.lowercase())) {
                bind = true
            }
        }
        config.getStringList("whitelist.admin.unbind", event.groupId).forEach {
            if (message.lowercase().startsWith(it.lowercase())) {
                unbind = true
            }
        }
        if (!bind && !unbind) return false
        if (!plugin.generalConfig.getBoolean("whitelist.admin.enable", event.groupId)) {
            return false
        }
        if (message.split(" ").size != 3) return false
        val targetUserId = message.split(" ")[1].toLongOrNull()
        if (targetUserId == null) {
            BotProvider.getBot()?.action(SendGroupMessage(event.groupId, plugin.messageManager.get("qq.whitelist.admin.invalid_user_id", event.groupId), true))
            return true
        }
        val playerName = message.split(" ")[2]
        var has = false
        var hasPermission = false
        for (member in memberList) {
            if (member.member.userId == targetUserId) {
                has = true
            }
            if (member.member.userId == event.senderId &&
                (member.role == GroupRole.ADMIN || member.role == GroupRole.OWNER)) {
                hasPermission = true
            }
        }
        if (!has) {
            BotProvider.getBot()?.action(
                SendGroupMessage(
                    event.groupId,
                    plugin.messageManager.get("qq.whitelist.admin.user_not_in_group", event.groupId),
                    true
                )
            )
            return true
        }
        if (!hasPermission) {
            return false
        }
        if (bind) {
            bind(event.senderId.toString(), targetUserId.toString(), event.groupId, playerName)
            return true
        } else if (unbind) {
            unbind(event.senderId.toString(), targetUserId.toString(), event.groupId, playerName)
            return true
        }
        return false
    }

}