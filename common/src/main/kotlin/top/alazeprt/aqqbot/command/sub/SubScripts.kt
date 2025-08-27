package top.alazeprt.aqqbot.command.sub

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.command.ACommand
import top.alazeprt.aqqbot.profile.ASender

class SubScripts(val plugin: AQQBot): ACommand {
    override fun onCommand(command: String, sender: ASender, args: List<String>) {
        if (!sender.hasPermission("aqqbot.scripts")) {
            sender.sendMessage(Component.text("你没有权限使用此命令!", NamedTextColor.RED))
            return
        }
        when (args[1]) {
            "list" -> plugin.scriptLoader.scriptManager.list(sender)
            "download" -> if (args.size == 3) {
                plugin.scriptLoader.scriptManager.download(sender, args[2])
            } else SubHelp(plugin).onCommand(command, sender, args)
            "info" -> if (args.size == 3) {
                plugin.scriptLoader.scriptManager.info(sender, args[2])
            } else SubHelp(plugin).onCommand(command, sender, args)
            "reload" -> plugin.scriptLoader.scriptManager.reload(sender)
            "load" -> if (args.size == 3) {
                plugin.scriptLoader.scriptManager.load(sender, args[2])
            } else SubHelp(plugin).onCommand(command, sender, args)
        }
    }
}