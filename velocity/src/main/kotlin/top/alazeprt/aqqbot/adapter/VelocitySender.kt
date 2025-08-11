package top.alazeprt.aqqbot.adapter

import com.velocitypowered.api.command.CommandSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import top.alazeprt.aqqbot.profile.ASender

class VelocitySender(val sender: CommandSource): ASender {
    override fun sendMessage(message: String) {
        sender.sendMessage(Component.text(message))
    }

    override fun sendMessage(message: TextComponent) {
        sender.sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return sender.hasPermission(permission)
    }

    override fun getName(): String {
        return sender.toString()
    }
}