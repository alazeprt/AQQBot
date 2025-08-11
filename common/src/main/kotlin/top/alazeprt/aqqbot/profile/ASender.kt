package top.alazeprt.aqqbot.profile

import net.kyori.adventure.text.TextComponent

interface ASender {
    @Deprecated("use `net.kyori.adventure.text.Component` instead")
    fun sendMessage(message: String)

    fun sendMessage(message: TextComponent)

    fun hasPermission(permission: String): Boolean

    fun getName(): String
}