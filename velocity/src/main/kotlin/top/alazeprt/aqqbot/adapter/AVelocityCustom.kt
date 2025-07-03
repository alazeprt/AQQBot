package top.alazeprt.aqqbot.adapter

import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import top.alazeprt.aqqbot.util.ACustom
import top.alazeprt.aqqbot.util.AImage

class AVelocityCustom(plugin: AQQBot, command: List<String>, execute: List<String>, unbind_execute: List<String>,
                      output: List<String>, unbind_output: List<String>, image: AImage?, unbind_image: AImage?, format: Boolean,
                      account: Int)
    : ACustom(plugin, command, execute, unbind_execute, output, unbind_output, image, unbind_image, format, account) {

    override fun setPlaceholders(player: AOfflinePlayer?, text: String): String {
        return text
    }
}