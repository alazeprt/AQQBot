package top.alazeprt.aqqbot.command.sub

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.command.ACommand
import top.alazeprt.aqqbot.profile.ASender

class SubMarket(val plugin: AQQBot) : ACommand {
    override fun onCommand(command: String, sender: ASender, args: List<String>) {
        if (!sender.hasPermission("aqqbot.market")) {
            sender.sendMessage(Component.text("你没有权限使用此命令!", NamedTextColor.RED))
            return
        }
        when (args[1]) {
            "list" -> plugin.scriptLoader.marketManager.list(sender)
            "info" -> if (args.size == 3) {
                plugin.scriptLoader.marketManager.info(sender, args[2], null)
            } else if (args.size == 4) {
                plugin.scriptLoader.marketManager.info(sender, args[2], args[3])
            } else SubHelp(plugin).onCommand(command, sender, args)
            "install" -> if (args.size == 3) {
                plugin.scriptLoader.marketManager.install(sender, args[2], null)
            } else if (args.size == 4) {
                plugin.scriptLoader.marketManager.install(sender, args[2], args[3])
            } else SubHelp(plugin).onCommand(command, sender, args)
            "update" -> if (args.size == 3) {
                plugin.scriptLoader.marketManager.update(sender, args[2])
            } else SubHelp(plugin).onCommand(command, sender, args)
        }
    }

}