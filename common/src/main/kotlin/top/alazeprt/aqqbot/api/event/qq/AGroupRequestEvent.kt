package top.alazeprt.aqqbot.api.event.qq

import top.alazeprt.aqqbot.api.event.APIEvent

class AGroupRequestEvent(val selfId: Long, val groupId: Long, val userId: Long, val comment: String, val isInvite: Boolean): APIEvent {
}