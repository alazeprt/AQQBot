package top.alazeprt.aqqbot.adapter

import net.kyori.adventure.text.TextComponent
import org.bukkit.Bukkit
import top.alazeprt.aqqbot.AQQBotFolia
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import top.alazeprt.aqqbot.profile.APlayer

object FoliaAdapter : AQQBotAdapter {
    override fun getOfflinePlayer(name: String): AOfflinePlayer {
        return FoliaOfflinePlayer(Bukkit.getOfflinePlayer(name))
    }

    override fun getOnlinePlayer(name: String): APlayer? {
        return FoliaPlayer(Bukkit.getPlayer(name)?: return null)
    }

    override fun getPlayerList(): List<APlayer> {
        return Bukkit.getOnlinePlayers().map { FoliaPlayer(it) }
    }

    override fun broadcastMessage(message: String) {
        Bukkit.broadcastMessage(message)
    }

    override fun broadcastMessage(message: TextComponent) {
        AQQBotFolia.audience.all().sendMessage(message)
    }

}