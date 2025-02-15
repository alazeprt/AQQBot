package top.alazeprt.aqqbot.handler

import top.alazeprt.aonebot.action.GetGroupMemberList
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aonebot.event.message.GroupMessageEvent
import top.alazeprt.aonebot.result.GroupMemberList
import top.alazeprt.aonebot.util.GroupRole
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.bot.BotProvider
import top.alazeprt.aqqbot.util.AFormatter.Companion.validateName

class WhitelistAdminHandler(val plugin: AQQBot) {

    private val config = plugin.generalConfig

    private fun bind(operatorId: String, userId: String, groupId: Long, playerName: String): Boolean {
        if (plugin.hasQQ(userId.toLong())) {
            plugin.removePlayer(userId.toLong())
        }
        if (!validateName(playerName)) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.getMessageManager().get("qq.whitelist.invalid_name"), true))
            return false
        }
        if (plugin.hasPlayer(plugin.adapter!!.getOfflinePlayer(playerName))) {
            plugin.removePlayer(plugin.adapter!!.getOfflinePlayer(playerName))
        }
        plugin.addPlayer(userId.toLong(), plugin.adapter!!.getOfflinePlayer(playerName))
        BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.getMessageManager().get("qq.whitelist.bind_successful"), true))
        plugin.debugModule?.debugLogger?.log("$operatorId bind $userId to account $playerName")
        return true
    }

    private fun unbind(operatorId: String, userId: String, groupId: Long, playerName: String): Boolean {
        if (!plugin.hasQQ(userId.toLong())) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.getMessageManager().get("qq.whitelist.admin.not_bind", mutableMapOf(Pair("userId", userId))), true))
            return false
        }
        if ((plugin.getQQByPlayer(plugin.adapter!!.getOfflinePlayer(playerName))?: -1L) != userId.toLong()) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.getMessageManager().get("qq.whitelist.admin.bind_by_other", mutableMapOf(Pair("name",
                plugin.getPlayerByQQ(userId.toLong()).joinToString(", ") { it.getName() }))), true))
            return false
        }
        plugin.removePlayer(userId.toLong(), plugin.adapter!!.getOfflinePlayer(playerName))
        BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.getMessageManager().get("qq.whitelist.unbind_successful"), true))
        plugin.debugModule?.debugLogger?.log("$operatorId unbind $userId to account $playerName")
        plugin.submit {
            plugin.adapter!!.getPlayerList().forEach {
                if (it.getName() == playerName) {
                    it.kick(plugin.getMessageManager().get("game.kick_when_unbind"))
                }
            }
        }
        return true

    }

    fun handle(message: String, event: GroupMessageEvent, memberList: GroupMemberList): Boolean {
        if (!plugin.generalConfig.getBoolean("whitelist.admin.enable")) {
            return false
        }
        if (message.split(" ").size != 3) return false
        val targetUserId = message.split(" ")[1].toLongOrNull()
        if (targetUserId == null) {
            BotProvider.getBot()?.action(SendGroupMessage(event.groupId, plugin.getMessageManager().get("qq.whitelist.admin.invalid_user_id"), true))
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
                    plugin.getMessageManager().get("qq.whitelist.admin.user_not_in_group"),
                    true
                )
            )
            return true
        }
        if (!hasPermission) {
            return false
        }
        config.getStringList("whitelist.admin.bind").forEach {
            if (message.lowercase().startsWith(it.lowercase())) {
                bind(event.senderId.toString(), targetUserId.toString(), event.groupId, playerName)
                return true
            }
        }
        config.getStringList("whitelist.admin.unbind").forEach {
            if (message.lowercase().startsWith(it.lowercase())) {
                unbind(event.senderId.toString(), targetUserId.toString(), event.groupId, playerName)
                return true
            }
        }
        return false
    }

}