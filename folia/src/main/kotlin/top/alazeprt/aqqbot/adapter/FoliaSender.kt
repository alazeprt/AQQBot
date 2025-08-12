package top.alazeprt.aqqbot.adapter

import net.kyori.adventure.text.TextComponent
import org.bukkit.command.CommandSender
import top.alazeprt.aqqbot.AQQBotFolia
import top.alazeprt.aqqbot.profile.ASender

class FoliaSender(val sender: CommandSender) : ASender {
    override fun sendMessage(message: String) {
        sender.sendMessage(message)
    }

    override fun sendMessage(message: TextComponent) {
        AQQBotFolia.audience.sender(sender).sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return sender.hasPermission(permission)
    }

    override fun getName(): String {
        return sender.name
    }
}