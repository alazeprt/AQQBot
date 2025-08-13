package top.alazeprt.aqqbot.api.event.qq

import top.alazeprt.aqqbot.api.event.APIEvent
import top.alazeprt.aqqbot.api.event.qq.PreInformationEvent.Type
import top.alazeprt.aqqbot.api.event.qq.reason.BindCancelReason
import top.alazeprt.aqqbot.api.event.qq.reason.InformationCancelReason

class PostInformationEvent(val groupId: Long, val userId: Long, val type: Type, val isCanceled: Boolean, val reason: InformationCancelReason?):
    APIEvent {
    fun getReasonMsg(): String {
        return when (reason) {
            InformationCancelReason.NOT_ENABLE -> "该功能未启用"
            InformationCancelReason.NOT_INSTALLED_DEPENDENCY -> "缺少依赖"
            InformationCancelReason.CANCEL_BY_PLUGIN -> "插件取消"
            else -> "未知原因"
        }
    }

    fun getType(): String {
        return when (type) {
            Type.PLAYER_LIST -> "PLAYER_LIST"
            Type.TPS -> "TPS"
            Type.MSPT -> "MSPT"
            Type.CPU_USAGE -> "CPU_USAGE"
        }
    }
}