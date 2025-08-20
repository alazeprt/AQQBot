package top.alazeprt.aqqbot.command.sub

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.bot.BotProvider
import top.alazeprt.aqqbot.command.ACommand
import top.alazeprt.aqqbot.profile.ASender
import top.alazeprt.aqqbot.util.AFormatter

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
        if (args.size < 3) {
            val usageMessage = plugin.messageManager.get("game.send.usage", null)
            sender.sendMessage(AFormatter.pluginToChat(usageMessage))
            return
        }
        val groupId = try {
            args[1].toLong()
        } catch (e: NumberFormatException) {
            val invalidIdMessage = plugin.messageManager.get(
                "game.send.invalid_group_id",
                mapOf("group_id" to args[1]),
                null
            )
            sender.sendMessage(AFormatter.pluginToChat(invalidIdMessage))
            return
        }
        val message = args.drop(2).joinToString(" ")
        BotProvider.getBot()!!.action(SendGroupMessage(groupId, message))

        val successMessage = plugin.messageManager.get(
            "game.send.success",
            mapOf("group_id" to groupId.toString()),
            null
        )
        sender.sendMessage(AFormatter.pluginToChat(successMessage))    }
}