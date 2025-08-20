package top.alazeprt.aqqbot.api.event.game

import top.alazeprt.aqqbot.api.event.APIEvent

class PrePlayerChatEvent(val name: String, val userId: Long, val message: String): APIEvent {

    private var isCanceled = false
    private var reason: String? = null

    fun cancel() {
        isCanceled = true
    }

    fun cancel(reason: String) {
        this.reason = reason
        isCanceled = true
    }

    fun isCanceled() = isCanceled

    fun getReasonMsg() = reason
}