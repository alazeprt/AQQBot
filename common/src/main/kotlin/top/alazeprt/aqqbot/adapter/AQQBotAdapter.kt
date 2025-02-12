package top.alazeprt.aqqbot.adapter

import net.kyori.adventure.text.Component
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import top.alazeprt.aqqbot.profile.APlayer

interface AQQBotAdapter {
    fun getOfflinePlayer(name: String): AOfflinePlayer

    fun getOnlinePlayer(name: String): APlayer?

    fun getPlayerList(): List<APlayer>

    @Deprecated("Use `net.kyori.adventure.text.Component` instead")
    fun broadcastMessage(message: String)

    fun broadcastMessage(message: Component)
}