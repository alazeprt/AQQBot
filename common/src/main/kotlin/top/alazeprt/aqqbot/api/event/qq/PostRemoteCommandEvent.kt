package top.alazeprt.aqqbot.api.event.qq

import top.alazeprt.aqqbot.api.event.APIEvent
import top.alazeprt.aqqbot.api.event.qq.reason.RemoteCommandCancelEvent

class PostRemoteCommandEvent(val groupCode: Long, val senderId: Long, val command: String, val isCanceled: Boolean, val reason: RemoteCommandCancelEvent?): APIEvent {

}