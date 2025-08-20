package top.alazeprt.aqqbot.api.event.game

import top.alazeprt.aqqbot.api.event.APIEvent

class PlayerQuitEvent(val name: String, val userId: Long): APIEvent {
}