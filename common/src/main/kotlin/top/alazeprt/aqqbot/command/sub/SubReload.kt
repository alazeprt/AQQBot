package top.alazeprt.aqqbot.command.sub

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.command.ACommand
import top.alazeprt.aqqbot.profile.ASender

class SubReload(val plugin: AQQBot): ACommand {
    override fun onCommand(command: String, sender: ASender, args: List<String>) {
        if (!sender.hasPermission("aqqbot.command.query")) {
            sender.sendMessage(Component.text("你没有权限使用该命令!", NamedTextColor.RED))
            return
        }
        plugin.reload()
        sender.sendMessage(Component.text("插件配置重载成功!", NamedTextColor.GREEN))
    }
}