package top.alazeprt.aqqbot.util

import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.bot.BotProvider
import top.alazeprt.aqqbot.profile.AOfflinePlayer

abstract class ACustom(val plugin: AQQBot, val command: List<String>, val execute: List<String>, val unbind_execute: List<String>,
              val output: List<String>, val unbind_output: List<String>, val format: Boolean, val account: Int) {
    fun handle(input: String, userId: String, groupId: String): Boolean {
        val map = matches(input)?: return false
        val player: List<String> = plugin.getPlayerByQQ(userId.toLong()).map { it.getName() }
        var outputString: String
        if (player.isEmpty()) {
            outputString = mapFormat(unbind_output.joinToString("\n"), map)
            if (unbind_execute.isNotEmpty() && unbind_execute[0].isNotEmpty()) {
                plugin.submit {
                    unbind_execute.forEach {
                        var str = it
                        if (plugin.getPlayerByQQ(userId.toLong()).isNotEmpty()) {
                            str = it.replace("\$player", plugin.getPlayerByQQ(userId.toLong())[0].getName())
                        }
                        plugin.submit {
                            plugin.submitCommand(mapFormat(str, map))
                        }
                    }
                }
            }
            outputString = setPlaceholders(null, outputString)
            if (format) {
                outputString = AFormatter.pluginClear(outputString)
                outputString = AFormatter.chatClear(outputString)
            }
            if (outputString.contains("\$random\n")) {
                val optionsOutput: List<String> = outputString.split("\$random\n")
                val outputList = optionsOutput.random()
                BotProvider.getBot()?.action(SendGroupMessage(groupId.toLong(), outputList))
            } else {
                BotProvider.getBot()?.action(SendGroupMessage(groupId.toLong(), outputString))
            }
        } else {
            outputString = mapFormat(output.joinToString("\n"), map)
            if (execute.isNotEmpty() && execute[0].isNotEmpty()) {
                plugin.submit {
                    execute.forEach {
                        var str = it
                        if (plugin.getPlayerByQQ(userId.toLong()).isNotEmpty()) {
                            str = it.replace("\$player", plugin.getPlayerByQQ(userId.toLong())[0].getName())
                        }
                        plugin.submitCommand(mapFormat(str, map))
                    }
                }
            }
            val playerName = player[if (player.size < account) 0 else account - 1]
            outputString = setPlaceholders(plugin.adapter!!.getOfflinePlayer(playerName), outputString)
            if (format) {
                outputString = AFormatter.pluginClear(outputString)
                outputString = AFormatter.chatClear(outputString)
            }
            if (outputString.contains("\$random\n")) {
                val optionsOutput: List<String> = outputString.split("\$random\n")
                val outputList = optionsOutput.random()
                BotProvider.getBot()?.action(SendGroupMessage(groupId.toLong(), outputList))
            } else {
                BotProvider.getBot()?.action(SendGroupMessage(groupId.toLong(), outputString))
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

    abstract fun setPlaceholders(player: AOfflinePlayer?, text: String): String
}