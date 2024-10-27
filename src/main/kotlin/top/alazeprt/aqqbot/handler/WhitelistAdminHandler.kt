package top.alazeprt.aqqbot.handler

import cn.evole.onebot.sdk.event.message.GroupMessageEvent
import top.alazeprt.aqqbot.AQQBot

class WhitelistAdminHandler {
    companion object {
        private fun bind(userId: String, groupId: Long, playerName: String) {
            if (AQQBot.dataMap.containsKey(userId)) {
                AQQBot.oneBotClient.bot.sendGroupMsg(groupId, "他(${userId})已经绑定过了!", true)
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

        private fun unbind(userId: String, groupId: Long, playerName: String) {
            if (!AQQBot.dataMap.containsKey(userId)) {
                AQQBot.oneBotClient.bot.sendGroupMsg(groupId, "他(${userId})还没有绑定过!", true)
                return
            }
            AQQBot.dataMap.forEach { (k, v) ->
                if (v == playerName && k == userId) {
                    AQQBot.dataMap.remove(k)
                    AQQBot.oneBotClient.bot.sendGroupMsg(groupId, "解绑成功!", true)
                    return
                } else if (k == userId) {
                    AQQBot.oneBotClient.bot.sendGroupMsg(groupId, "该名称不是他(${userId})绑定的! " +
                            "你绑定的名称为: $v", true)
                    return
                }
            }
            AQQBot.oneBotClient.bot.sendGroupMsg(groupId, "该名称尚未绑定过/不是他(${userId})绑定的!", true)
        }

        private fun validateName(name: String): Boolean {
            val regex = "^\\w+\$"
            return name.matches(regex.toRegex())
        }

        fun handle(message: String, event: GroupMessageEvent, action: String) {
            if (!AQQBot.config.getBoolean("whitelist.admin")) {
                return
            }
            val userId = message.split(" ")[1].toLongOrNull()
            if (userId == null) {
                AQQBot.oneBotClient.bot.sendGroupMsg(event.groupId, "请输入正确的QQ号!", true)
                return
            }
            val playerName = message.split(" ")[2]
            if (AQQBot.oneBotClient.bot.getGroupMemberInfo(event.groupId, userId, false) == null) {
                AQQBot.oneBotClient.bot.sendGroupMsg(event.groupId, "该用户不在本群!", true)
                return
            }
            if (action == "bind") {
                bind(userId.toString(), event.groupId, playerName)
            } else if (action == "unbind") {
                unbind(userId.toString(), event.groupId, playerName)
            }
        }
    }
}