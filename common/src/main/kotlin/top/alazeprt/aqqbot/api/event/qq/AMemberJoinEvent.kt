package top.alazeprt.aqqbot.api.event.qq

import top.alazeprt.aqqbot.api.event.APIEvent

class AMemberJoinEvent(val groupId: Long, val userId: Long, val selfId: Long, val operatorId: Long): APIEvent {
}