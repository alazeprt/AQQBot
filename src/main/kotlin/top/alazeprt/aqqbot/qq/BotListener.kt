package top.alazeprt.aqqbot.qq

import cn.evole.onebot.client.annotations.SubscribeEvent
import cn.evole.onebot.client.interfaces.Listener
import cn.evole.onebot.sdk.event.message.GroupMessageEvent
import org.bukkit.Bukkit
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.handler.InformationHandler
import top.alazeprt.aqqbot.handler.WhitelistHandler

class BotListener : Listener {
    @SubscribeEvent
    fun onGroupMessage(event: GroupMessageEvent) {
        if (!AQQBot.enableGroups.contains(event.groupId.toString())) {
            return
        }
        val rawData = "{\"list\":" + event.message.toString() + "}"
        val messageList = Configuration.loadFromString(rawData, Type.JSON)
            .getMapList("list")
        var message = ""
        messageList.forEach {
            if (it["type"] == "at") {
                return
            } else if (it["type"] == "text") {
                val dataMap = it["data"] as? Map<*, *>
                message += dataMap?.get("text").toString()
            }
        }
        InformationHandler.handle(message, event)
        WhitelistHandler.handle(message, event)
    }
}