package top.alazeprt.aqqbot.command

import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.command.sub.*
import top.alazeprt.aqqbot.profile.ASender

class AParentCommand(val plugin: AQQBot) : ACommand {
    override fun onCommand(command: String, sender: ASender, args: List<String>) {
        plugin.debugModule?.debugLogger?.log("${sender.getName()} executed command: $command ${args.joinToString(" ")}")
        if (args.isEmpty()) {
            SubHelp(plugin).onCommand(command, sender, args)
        } else when (args[0]) {
            "status" -> SubStatus(plugin).onCommand(command, sender, args)
            "whitelist" -> if (args.size == 1) {
                SubHelp(plugin).onCommand(command, sender, args)
            } else when (args[1]) {
                "bind" -> SubBind(plugin).onCommand(command, sender, args)
                "unbind" -> SubUnbind(plugin).onCommand(command, sender, args)
                "query" -> SubQuery(plugin).onCommand(command, sender, args)
                "info" -> SubQuery(plugin).onCommand(command, sender, args)
                "reset" -> SubReset(plugin).onCommand(command, sender, args)
                else -> SubHelp(plugin).onCommand(command, sender, args)
            }
            "reload" -> SubReload(plugin).onCommand(command, sender, args)
            else -> SubHelp(plugin).onCommand(command, sender, args)
        }
    }

    override fun onComplete(args: List<String>): List<String> {
        return when (args.size) {
            1 -> listOf("whitelist", "status", "help", "reload")
            2 -> return when (args[0]) {
                "whitelist" -> listOf("bind", "unbind", "reset", "query", "info")
                else -> emptyList()
            }
            3 -> return when (args[0]) {
                "whitelist" -> return when (args[1]) {
                    "unbind" -> listOf("qq", "player")
                    "query" -> listOf("qq", "player")
                    "info" -> listOf("qq", "player")
                    "reset" -> listOf("qq", "player")
                    else -> emptyList()
                }
                else -> emptyList()
            }
            else -> emptyList()
        }
    }
}