package top.alazeprt.aqqbot.api.event.qq

import top.alazeprt.aqqbot.api.event.APIEvent
import top.alazeprt.aqqbot.api.event.qq.reason.RemoteCommandCancelEvent

class PostRemoteCommandEvent(val groupId: Long, val senderId: Long, val command: String, val isCanceled: Boolean, val reason: RemoteCommandCancelEvent?): APIEvent {
    fun getReasonMsg(): String {
        return when (reason) {
            RemoteCommandCancelEvent.NOT_ENABLE -> "该功能未开启"
            RemoteCommandCancelEvent.NO_PERMISSION -> "你没有权限使用该功能"
            RemoteCommandCancelEvent.CANCEL_BY_PLUGIN -> "插件取消了该功能"
            else -> "未知原因"
        }
    }
}