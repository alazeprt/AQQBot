package top.alazeprt.aqqbot.handler

import top.alazeprt.aonebot.event.message.GroupMessageEvent
import top.alazeprt.aqqbot.AQQBot

class PlayerAtHandler(val plugin: AQQBot) {
    fun handle(message: String, event: GroupMessageEvent): Boolean {
        if (!plugin.generalConfig.getBoolean("information.at.enable", event.groupId)) return false
        val players = mutableListOf<String>()
        val commands = plugin.generalConfig.getStringList("information.at.command", event.groupId)
        for (player in plugin.adapter.getPlayerList()) {
            commands.forEach {
                if (message.contains(it.replace("\${player}", player.getName())) && !players.contains(player.getName())) {
                    players.add(player.getName())
                    plugin.generalConfig.getStringList("information.at.command", event.groupId).forEach { command ->
                        plugin.submitCommand(command, event.groupId)
                    }
                }
            }
        }
        return players.isNotEmpty()
    }
}