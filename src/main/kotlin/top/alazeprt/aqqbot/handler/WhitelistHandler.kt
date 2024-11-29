package top.alazeprt.aqqbot.handler

import top.alazeprt.aonebot.action.GetGroupMemberInfo
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aonebot.event.message.GroupMessageEvent
import top.alazeprt.aonebot.util.GroupRole
import top.alazeprt.aqqbot.AQQBot

class WhitelistHandler {
    companion object {
        private fun bind(userId: String, groupId: Long, playerName: String) {
            if (AQQBot.dataMap.containsKey(userId)) {
                AQQBot.oneBotClient.action(
                    SendGroupMessage(groupId, "你已经绑定过了!", true))
                return
            }
            if (!validateName(playerName)) {
                AQQBot.oneBotClient.action(SendGroupMessage(groupId, "名称不合法! (名称只能由字母、数字、下划线组成)", true))
                return
            }
            AQQBot.dataMap.values.forEach {
                if (it == playerName) {
                    AQQBot.oneBotClient.action(SendGroupMessage(groupId, "该名称已被他人占用!", true))
                    return
                }
            }
            AQQBot.dataMap[userId] = playerName
            AQQBot.oneBotClient.action(SendGroupMessage(groupId, "绑定成功!", true))
        }
        
        private fun unbind(userId: String, groupId: Long, playerName: String) {
            if (!AQQBot.dataMap.containsKey(userId)) {
                AQQBot.oneBotClient.action(SendGroupMessage(groupId, "你还没有绑定过!", true))
                return
            }
            AQQBot.dataMap.forEach { (k, v) ->
                if (v == playerName && k == userId) {
                    AQQBot.dataMap.remove(k)
                    AQQBot.oneBotClient.action(SendGroupMessage(groupId, "解绑成功!", true))
                    return
                } else if (k == userId) {
                    AQQBot.oneBotClient.action(SendGroupMessage(groupId, "该名称不是你绑定的! " +
                            "你绑定的名称为: $v", true))
                    return
                }
            }
            AQQBot.oneBotClient.action(SendGroupMessage(groupId, "该名称尚未绑定过/不是你绑定的!", true))
        }

        private fun validateName(name: String): Boolean {
            val regex = "^\\w+\$"
            return name.matches(regex.toRegex())
        }

        fun handle(message: String, event: GroupMessageEvent): Boolean {
            if (!AQQBot.config.getBoolean("whitelist.enable")) {
                return false
            }
            if (message.split(" ").size < 2) return false
            AQQBot.config.getStringList("whitelist.prefix.bind").forEach {
                if (message.lowercase().startsWith(it.lowercase())) {
                    val playerName = message.split(" ")[1]
                    AQQBot.oneBotClient.action(GetGroupMemberInfo(event.groupId, event.senderId)) { sender ->
                        if (message.split(" ").size == 3 && (sender.role == GroupRole.ADMIN || sender.role == GroupRole.OWNER)) {
                            WhitelistAdminHandler.handle(message, event, "bind")
                        } else {
                            bind(event.senderId.toString(), event.groupId, playerName)
                        }
                    }
                    return true
                }
            }
            AQQBot.config.getStringList("whitelist.prefix.unbind").forEach {
                if (message.lowercase().startsWith(it.lowercase())) {
                    val playerName = message.substring(it.length + 1)
                    AQQBot.oneBotClient.action(GetGroupMemberInfo(event.groupId, event.senderId)) { sender ->
                        if (message.split(" ").size == 3 && (sender.role == GroupRole.ADMIN || sender.role == GroupRole.OWNER)) {
                            WhitelistAdminHandler.handle(message, event, "unbind")
                        } else {
                            unbind(event.senderId.toString(), event.groupId, playerName)
                        }
                    }
                    return true
                }
            }
            return false
        }
    }
}