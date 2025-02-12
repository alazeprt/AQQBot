package top.alazeprt.aqqbot.event

import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.bot.BotProvider
import top.alazeprt.aqqbot.event.AEventUtil.canForwardMessage
import top.alazeprt.aqqbot.event.sub.AEvent
import top.alazeprt.aqqbot.profile.APlayer

class AChatEvent(val plugin: AQQBot, private val player: APlayer, private val message: String): AEvent {
    override fun handle() {
        if (canForwardMessage(plugin, message) != null) {
            plugin.submitAsync {
                val message = canForwardMessage(plugin, message)!!
                plugin.enableGroups.forEach {
                    BotProvider.getBot()?.action(SendGroupMessage(
                        it.toLong(), plugin.getMessageManager().
                        get("qq.chat_from_game", mutableMapOf("player" to player.getName(), "message" to message))))
                }
            }
        }
    }
}