package top.alazeprt.aqqbot.adapter

import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import top.alazeprt.aqqbot.util.ACustom
import top.alazeprt.aqqbot.util.AImage
import top.alazeprt.aqqbot.util.AWeb

class AFabricCustom(plugin: AQQBot, name: String, command: List<String>, execute: List<String>, unbind_execute: List<String>,
                    output: List<String>, unbind_output: List<String>, image: AImage?, unbind_image: AImage?, format: Boolean,
                    web: AWeb?, unbind_web: AWeb?, account: Int, enable: Boolean, permission: String)
    : ACustom(plugin, name, command, execute, unbind_execute, output, unbind_output, image, unbind_image, web, unbind_web, format, account, enable, permission) {
    override fun setPlaceholders(player: AOfflinePlayer?, text: String): String {
        return text
    }
}