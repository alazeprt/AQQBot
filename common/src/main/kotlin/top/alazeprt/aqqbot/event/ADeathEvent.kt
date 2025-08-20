package top.alazeprt.aqqbot.event

import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.bot.BotProvider
import top.alazeprt.aqqbot.profile.APlayer

class ADeathEvent(val plugin: AQQBot, private val player: APlayer, private val reason: String): AEvent {
    override fun handle() {
        plugin.debugModule?.debugLogger?.log("receive death message: ${player.getName()} died, reason: $reason")
        plugin.submitAsync {
            plugin.enableGroups.forEach {
                if (!plugin.generalConfig.getBoolean("notify.player_death.enable", it.key.toLong())) return@forEach
                BotProvider.getBot()?.action(
                    SendGroupMessage(it.key.toLong(), plugin.setPlaceholders(player, plugin.generalConfig.getString("notify.player_death.message", it.key.toLong()))
                        .replace("\${playerName}", player.getName())
                        .replace("\${userId}", plugin.getQQByPlayer(player).toString())
                        .replace("\${reason}", reason), true)
                )
            }
        }
    }
}