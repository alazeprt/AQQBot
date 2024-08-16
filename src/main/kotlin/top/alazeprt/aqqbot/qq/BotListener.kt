package top.alazeprt.aqqbot.qq

import cn.evole.onebot.client.annotations.SubscribeEvent
import cn.evole.onebot.client.interfaces.Listener
import cn.evole.onebot.sdk.event.message.GroupMessageEvent
import taboolib.module.configuration.Configuration
import taboolib.module.configuration.Type
import taboolib.module.configuration.util.asMap
import top.alazeprt.aqqbot.AQQBot

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
        if (message.startsWith(AQQBot.config.getString("whitelist.prefix.bind")?: return)) {
            val qqId = event.sender.userId
            if (AQQBot.dataMap.containsKey(qqId)) {
                AQQBot.oneBotClient.bot.sendGroupMsg(event.groupId, "你已经绑定过了!", true)
                return
            }
            val playerName = message.substring(
                AQQBot.config.getString("whitelist.prefix.bind")!!.length + 1
            )
            if (!validateName(playerName)) {
                AQQBot.oneBotClient.bot.sendGroupMsg(event.groupId, "名称不合法! (名称只能由字母、数字、下划线组成)", true)
                return
            }
            AQQBot.dataMap.values.forEach {
                if (it == playerName) {
                    AQQBot.oneBotClient.bot.sendGroupMsg(event.groupId, "该名称已被他人占用!", true)
                    return
                }
            }
            AQQBot.dataMap[qqId] = playerName
            AQQBot.oneBotClient.bot.sendGroupMsg(event.groupId, "绑定成功!", true)
        } else if (message.startsWith(AQQBot.config.getString("whitelist.prefix.unbind")?: return)) {
            val qqId = event.sender.userId
            if (!AQQBot.dataMap.containsKey(qqId)) {
                AQQBot.oneBotClient.bot.sendGroupMsg(event.groupId, "你还没有绑定过!", true)
                return
            }
            val playerName = message.substring(
                AQQBot.config.getString("whitelist.prefix.bind")!!.length + 1
            )
            AQQBot.dataMap.forEach { (k, v) ->
                if (v == playerName && k == qqId) {
                    AQQBot.dataMap.remove(k)
                    AQQBot.oneBotClient.bot.sendGroupMsg(event.groupId, "解绑成功!", true)
                    return
                } else if (k == qqId) {
                    AQQBot.oneBotClient.bot.sendGroupMsg(event.groupId, "该名称不是你绑定的! " +
                            "你绑定的名称为: $v", true)
                    return
                }
            }
            AQQBot.oneBotClient.bot.sendGroupMsg(event.groupId, "该名称尚未绑定过/不是你绑定的!", true)
        }
    }

    private fun validateName(name: String): Boolean {
        val regex = "^\\w+\$"
        return name.matches(regex.toRegex())
    }
}