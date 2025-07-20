package top.alazeprt.aqqbot.api.event.qq

import top.alazeprt.aqqbot.api.event.APIEvent
import top.alazeprt.aqqbot.util.Cancelable

class PreBindEvent(val groupId: Long, val operatorId: Long, val userId: Long, val playerName: String): APIEvent, Cancelable {

    private var isCanceled = false

    override fun cancel() {
        isCanceled = true
    }

    fun isCanceled(): Boolean {
        return isCanceled
    }
}
