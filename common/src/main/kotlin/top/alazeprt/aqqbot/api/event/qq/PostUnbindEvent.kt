package top.alazeprt.aqqbot.api.event.qq

import top.alazeprt.aqqbot.api.event.APIEvent
import top.alazeprt.aqqbot.api.event.qq.reason.UnbindCancelReason

class PostUnbindEvent(val groupId: Long, val operatorId: Long, val userId: Long, val playerName: String, val isCanceled: Boolean, val reason: UnbindCancelReason?):
    APIEvent {
    fun getReasonMsg(): String {
        return when (reason) {
            UnbindCancelReason.NOT_BIND -> "该用户未绑定"
            UnbindCancelReason.BIND_BY_OTHER -> "该用户已被其他QQ号绑定"
            UnbindCancelReason.CANCEL_BY_PLUGIN -> "解绑操作已被插件取消"
            else -> "未知原因"
        }
    }
}