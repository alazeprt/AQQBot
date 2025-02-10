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

class ACustom(val command: List<String>, val execute: List<String>, val unbind_execute: List<String>,
              val output: List<String>, val unbind_output: List<String>, val format: Boolean, val account: Int) {
    fun handle(input: String, userId: String, groupId: String): Boolean {
        val map = matches(input)?: return false
        val player: List<String>?
        if (isFileStorage) {
            player = dataMap.get(userId)
        } else {
            player = DBQuery.qqInDatabase(userId.toLong())
        }
        var outputString = mapFormat(output.joinToString("\n"), map)
        if (format) {
            outputString = AFormatter.pluginClear(outputString)
            outputString = AFormatter.chatClear(outputString)
        }
        if (player.isNullOrEmpty()) {
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
            if (withPAPI) {
                outputString = PlaceholderAPI.setPlaceholders(null, outputString)
            }
            if (outputString.contains("\$random\n")) {
                val optionsOutput: List<String> = outputString.split("\$random\n")
                val outputList = optionsOutput.random()
                oneBotClient.action(SendGroupMessage(groupId.toLong(), outputList))
            } else {
                oneBotClient.action(SendGroupMessage(groupId.toLong(), outputString))
            }
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
                outputString = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(player.get(if (player.size < account) 0 else account - 1)), outputString)
            }
            if (outputString.contains("\$random\n")) {
                val optionsOutput: List<String> = outputString.split("\$random\n")
                val outputList = optionsOutput.random()
                oneBotClient.action(SendGroupMessage(groupId.toLong(), outputList))
            } else {
                oneBotClient.action(SendGroupMessage(groupId.toLong(), outputString))
            }
        }
        return true
    }

    private fun mapFormat(input: String, map: Map<String, String>): String {
        return input.replace(Regex("\\$\\{([^}]+)}")) { match ->
            val key = match.groupValues[1]
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
            } else if (it.startsWith("\$regex:")) {
                val regex = it.substring(8, it.length - 1)
                if (string.split(" ").getOrElse(argsIndex) { _ -> null } != null && string.split(" ")[argsIndex].matches(Regex(regex))) {
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