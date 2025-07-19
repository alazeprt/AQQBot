package top.alazeprt.aqqbot.event

import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.bot.BotProvider
import top.alazeprt.aqqbot.event.AEventUtil.canForwardMessage
import top.alazeprt.aqqbot.profile.APlayer

class AChatEvent(val plugin: AQQBot, private val player: APlayer, private val message: String): AEvent {
    override fun handle() {
        plugin.debugModule?.debugLogger?.log("receive message from game: ${player.getName()}: $message")
        plugin.debugModule?.debugLogger?.log("forward message from game: ${player.getName()}: $message")
        plugin.submitAsync {
            plugin.enableGroups.forEach {
                val message = canForwardMessage(plugin, message, it.key.toLong())?: return@forEach
                BotProvider.getBot()?.action(SendGroupMessage(
                    it.key.toLong(), plugin.messageManager.
                    get("qq.chat_from_game", mutableMapOf("player" to player.getName(), "message" to message), it.key.toLong())))
            }
        }
    }
}