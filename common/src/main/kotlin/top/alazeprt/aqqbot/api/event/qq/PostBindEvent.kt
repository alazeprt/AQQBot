package top.alazeprt.aqqbot.api.event.qq

import top.alazeprt.aqqbot.api.event.APIEvent
import top.alazeprt.aqqbot.api.event.qq.reason.BindCancelReason

class PostBindEvent(val groupId: Long, val operatorId: Long, val userId: Long, val playerName: String, val isCanceled: Boolean, val reason: BindCancelReason?):
    APIEvent {
}