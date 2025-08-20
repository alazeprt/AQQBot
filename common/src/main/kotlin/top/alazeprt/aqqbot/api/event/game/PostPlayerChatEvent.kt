package top.alazeprt.aqqbot.api.event.game

import top.alazeprt.aqqbot.api.event.APIEvent

class PostPlayerChatEvent(val name: String, val userId: Long, val message: String, val isCanceled: Boolean, val reason: String?): APIEvent {
    fun getReasonMsg() = reason ?: ""
}