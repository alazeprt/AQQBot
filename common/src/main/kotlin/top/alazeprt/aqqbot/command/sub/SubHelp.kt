package top.alazeprt.aqqbot.command.sub

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.command.ACommand
import top.alazeprt.aqqbot.profile.ASender
import top.alazeprt.aqqbot.util.AFormatter

class SubHelp(val plugin: AQQBot) : ACommand {
    override fun onCommand(command: String, sender: ASender, args: List<String>) {
        if (sender.hasPermission("aqqbot.help")) {
            sender.sendMessage(AFormatter.pluginToChat(plugin.getMessageManager().getList("game.help")))
        } else {
            sender.sendMessage(Component.text("你没有权限使用此命令!", NamedTextColor.RED))
        }
    }
}