package top.alazeprt.aqqbot.qq

import org.bukkit.Bukkit
import top.alazeprt.aonebot.action.GetGroupMemberList
import top.alazeprt.aonebot.event.Listener
import top.alazeprt.aonebot.event.SubscribeBotEvent
import top.alazeprt.aonebot.event.message.GroupMessageEvent
import top.alazeprt.aonebot.event.notice.GroupMemberDecreaseEvent
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.AQQBot.config
import top.alazeprt.aqqbot.AQQBot.customCommands
import top.alazeprt.aqqbot.AQQBot.isBukkit
import top.alazeprt.aqqbot.AQQBot.isFileStorage
import top.alazeprt.aqqbot.AQQBot.oneBotClient
import top.alazeprt.aqqbot.handler.CommandHandler
import top.alazeprt.aqqbot.handler.InformationHandler
import top.alazeprt.aqqbot.handler.WhitelistHandler
import top.alazeprt.aqqbot.util.AI18n.get
import top.alazeprt.aqqbot.util.DBQuery.qqInDatabase
import top.alazeprt.aqqbot.util.DBQuery.removePlayer

class BotListener : Listener {
    @SubscribeBotEvent
    fun onGroupMessage(event: GroupMessageEvent) {
        if (!AQQBot.enableGroups.contains(event.groupId.toString())) {
            return
        }
        var message = ""
        oneBotClient.action(GetGroupMemberList(event.groupId), { memberList ->
            event.jsonMessage.forEach {
                val jsonObject = it.asJsonObject?: return@forEach
                if (jsonObject.get("type").asString == "text") {
                    message += jsonObject.get("data").asJsonObject.get("text").asString
                } else if (jsonObject.get("type").asString == "image") {
                    message += "[图片]"
                } else if (jsonObject.get("type").asString == "at") {
                    memberList.forEach { member ->
                        if (member.member.userId == jsonObject.get("data").asJsonObject.get("qq").asLong) {
                            message += "@${member.member.nickname}"
                        }
                    }
                }
            }
            val handleInfo = InformationHandler.handle(message, event)
            val handleWl = WhitelistHandler.handle(message, event)
            val handleCommand = CommandHandler.handle(message, event)
            var handleCustom = false
            customCommands.forEach {
                if (it.handle(message, event.senderId.toString(), event.groupId.toString())) {
                    handleCustom = true
                    return@forEach
                }
            }
            if(canForwardMessage(message) != null && !(handleInfo || handleWl || handleCustom || handleCommand) && isBukkit) {
                Bukkit.broadcastMessage(formatString(get("game.chat_from_qq", mutableMapOf("groupId" to event.groupId.toString(),
                    "userName" to event.senderNickName,
                    "message" to (canForwardMessage(message)?: return@action)))))
            }
        })
    }

    @SubscribeBotEvent
    fun onMemberLeave(event: GroupMemberDecreaseEvent) {
        val userId = event.userId.toString()
        if (isFileStorage && !AQQBot.dataMap.containsKey(userId)) {
            return
        } else if (!isFileStorage && qqInDatabase(userId.toLong()) == null) {
            return
        }
        if (isFileStorage) {
            AQQBot.dataMap.forEach { (k, v) ->
                if (k == userId) {
                    AQQBot.dataMap.remove(k)
                    return
                }
            }
        } else {
            val playerName = qqInDatabase(userId.toLong())!!
            removePlayer(userId.toLong(), playerName)
            for (player in Bukkit.getOnlinePlayers()) {
                if (player.name == playerName) {
                    player.kickPlayer(get("qq.whitelist.unbind_kick"))
                }
            }
        }
    }

    private fun formatString(input: String): String {
        return input.replace(Regex("&([0-9a-fklmnor])")) { matchResult ->
            "§" + matchResult.groupValues[1]
        }
    }

    private fun canForwardMessage(message: String): String? {
        if (!config.getBoolean("chat.group_to_server.enable")) {
            return null
        }
        if (config.getStringList("chat.group_to_server.prefix").contains("")) {
            return message
        }
        config.getStringList("chat.group_to_server.prefix").forEach {
            if (message.startsWith(it)) {
                return message.substring(it.length)
            }
        }
        return null
    }
}