package top.alazeprt.aqqbot.qq

import org.bukkit.Bukkit
import top.alazeprt.aonebot.event.Listener
import top.alazeprt.aonebot.event.SubscribeBotEvent
import top.alazeprt.aonebot.event.message.GroupMessageEvent
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.AQQBot.customCommands
import top.alazeprt.aqqbot.AQQBot.isBukkit
import top.alazeprt.aqqbot.handler.InformationHandler
import top.alazeprt.aqqbot.handler.WhitelistHandler
import top.alazeprt.aqqbot.util.AI18n.get

class BotListener : Listener {
    @SubscribeBotEvent
    fun onGroupMessage(event: GroupMessageEvent) {
        if (!AQQBot.enableGroups.contains(event.groupId.toString())) {
            return
        }
        val message = event.message
        val handleInfo = InformationHandler.handle(message, event)
        val handleWl = WhitelistHandler.handle(message, event)
        var handleCustom = false
        customCommands.forEach {
            if (it.handle(message?: return@forEach, event.senderId.toString(), event.groupId.toString())) {
                handleCustom = true
                return@forEach
            }
        }
        if(AQQBot.config.getBoolean("chat.group_to_server") && !(handleInfo || handleWl || handleCustom) && isBukkit) {
            Bukkit.broadcastMessage(formatString(get("game.chat_from_qq", mutableMapOf("groupId" to event.groupId.toString(),
                "userName" to event.senderNickName,
                "message" to message))))
        }
    }

    private fun formatString(input: String): String {
        return input.replace(Regex("&([0-9a-fklmnor])")) { matchResult ->
            "§" + matchResult.groupValues[1]
        }
    }
}