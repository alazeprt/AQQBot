package top.alazeprt.aqqbot.command

import top.alazeprt.aqqbot.profile.ASender

interface ACommand {
    fun onCommand(command: String, sender: ASender, args: List<String>)

    fun onComplete(args: List<String>): List<String> {
        return emptyList()
    }
}