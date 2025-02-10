package top.alazeprt.aqqbot.api.events

import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.util.AI18n.get

class ServerToGroupEvent(val playerName: String, var message: String): AQBEventInterface {
    override fun handle() {
        AQQBot.enableGroups.forEach {
            AQQBot.oneBotClient.action(SendGroupMessage(it.toLong(), get("qq.chat_from_game", mutableMapOf("player" to playerName, "message" to message)), true))
        }
    }
}