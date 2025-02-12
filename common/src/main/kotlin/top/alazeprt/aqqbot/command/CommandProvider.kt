package top.alazeprt.aqqbot.command

import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.command.impl.ACommandImpl

interface CommandProvider {
    fun registerCommand(command: String, handler: ACommand)

    fun loadCommands(plugin: AQQBot) {
        registerCommand("aqqbot", AParentCommand(plugin))
    }

    fun getCommandImpl(plugin: AQQBot): ACommandImpl {
        return ACommandImpl(plugin)
    }
}