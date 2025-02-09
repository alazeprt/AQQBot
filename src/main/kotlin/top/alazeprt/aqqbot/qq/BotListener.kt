package top.alazeprt.aqqbot.qq

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import taboolib.common.platform.function.submit
import taboolib.platform.VelocityPlugin
import top.alazeprt.aonebot.action.GetGroupMemberInfo
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
import top.alazeprt.aqqbot.util.AFormatter
import top.alazeprt.aqqbot.util.AI18n.get
import top.alazeprt.aqqbot.util.DBQuery.qqInDatabase
import top.alazeprt.aqqbot.util.DBQuery.removePlayerByUserId

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
            oneBotClient.action(GetGroupMemberInfo(event.groupId, event.senderId)) { member ->
                if (canForwardMessage(message) != null && !(handleInfo || handleWl || handleCustom || handleCommand) && isBukkit) {
                    Bukkit.broadcastMessage(
                        AFormatter.pluginToChat(get("game.chat_from_qq", mutableMapOf("groupId" to event.groupId.toString(),
                            "userName" to member.card,
                            "message" to (canForwardMessage(message)?: return@action)))))
                } else if (canForwardMessage(message) != null && !(handleInfo || handleWl || handleCustom || handleCommand) &&
                    config.getBoolean("chat.group_to_server.vc_broadcast") && !isBukkit) {
                    VelocityPlugin.getInstance().server.allServers.forEach {
                        it.sendMessage(
                            Component.text(
                                AFormatter.pluginToChat(get("game.chat_from_qq", mutableMapOf("groupId" to event.groupId.toString(),
                                    "userName" to member.card,
                                    "message" to (canForwardMessage(message)?: return@action))))
                            )
                        )
                    }
                }
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
            removePlayerByUserId(userId.toLong())
            submit {
                if (isBukkit) {
                    for (player in Bukkit.getOnlinePlayers()) {
                        if (playerName.contains(player.name)) {
                            player.kickPlayer(get("game.kick_when_unbind"))
                        }
                    }
                } else {
                    for (player in VelocityPlugin.getInstance().server.allPlayers) {
                        if (playerName.contains(player.username)) {
                            player.disconnect(Component.text(get("game.kick_when_unbind")))
                        }
                    }
                }
            }
        }
    }

    private fun canForwardMessage(message: String): String? {
        if (!config.getBoolean("chat.group_to_server.enable")) {
            return null
        }
        var newMessage = message;
        if (message.length > config.getInt("chat.max_forward_length")) {
            newMessage = newMessage.substring(0, config.getInt("chat.max_forward_length")) + "..."
        }
        if (config.getStringList("chat.group_to_server.prefix").contains("")) {
            return formatter.regexFilter(config.getStringList("chat.group_to_server.filter"), newMessage)
        }
        config.getStringList("chat.group_to_server.prefix").forEach {
            if (newMessage.startsWith(it)) {
                return formatter.regexFilter(config.getStringList("chat.group_to_server.filter"), newMessage.substring(it.length))
            }
        }
        return null
    }

    companion object {
        val formatter = AFormatter()
    }
}