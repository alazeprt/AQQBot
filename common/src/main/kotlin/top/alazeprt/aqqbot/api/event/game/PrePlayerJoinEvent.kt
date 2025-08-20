package top.alazeprt.aqqbot.api.event.game

import top.alazeprt.aqqbot.api.event.APIEvent
import top.alazeprt.aqqbot.event.AJoinEvent
import top.alazeprt.aqqbot.profile.APlayer

class PrePlayerJoinEvent(val event: AJoinEvent, val name: String, val userId: Long): APIEvent {

    var isCanceled = false

    fun cancel() {
        isCanceled = true
        event.kickMethod.accept("Canceled by other extensions")
    }

    fun cancel(reason: String) {
        isCanceled = true
        event.kickMethod.accept(reason)
    }
}