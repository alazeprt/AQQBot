package top.alazeprt.aqqbot.event

import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.event.AEventUtil.playerStatusHandler
import top.alazeprt.aqqbot.profile.APlayer

class AQuitEvent(val plugin: AQQBot, private val player: APlayer): AEvent {
    override fun handle() {
        playerStatusHandler(plugin, player.getName(), false)
    }
}