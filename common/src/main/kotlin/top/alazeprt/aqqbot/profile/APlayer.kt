package top.alazeprt.aqqbot.profile

import net.kyori.adventure.text.Component

interface APlayer: AOfflinePlayer {
    @Deprecated("Use `net.kyori.adventure.text.Component` instead")
    fun kick(reason: String)

    fun kick(reason: Component)
}