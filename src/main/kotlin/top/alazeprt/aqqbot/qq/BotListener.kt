package top.alazeprt.aqqbot.qq

import org.bukkit.Bukkit
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import top.alazeprt.aonebot.event.Listener
import top.alazeprt.aonebot.event.SubscribeBotEvent
import top.alazeprt.aonebot.event.message.GroupMessageEvent
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.handler.InformationHandler
import top.alazeprt.aqqbot.handler.StatsHandler
import top.alazeprt.aqqbot.handler.WhitelistHandler

class BotListener : Listener {
    @SubscribeBotEvent
    fun onGroupMessage(event: GroupMessageEvent) {
        if (!AQQBot.enableGroups.contains(event.groupId.toString())) {
            return
        }
        val message = event.message
        val handleInfo = InformationHandler.handle(message, event)
        val handleWl = WhitelistHandler.handle(message, event)
        val handleStats = StatsHandler.handle(message, event)
        if(AQQBot.config.getBoolean("chat.group_to_server") && !(handleInfo || handleWl || handleStats)) {
            Bukkit.broadcastMessage(
                "§8[§aQQ群(${event.groupId})§8] §b${event.senderNickName}: §f$message")
        }
    }
}