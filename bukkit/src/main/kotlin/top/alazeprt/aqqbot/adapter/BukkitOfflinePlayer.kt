package top.alazeprt.aqqbot.adapter

import org.bukkit.OfflinePlayer
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import java.util.*

class BukkitOfflinePlayer(val player: OfflinePlayer) : AOfflinePlayer {
    override fun getName(): String {
        return player.name?: ""
    }

    override fun getUUID(): UUID {
        return player.uniqueId
    }
}