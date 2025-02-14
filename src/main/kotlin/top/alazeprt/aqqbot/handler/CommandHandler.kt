package top.alazeprt.aqqbot.handler

import taboolib.common.platform.function.submit
import top.alazeprt.aonebot.action.GetGroupMemberInfo
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aonebot.event.message.GroupMessageEvent
import top.alazeprt.aonebot.util.GroupRole
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.AQQBot.config
import top.alazeprt.aqqbot.AQQBot.isBukkit
import top.alazeprt.aqqbot.AQQBot.messageConfig
import top.alazeprt.aqqbot.command.sender.ABukkitSender
import top.alazeprt.aqqbot.command.sender.AVCSender

class CommandHandler {
    companion object {
        fun handle(message: String, event: GroupMessageEvent): Boolean {
            if (!config.getBoolean("command_execution.enable")) {
                return false
            }
            if (message.split(" ").size < 2) return false
            config.getStringList("command_execution.prefix").forEach prefix@ { prefix ->
                if (message.lowercase().startsWith(prefix.lowercase())) {
                    var hasPermission = false
                    AQQBot.oneBotClient.action(GetGroupMemberInfo(event.groupId, event.senderId)) { member ->
                        config.getStringList("command_execution.allow").forEach {
                            if (it.startsWith("\$")) {
                                if (it == "\$OWNER" && member.role == GroupRole.OWNER) {
                                    hasPermission = true
                                    return@forEach
                                } else if (it == "\$ADMIN" && (member.role == GroupRole.ADMIN || member.role == GroupRole.OWNER)) {
                                    hasPermission = true
                                    return@forEach
                                } else if (it == "\$MEMBER") {
                                    hasPermission = true
                                    return@forEach
                                }
                            } else if (it == event.senderId.toString()) {
                                hasPermission = true
                                return@forEach
                            }
                        }
                        if (!hasPermission) {
                            AQQBot.oneBotClient.action(SendGroupMessage(event.groupId, messageConfig.getString("qq.no_permission")))
                        } else {
                            val commandList = message.split(" ").toMutableList()
                            commandList.removeAt(0)
                            val command = commandList.joinToString(" ")
                            AQQBot.oneBotClient.action(SendGroupMessage(event.groupId, messageConfig.getString("qq.executing_command")))
                            submit {
                                val sender = if (isBukkit) {
                                    ABukkitSender()
                                } else {
                                    AVCSender()
                                }
                                sender.execute(command)
                                submit(delay = config.getInt("command_execution.delay")*20L, async = true) {
                                    if (config.getBoolean("command_execution.format")) {
                                        AQQBot.oneBotClient.action(SendGroupMessage(event.groupId, if (sender.getFormatString().isEmpty()) messageConfig.getString("qq.execution_finished") else sender.getFormatString()))
                                    } else {
                                        AQQBot.oneBotClient.action(SendGroupMessage(event.groupId, if (sender.getRawString().isEmpty()) messageConfig.getString("qq.execution_finished") else sender.getRawString()))
                                    }
                                }
                            }
                        }
                    }
                    return true
                }
            }
            return false
        }
    }
}