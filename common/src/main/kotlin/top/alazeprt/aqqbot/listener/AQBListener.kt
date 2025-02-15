package top.alazeprt.aqqbot.listener

import net.kyori.adventure.text.Component
import top.alazeprt.aonebot.action.GetGroupMemberInfo
import top.alazeprt.aonebot.action.GetGroupMemberList
import top.alazeprt.aonebot.event.Listener
import top.alazeprt.aonebot.event.SubscribeBotEvent
import top.alazeprt.aonebot.event.message.GroupMessageEvent
import top.alazeprt.aonebot.event.notice.GroupMemberDecreaseEvent
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.bot.BotProvider
import top.alazeprt.aqqbot.handler.CommandHandler
import top.alazeprt.aqqbot.handler.InformationHandler
import top.alazeprt.aqqbot.handler.WhitelistHandler
import top.alazeprt.aqqbot.util.AFormatter
import top.alazeprt.aqqbot.util.LogLevel

class AQBListener(val plugin: AQQBot) : Listener {
    @SubscribeBotEvent
    fun onGroupMessage(event: GroupMessageEvent) {
        if (!plugin.enableGroups.contains(event.groupId.toString())) {
            return
        }
        var message = ""
        val oneBotClient = BotProvider.getBot()
        synchronized(oneBotClient!!) {
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
                val handleInfo = InformationHandler(plugin).handle(message, event)
                val handleWl = WhitelistHandler(plugin).handle(message, event)
                val handleCommand = CommandHandler(plugin).handle(message, event)
                var handleCustom = false
                plugin.customCommands.forEach {
                    if (it.handle(message, event.senderId.toString(), event.groupId.toString())) {
                        handleCustom = true
                        return@forEach
                    }
                }
                oneBotClient.action(GetGroupMemberInfo(event.groupId, event.senderId)) sendAction@ { member ->
                    if (!(canForwardMessage(message) != null && !(handleInfo || handleWl || handleCommand || handleCustom))) {
                        return@sendAction
                    }
                    val newMessage: String = canForwardMessage(message)?: return@sendAction
                    plugin.adapter!!.broadcastMessage(
                        AFormatter.pluginToChat(
                            plugin.getMessageManager().get("game.chat_from_qq", mutableMapOf(
                                "groupId" to event.groupId.toString(),
                                "userName" to member.card,
                                "message" to newMessage))
                        )
                    )
                }
            })
        }
    }

    @SubscribeBotEvent
    fun onMemberLeave(event: GroupMemberDecreaseEvent) {
        val userId = event.userId
        if (!plugin.hasQQ(userId)) {
            return
        }
        val playerName = plugin.getPlayerByQQ(userId)
        val nameList = playerName.map { it.getName() }
        plugin.removePlayer(userId)
        plugin.submit {
            plugin.adapter!!.getPlayerList().forEach {
                if (nameList.contains(it.getName())) {
                    it.kick(plugin.getMessageManager().get("game.kick_when_unbind"))
                }
            }
        }
    }

    private fun canForwardMessage(message: String): String? {
        if (!plugin.generalConfig.getBoolean("chat.group_to_server.enable")) {
            return null
        }
        val formatter = AFormatter(plugin)
        var newMessage = message;
        if (message.length > plugin.generalConfig.getInt("chat.max_forward_length")) {
            newMessage = newMessage.substring(0, plugin.generalConfig.getInt("chat.max_forward_length")) + "..."
        }
        if (plugin.generalConfig.getStringList("chat.group_to_server.prefix").contains("")) {
            return formatter.regexFilter(plugin.generalConfig.getStringList("chat.group_to_server.filter"), newMessage)
        }
        plugin.generalConfig.getStringList("chat.group_to_server.prefix").forEach {
            if (newMessage.startsWith(it)) {
                return formatter.regexFilter(plugin.generalConfig.getStringList("chat.group_to_server.filter"), newMessage.substring(it.length))
            }
        }
        return null
    }
}