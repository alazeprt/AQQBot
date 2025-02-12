package top.alazeprt.aqqbot.hook.bukkit

import top.alazeprt.aqqbot.profile.AOfflinePlayer

abstract class PAPIHook {
    fun setPlaceholders(player: AOfflinePlayer?, message: String): String {
        return message
    }
}