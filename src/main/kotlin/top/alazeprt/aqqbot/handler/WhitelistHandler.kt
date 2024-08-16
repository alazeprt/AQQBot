package top.alazeprt.aqqbot.handler

import top.alazeprt.aqqbot.AQQBot

class WhitelistHandler {
    companion object {
        fun bind(userId: String, groupId: Long, playerName: String) {
            if (AQQBot.dataMap.containsKey(userId)) {
                AQQBot.oneBotClient.bot.sendGroupMsg(groupId, "你已经绑定过了!", true)
                return
            }
            if (!validateName(playerName)) {
                AQQBot.oneBotClient.bot.sendGroupMsg(groupId, "名称不合法! (名称只能由字母、数字、下划线组成)", true)
                return
            }
            AQQBot.dataMap.values.forEach {
                if (it == playerName) {
                    AQQBot.oneBotClient.bot.sendGroupMsg(groupId, "该名称已被他人占用!", true)
                    return
                }
            }
            AQQBot.dataMap[userId] = playerName
            AQQBot.oneBotClient.bot.sendGroupMsg(groupId, "绑定成功!", true)
        }
        
        fun unbind(userId: String, groupId: Long, playerName: String) {
            if (!AQQBot.dataMap.containsKey(userId)) {
                AQQBot.oneBotClient.bot.sendGroupMsg(groupId, "你还没有绑定过!", true)
                return
            }
            AQQBot.dataMap.forEach { (k, v) ->
                if (v == playerName && k == userId) {
                    AQQBot.dataMap.remove(k)
                    AQQBot.oneBotClient.bot.sendGroupMsg(groupId, "解绑成功!", true)
                    return
                } else if (k == userId) {
                    AQQBot.oneBotClient.bot.sendGroupMsg(groupId, "该名称不是你绑定的! " +
                            "你绑定的名称为: $v", true)
                    return
                }
            }
            AQQBot.oneBotClient.bot.sendGroupMsg(groupId, "该名称尚未绑定过/不是你绑定的!", true)
        }

        private fun validateName(name: String): Boolean {
            val regex = "^\\w+\$"
            return name.matches(regex.toRegex())
        }
    }
}