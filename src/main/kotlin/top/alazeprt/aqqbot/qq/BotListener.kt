package top.alazeprt.aqqbot.qq

import cn.evole.onebot.client.annotations.SubscribeEvent
import cn.evole.onebot.client.interfaces.Listener
import cn.evole.onebot.sdk.event.message.GroupMessageEvent
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.handler.WhitelistHandler

class BotListener : Listener {
    @SubscribeEvent
    fun onGroupMessage(event: GroupMessageEvent) {
        if (!AQQBot.config.getBoolean("whitelist.enable")) {
            return
        }
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
        AQQBot.config.getStringList("whitelist.prefix.bind").forEach {
            if (message.startsWith(it)) {
                val playerName = message.substring(it.length + 1)
                WhitelistHandler.bind(event.sender.userId, event.groupId, playerName)
                return
            }
        }
        AQQBot.config.getStringList("whitelist.prefix.unbind").forEach {
            if (message.startsWith(it)) {
                val playerName = message.substring(it.length + 1)
                WhitelistHandler.unbind(event.sender.userId, event.groupId, playerName)
                return
            }
        }
    }
}