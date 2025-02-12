package top.alazeprt.aqqbot.adapter

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import top.alazeprt.aqqbot.AQQBotBukkit
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import top.alazeprt.aqqbot.profile.APlayer

object BukkitAdapter : AQQBotAdapter {
    override fun getOfflinePlayer(name: String): AOfflinePlayer {
        return BukkitOfflinePlayer(Bukkit.getOfflinePlayer(name))
    }

    override fun getOnlinePlayer(name: String): APlayer? {
        return BukkitPlayer(Bukkit.getPlayer(name)?: return null)
    }

    override fun getPlayerList(): List<APlayer> {
        return Bukkit.getOnlinePlayers().map { BukkitPlayer(it) }
    }

    override fun broadcastMessage(message: String) {
        Bukkit.broadcastMessage(message)
    }

    override fun broadcastMessage(message: Component) {
        AQQBotBukkit.audience.server("").sendMessage(message)
    }

}