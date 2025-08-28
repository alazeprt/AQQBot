package top.alazeprt.aqqbot.adapter

import net.kyori.adventure.text.TextComponent
import net.md_5.bungee.api.CommandSender
import top.alazeprt.aqqbot.AQQBotBungee
import top.alazeprt.aqqbot.profile.ASender

class BungeeSender(val sender: CommandSender): ASender {
    override fun sendMessage(message: String) {
        sender.sendMessage(message)
    }

    override fun sendMessage(message: TextComponent) {
        AQQBotBungee.audience.sender(sender).sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return sender.hasPermission(permission)
    }

    override fun getName(): String {
        return sender.name
    }
}