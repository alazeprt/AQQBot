package top.alazeprt.aqqbot.adapter

import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import top.alazeprt.aqqbot.AQQBotBukkit
import top.alazeprt.aqqbot.profile.ASender

class BukkitSender(val sender: CommandSender) : ASender {
    override fun sendMessage(message: String) {
        sender.sendMessage(message)
    }

    override fun sendMessage(message: Component) {
        AQQBotBukkit.audience.sender(sender).sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return sender.hasPermission(permission)
    }
}