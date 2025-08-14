package top.alazeprt.aqqbot.command.sub

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.bot.BotProvider
import top.alazeprt.aqqbot.command.ACommand
import top.alazeprt.aqqbot.profile.ASender

class SubSend(val plugin: AQQBot): ACommand {
    override fun onCommand(
        command: String,
        sender: ASender,
        args: List<String>
    ) {
        if (!sender.hasPermission("aqqbot.command.send")) {
            sender.sendMessage(Component.text("你没有权限使用该命令!", NamedTextColor.RED))
            return
        }
        if (args.size != 3) {
            sender.sendMessage(Component.text("用法: /aqqbot send <群号> <消息>"))
            return
        }
        val groupId = args[1].toLong()
        val message = args[2]
        BotProvider.getBot()!!.action(SendGroupMessage(groupId, message))
        sender.sendMessage(Component.text("消息已发送到群 $groupId", NamedTextColor.GREEN))
    }
}