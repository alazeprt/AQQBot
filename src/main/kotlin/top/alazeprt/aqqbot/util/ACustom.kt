package top.alazeprt.aqqbot.util

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.AQQBot.dataMap
import top.alazeprt.aqqbot.AQQBot.isFileStorage
import top.alazeprt.aqqbot.AQQBot.oneBotClient
import top.alazeprt.aqqbot.DependencyImpl.Companion.withPAPI

class ACustom(val command: List<String>, val output: List<String>, val unbind_output: List<String>, val format: Boolean) {
    companion object {
        private fun formatString(input: String): String {
            return input.replace(Regex("&([0-9a-fklmnor])"), "")
        }
    }

    fun handle(input: String, userId: String, groupId: String): Boolean {
        if (!command.contains(input)) return false
        var player: String?
        if (isFileStorage) {
            player = dataMap.getOrDefault(userId, null)
        } else {
            player = DBQuery.qqInDatabase(userId.toLong())
        }
        if (player == null) {
            oneBotClient.action(SendGroupMessage(groupId.toLong(), unbind_output.joinToString("\n")))
            return true
        }
        var outputString: String = output.joinToString("\n")
        if (withPAPI) {
            outputString = PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(player), outputString)
        }
        if (format) {
            outputString = formatString(outputString)
        }
        oneBotClient.action(SendGroupMessage(groupId.toLong(), outputString))
        return true
    }
}