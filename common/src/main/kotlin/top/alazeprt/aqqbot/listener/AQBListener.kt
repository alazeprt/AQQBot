package top.alazeprt.aqqbot.listener

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import top.alazeprt.aonebot.action.GetGroupMemberList
import top.alazeprt.aonebot.event.Listener
import top.alazeprt.aonebot.event.SubscribeBotEvent
import top.alazeprt.aonebot.event.message.GroupMessageEvent
import top.alazeprt.aonebot.event.notice.GroupMemberDecreaseEvent
import top.alazeprt.aonebot.result.GroupMember
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.api.AQQBotAPI
import top.alazeprt.aqqbot.api.event.qq.ReceiveMessageEvent
import top.alazeprt.aqqbot.bot.BotProvider
import top.alazeprt.aqqbot.handler.CommandHandler
import top.alazeprt.aqqbot.handler.InformationHandler
import top.alazeprt.aqqbot.handler.PlayerAtHandler
import top.alazeprt.aqqbot.handler.WhitelistAdminHandler
import top.alazeprt.aqqbot.handler.WhitelistHandler
import top.alazeprt.aqqbot.util.AFormatter

class AQBListener(val plugin: AQQBot) : Listener {
    @SubscribeBotEvent
    fun onGroupMessage(event: GroupMessageEvent) {
        if (!plugin.enableGroups.keys.contains(event.groupId.toString())) {
            return
        }
        AQQBotAPI.fireEvent(ReceiveMessageEvent(event))
        var message = ""
        val oneBotClient = BotProvider.getBot()
        var component: Component? = null
        synchronized(oneBotClient!!) {
            oneBotClient.action(GetGroupMemberList(event.groupId)) { memberList ->
                event.jsonMessage.forEach {
                    val jsonObject = it.asJsonObject ?: return@forEach
                    if (jsonObject.get("type").asString == "text") {
                        message += jsonObject.get("data").asJsonObject.get("text").asString
                    } else if (jsonObject.get("type").asString == "image") {
                        component = plugin.handleImage(jsonObject.get("data").asJsonObject.get("file").asString)
                        message += "[图片]"
                    } else if (jsonObject.get("type").asString == "at") {
                        if (jsonObject.get("data").asJsonObject.get("qq").asString == "all") {
                            message += "@全体成员"
                            return@forEach
                        }
                        memberList.forEach { member ->
                            if (member.member.userId == jsonObject.get("data").asJsonObject.get("qq").asLong) {
                                message += "@${member.member.nickname}"
                            }
                        }
                    } else if (jsonObject.get("type").asString == "face") {
                        message += "表情包-"
                    }
                }
                plugin.debugModule?.debugLogger?.log("receive message from ${event.groupId} which is sent by ${event.senderId}: $message")
                val handleInfo = InformationHandler(plugin).handle(message, event)
                plugin.debugModule?.debugLogger?.log("is handle information?: $handleInfo")
                val handleWl = WhitelistHandler(plugin).handle(message, event)
                plugin.debugModule?.debugLogger?.log("is handle whitelist?: $handleWl")
                val handleWlAdmin = WhitelistAdminHandler(plugin).handle(message, event, memberList)
                plugin.debugModule?.debugLogger?.log("is handle whitelist admin?: $handleWlAdmin")
                val handleCommand = CommandHandler(plugin).handle(message, event, memberList)
                plugin.debugModule?.debugLogger?.log("is handle command?: $handleCommand")
                var handleCustom = false
                plugin.customCommands.forEach {
                    if (it.handle(message, event.senderId.toString(), event.groupId.toString())) {
                        handleCustom = true
                        return@forEach
                    }
                }
                plugin.debugModule?.debugLogger?.log("is handle custom command?: $handleCustom")
                var member: GroupMember? = null
                memberList.forEach { groupMember ->
                    if (groupMember.member.userId == event.senderId) {
                        member = groupMember
                    }
                }
                if (member == null) return@action
                if (!(canForwardMessage(message, event.groupId) != null && !(handleInfo || handleWlAdmin || handleWl || handleCommand || handleCustom))) {
                    return@action
                }
                val newMessage: String = canForwardMessage(message, event.groupId) ?: return@action
                val atHandle = PlayerAtHandler(plugin).handle(newMessage, event)
                plugin.debugModule?.debugLogger?.log("is handle at?: $atHandle")
                plugin.debugModule?.debugLogger?.log("forward message to server: $newMessage")
                if (component == null) {
                    plugin.adapter.broadcastMessage(
                        AFormatter.pluginToChat(
                            plugin.messageManager.get(
                                "game.chat_from_qq", mutableMapOf(
                                    "groupId" to event.groupId.toString(),
                                    "userName" to if (member!!.card.isNullOrBlank()) member!!.member.nickname else member!!.card,
                                    "message" to newMessage
                                )
                            , event.groupId)
                        )
                    )
                } else {
                    var originComponent: Component = Component.text(newMessage)
                    originComponent = originComponent.replaceText { builder ->
                        builder.matchLiteral("[图片]").replacement(component)
                    }
                    plugin.adapter.broadcastMessage(
                        plugin.messageManager.getAndFormat(
                            "game.chat_from_qq", mutableMapOf(
                                "groupId" to Component.text(event.groupId),
                                "userName" to Component.text(if (member!!.card.isNullOrBlank()) member!!.member.nickname else member!!.card),
                                "message" to originComponent
                            )
                        , event.groupId) as TextComponent
                    )
                }

            }
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
                    it.kick(plugin.messageManager.get("game.kick_when_unbind", event.groupId))
                }
            }
        }
    }

    private fun canForwardMessage(message: String, groupId: Long): String? {
        if (!plugin.generalConfig.getBoolean("chat.group_to_server.enable", groupId)) {
            return null
        }
        val formatter = plugin.toGameFormatter
        var newMessage = message;
        if (message.length > plugin.generalConfig.getInt("chat.max_forward_length", groupId)) {
            newMessage = newMessage.substring(0, plugin.generalConfig.getInt("chat.max_forward_length", groupId)) + "..."
        }
        if (plugin.generalConfig.getStringList("chat.group_to_server.prefix", groupId).contains("")) {
            val str = formatter[groupId]?.regexFilter(plugin.generalConfig.getStringList("chat.group_to_server.filter", groupId), newMessage)?: newMessage
            return if (str.contains("!CANCEL")) {
                null
            } else {
                str
            }
        }
        plugin.generalConfig.getStringList("chat.group_to_server.prefix", groupId).forEach {
            if (newMessage.startsWith(it)) {
                val str = formatter[groupId]?.regexFilter(plugin.generalConfig.getStringList("chat.group_to_server.filter", groupId), newMessage.substring(it.length))?: newMessage.substring(it.length)
                return if (str.contains("!CANCEL")) {
                    null
                } else {
                    str
                }
            }
        }
        return null
    }
}