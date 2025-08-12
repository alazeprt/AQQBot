package top.alazeprt.aqqbot.adapter

import net.kyori.adventure.text.TextComponent
import org.bukkit.entity.Player
import top.alazeprt.aqqbot.AQQBotFolia
import top.alazeprt.aqqbot.profile.APlayer
import java.net.InetSocketAddress
import java.util.*

class FoliaPlayer(val player: Player) : APlayer {
    override fun kick(reason: String) {
        player.kickPlayer(reason)
    }

    override fun getIP(): InetSocketAddress? {
        return player.address
    }

    override fun getName(): String {
        return player.name
    }

    override fun getUUID(): UUID {
        return player.uniqueId
    }

    override fun sendMessage(message: String) {
        player.sendMessage(message)
    }

    override fun sendMessage(message: TextComponent) {
        AQQBotFolia.audience.player(player).sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }
}