package top.alazeprt.aqqbot.command

import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.command.sub.*
import top.alazeprt.aqqbot.profile.ASender

class AParentCommand(val plugin: AQQBot) : ACommand {
    override fun onCommand(command: String, sender: ASender, args: List<String>) {
        if (args.isEmpty()) {
            SubHelp(plugin).onCommand(command, sender, args)
        } else when (args[0]) {
            "forcebind" -> SubForceBind(plugin).onCommand(command, sender, args)
            "forceunbind" -> SubForceUnbind(plugin).onCommand(command, sender, args)
            "query" -> SubQuery(plugin).onCommand(command, sender, args)
            "reload" -> SubReload(plugin).onCommand(command, sender, args)
            else -> SubHelp(plugin).onCommand(command, sender, args)
        }
    }
}