package top.alazeprt.aqqbot.handler

import top.alazeprt.aonebot.action.GetGroupMemberList
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aonebot.event.message.GroupMessageEvent
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.AQQBot.isFileStorage
import top.alazeprt.aqqbot.util.AI18n.get
import top.alazeprt.aqqbot.util.DBQuery
import top.alazeprt.aqqbot.util.DBQuery.addPlayer
import top.alazeprt.aqqbot.util.DBQuery.playerInDatabase
import top.alazeprt.aqqbot.util.DBQuery.qqInDatabase
import top.alazeprt.aqqbot.util.DBQuery.removePlayer

class WhitelistAdminHandler {
    companion object {
        private fun bind(userId: String, groupId: Long, playerName: String) {
            if (isFileStorage && AQQBot.dataMap.containsKey(userId)) {
                AQQBot.dataMap.remove(userId)
            } else if (!isFileStorage && qqInDatabase(userId.toLong()) != null) {
                DBQuery.removePlayerByUserId(userId.toLong())
            }
            if (!validateName(playerName)) {
                AQQBot.oneBotClient.action(SendGroupMessage(groupId, get("qq.whitelist.invalid_name"), true))
                return
            }
            if (isFileStorage) {
                var existUserId = ""
                AQQBot.dataMap.forEach { k, v ->
                    if (v == playerName) {
                        existUserId = k
                        return@forEach
                    }
                }
                if (existUserId != "") {
                    AQQBot.dataMap.remove(existUserId)
                }
            } else {
                if (playerInDatabase(playerName)) {
                    DBQuery.removePlayerByName(playerName)
                }
            }
            if (isFileStorage) AQQBot.dataMap[userId] = playerName
            else addPlayer(userId.toLong(), playerName)
            AQQBot.oneBotClient.action(SendGroupMessage(groupId, get("qq.whitelist.bind_successful"), true))
        }

        private fun unbind(userId: String, groupId: Long, playerName: String) {
            if (isFileStorage && !AQQBot.dataMap.containsKey(userId)) {
                AQQBot.oneBotClient.action(SendGroupMessage(groupId, get("qq.whitelist.admin.not_bind", mutableMapOf(Pair("userId", userId))), true))
                return
            } else if (!isFileStorage && qqInDatabase(userId.toLong()) == null) {
                AQQBot.oneBotClient.action(SendGroupMessage(groupId, get("qq.whitelist.admin.not_bind", mutableMapOf(Pair("userId", userId))), true))
                return
            }
            if (isFileStorage) {
                AQQBot.dataMap.forEach { (k, v) ->
                    if (v == playerName && k == userId) {
                        AQQBot.dataMap.remove(k)
                        AQQBot.oneBotClient.action(SendGroupMessage(groupId, get("qq.whitelist.unbind_successful"), true))
                        return
                    } else if (k == userId) {
                        AQQBot.oneBotClient.action(SendGroupMessage(groupId, get("qq.whitelist.admin.bind_by_other", mutableMapOf(Pair("name", v))), true))
                        return
                    }
                }
            } else {
                if (qqInDatabase(userId.toLong()) != playerName) {
                    AQQBot.oneBotClient.action(SendGroupMessage(groupId, get("qq.whitelist.admin.bind_by_other", mutableMapOf(Pair("name", qqInDatabase(userId.toLong())!!))), true))
                }
                removePlayer(userId.toLong(), playerName)
                AQQBot.oneBotClient.action(SendGroupMessage(groupId, get("qq.whitelist.unbind_successful"), true))
            }
            AQQBot.oneBotClient.action(SendGroupMessage(groupId, get("qq.whitelist.admin.invalid_bind", mutableMapOf(Pair("userId", userId))), true))
        }

        fun validateName(name: String): Boolean {
            val regex = "^\\w+\$"
            return name.matches(regex.toRegex())
        }

        fun handle(message: String, event: GroupMessageEvent, action: String) {
            if (!AQQBot.config.getBoolean("whitelist.admin")) {
                return
            }
            val userId = message.split(" ")[1].toLongOrNull()
            if (userId == null) {
                AQQBot.oneBotClient.action(SendGroupMessage(event.groupId, get("qq.whitelist.admin.invalid_user_id"), true))
                return
            }
            val playerName = message.split(" ")[2]
            AQQBot.oneBotClient.action(GetGroupMemberList(event.groupId)) {
                var has = false;
                for (member in it) {
                    if (member.member.userId == userId) {
                        has = true
                        break
                    }
                }
                if (!has) {
                    AQQBot.oneBotClient.action(SendGroupMessage(event.groupId, get("qq.whitelist.admin.user_not_in_group"), true))
                    return@action
                } else {
                    if (action == "bind") {
                        bind(userId.toString(), event.groupId, playerName)
                    } else if (action == "unbind") {
                        unbind(userId.toString(), event.groupId, playerName)
                    }
                }
            }
        }
    }
}