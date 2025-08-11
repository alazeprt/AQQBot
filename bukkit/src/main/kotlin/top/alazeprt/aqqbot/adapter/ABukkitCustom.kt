package top.alazeprt.aqqbot.adapter

import me.clip.placeholderapi.PlaceholderAPI
import org.bukkit.Bukkit
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import top.alazeprt.aqqbot.util.ACustom
import top.alazeprt.aqqbot.util.AImage

class ABukkitCustom(plugin: AQQBot, name: String, command: List<String>, execute: List<String>, unbind_execute: List<String>,
                    output: List<String>, unbind_output: List<String>, image: AImage?, unbind_image: AImage?, format: Boolean,
                    account: Int, enable: Boolean)
    : ACustom(plugin, name, command, execute, unbind_execute, output, unbind_output, image, unbind_image, format, account, enable) {
    override fun setPlaceholders(player: AOfflinePlayer?, text: String): String {
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI")
        } catch (e: ClassNotFoundException) {
            return text
        }
        return PlaceholderAPI.setPlaceholders(if (player == null) null else Bukkit.getOfflinePlayer(player.getUUID()), text)
    }

}