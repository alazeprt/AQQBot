package top.alazeprt.aqqbot.api.event.game

import top.alazeprt.aqqbot.api.event.APIEvent

class PlayerDeathEvent(val name: String, val userId: Long, val reason: String): APIEvent {
}