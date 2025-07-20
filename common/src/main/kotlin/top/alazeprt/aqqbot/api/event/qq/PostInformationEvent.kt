package top.alazeprt.aqqbot.api.event.qq

import top.alazeprt.aqqbot.api.event.APIEvent
import top.alazeprt.aqqbot.api.event.qq.PreInformationEvent.Type
import top.alazeprt.aqqbot.api.event.qq.reason.InformationCancelReason

class PostInformationEvent(val groupId: Long, val userId: Long, val type: Type, val isCanceled: Boolean, val reason: InformationCancelReason?):
    APIEvent