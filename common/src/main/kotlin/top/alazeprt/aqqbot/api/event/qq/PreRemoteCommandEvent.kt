package top.alazeprt.aqqbot.api.event.qq

import top.alazeprt.aqqbot.api.event.APIEvent
import top.alazeprt.aqqbot.util.Cancelable

class PreRemoteCommandEvent(val groupCode: Long, val senderId: Long, val command: String): APIEvent, Cancelable {
    private var isCanceled = false

    override fun cancel() {
        isCanceled = true
    }

    fun isCanceled(): Boolean {
        return isCanceled
    }
}