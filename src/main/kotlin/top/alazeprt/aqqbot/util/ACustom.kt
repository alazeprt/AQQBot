package top.alazeprt.aqqbot.util

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import taboolib.common.platform.function.info
import taboolib.common.platform.function.submit
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.AQQBot.dataMap
import top.alazeprt.aqqbot.AQQBot.isBukkit
import top.alazeprt.aqqbot.AQQBot.isFileStorage
import top.alazeprt.aqqbot.AQQBot.oneBotClient
import top.alazeprt.aqqbot.DependencyImpl.Companion.withPAPI
import top.alazeprt.aqqbot.command.sender.ABukkitSender
import top.alazeprt.aqqbot.command.sender.AVCSender
import java.security.Key

class ACustom(val command: List<String>, val execute: List<String>, val unbind_execute: List<String>,
              val output: List<String>, val unbind_output: List<String>, val format: Boolean) {
    fun handle(input: String, userId: String, groupId: String): Boolean {
        val map = matches(input)?: return false
        val player: String?
        if (isFileStorage) {
            player = dataMap.getOrDefault(userId, null)
        } else {
            player = DBQuery.qqInDatabase(userId.toLong())
        }
        var outputString = mapFormat(output.joinToString("\n"), map)
        if (format) {
            outputString = AFormatter.pluginClear(outputString)
        }
        if (player == null) {
            if (unbind_execute.isNotEmpty() && unbind_execute[0].isNotEmpty()) {
                submit {
                    val sender = if (isBukkit) {
                        ABukkitSender()
                    } else {
                        AVCSender()
                    }
                    unbind_execute.forEach {
                        sender.execute(mapFormat(it, map))
                    }
                }
            }
            oneBotClient.action(SendGroupMessage(groupId.toLong(), unbind_output.joinToString("\n")))
        } else {
            if (execute.isNotEmpty() && execute[0].isNotEmpty()) {
                submit {
                    val sender = if (isBukkit) {
                        ABukkitSender()
                    } else {
                        AVCSender()
                    }
                    execute.forEach {
                        sender.execute(mapFormat(it, map))
                    }
                }
            }
            if (withPAPI) {
                outputString = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(player), outputString)
            }
            oneBotClient.action(SendGroupMessage(groupId.toLong(), outputString))
        }
        return true
    }

    private fun mapFormat(input: String, map: Map<String, String>): String {
        return input.replace(Regex("\\$\\{([^}]+)}")) { match ->
            val key = match.groupValues[1]
            info(key)
            map[key] ?: ""
        }
    }

    private fun matches(string: String, commandPattern: String): Map<String, String>? {
        var argsIndex = 0
        val args = mutableMapOf<String, String>()
        commandPattern.split(" ").forEach {
            if (it.startsWith("\${")) {
                if (it.contains("?:")) {
                    args.put(it.split("?:")[0].substring(2), string.split(" ")
                        .getOrElse(argsIndex) { _ -> it.split("?:")[1].substring(0, it.split("?:")[1].length - 1) })
                } else if (it.endsWith("?}")) {
                    args.put(it.substring(2, it.length - 1), string.split(" ")
                        .getOrElse(argsIndex) { _ -> "" })
                } else if (string.split(" ").getOrElse(argsIndex) { _ -> null } != null) {
                    args.put(it.substring(2, it.length - 1), string.split(" ")[argsIndex])
                } else {
                    return null
                }
            } else if (it == string.split(" ")[argsIndex]) {
                args.put(it, it)
            } else {
                return null
            }
            argsIndex++
        }
        return if (args.size >= string.split(" ").size) {
            args
        } else {
            null
        }
    }

    fun matches(string: String): Map<String, String>? {
        for (commandPattern in command) {
            val matches = matches(string, commandPattern)
            if (matches != null) return matches
        }
        return null
    }
}