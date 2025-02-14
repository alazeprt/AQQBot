package top.alazeprt.aqqbot.handler

import top.alazeprt.aonebot.action.GetGroupMemberList
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aonebot.event.message.GroupMessageEvent
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.bot.BotProvider
import top.alazeprt.aqqbot.util.AFormatter.Companion.validateName

class WhitelistAdminHandler(val plugin: AQQBot) {
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
        if (plugin.hasQQ(userId.toLong())) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.getMessageManager().get("qq.whitelist.admin.not_bind", mutableMapOf(Pair("userId", userId))), true))
            return false
        }
        if ((plugin.getQQByPlayer(plugin.adapter!!.getOfflinePlayer(playerName))?: -1L) != userId.toLong()) {
            BotProvider.getBot()?.action(SendGroupMessage(groupId, plugin.getMessageManager().get("qq.whitelist.admin.bind_by_other", mutableMapOf(Pair("name",
                plugin.getPlayerByQQ(userId.toLong()).joinToString(", ") { it.getName() }))), true))
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

    fun handle(message: String, event: GroupMessageEvent, action: String) {
        if (!plugin.generalConfig.getBoolean("whitelist.admin")) {
            return
        }
        val userId = message.split(" ")[1].toLongOrNull()
        if (userId == null) {
            BotProvider.getBot()?.action(SendGroupMessage(event.groupId, plugin.getMessageManager().get("qq.whitelist.admin.invalid_user_id"), true))
            return
        }
        val playerName = message.split(" ")[2]
        BotProvider.getBot()?.action(GetGroupMemberList(event.groupId)) {
            var has = false;
            for (member in it) {
                if (member.member.userId == userId) {
                    has = true
                    break
                }
            }
            if (!has) {
                BotProvider.getBot()?.action(SendGroupMessage(event.groupId, plugin.getMessageManager().get("qq.whitelist.admin.user_not_in_group"), true))
                return@action
            } else {
                if (action == "bind") {
                    bind(event.senderId.toString(), userId.toString(), event.groupId, playerName)
                } else if (action == "unbind") {
                    unbind(event.senderId.toString(), userId.toString(), event.groupId, playerName)
                }
            }
        }
    }
}