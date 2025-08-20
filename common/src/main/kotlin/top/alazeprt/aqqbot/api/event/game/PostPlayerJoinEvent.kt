package top.alazeprt.aqqbot.api.event.game

import top.alazeprt.aqqbot.api.event.APIEvent
import top.alazeprt.aqqbot.api.event.game.reason.JoinFailedReason

class PostPlayerJoinEvent(val name: String, val userId: Long, val isCanceled: Boolean, val reason: JoinFailedReason?): APIEvent {
    fun getReasonMsg(): String? {
        return when (reason) {
            JoinFailedReason.CANCEL_BY_PLUGIN -> "被其它插件取消"
            JoinFailedReason.UNBIND -> "未绑定"
            null -> null
        }
    }
}