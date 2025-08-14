package top.alazeprt.aqqbot.adapter

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.TextComponent
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import top.alazeprt.aqqbot.profile.ASender

class FabricSender(val sender: ServerCommandSource) : ASender {
    override fun sendMessage(message: String) {
        sender.sendFeedback(Text.of(message), false)
    }

    override fun sendMessage(message: TextComponent) {
        val audience = sender as Audience
        audience.sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return sender.hasPermissionLevel(4)
    }

    override fun getName(): String {
        return sender.name
    }
}