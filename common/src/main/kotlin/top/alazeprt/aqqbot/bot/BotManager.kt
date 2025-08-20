package top.alazeprt.aqqbot.bot

import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aonebot.action.SendPrivateMessage

object BotManager {
    fun sendGroupMessage(groupId: Long, message: String) {
        BotProvider.getBot()?.action(SendGroupMessage(groupId, message))
    }

    fun sendPrivateMessage(userId: Long, message: String) {
        BotProvider.getBot()?.action(SendPrivateMessage(userId, message))
    }
}