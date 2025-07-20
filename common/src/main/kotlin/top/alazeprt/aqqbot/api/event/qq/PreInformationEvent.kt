package top.alazeprt.aqqbot.api.event.qq

import top.alazeprt.aqqbot.api.event.APIEvent
import top.alazeprt.aqqbot.util.Cancelable

class PreInformationEvent(val groupId: Long, val userId: Long, val type: Type): APIEvent, Cancelable {
    enum class Type {
        PLAYER_LIST,
        @Deprecated("This feature was replaced by custom commands") TPS,
        @Deprecated("This feature was replaced by custom commands") MSPT,
        @Deprecated("This feature was replaced by custom commands") CPU_USAGE
    }

    private var isCanceled = false

    override fun cancel() {
        isCanceled = true
    }

    fun isCanceled(): Boolean {
        return isCanceled
    }
}