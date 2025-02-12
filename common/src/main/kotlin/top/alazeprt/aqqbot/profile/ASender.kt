package top.alazeprt.aqqbot.profile

import net.kyori.adventure.text.Component

interface ASender {
    @Deprecated("use `net.kyori.adventure.text.Component` instead")
    fun sendMessage(message: String)

    fun sendMessage(message: Component)

    fun hasPermission(permission: String): Boolean
}