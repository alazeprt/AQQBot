package top.alazeprt.aqqbot.adapter

import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import top.alazeprt.aqqbot.util.ACustom

class AVelocityCustom(plugin: AQQBot, command: List<String>, execute: List<String>, unbind_execute: List<String>,
                    output: List<String>, unbind_output: List<String>, format: Boolean, account: Int)
    : ACustom(plugin, command, execute, unbind_execute, output, unbind_output, format, account) {

    override fun setPlaceholders(player: AOfflinePlayer?, text: String): String {
        return text
    }
}