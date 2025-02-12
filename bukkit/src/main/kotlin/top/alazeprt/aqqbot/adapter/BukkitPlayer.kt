package top.alazeprt.aqqbot.adapter

import net.kyori.adventure.text.Component
import org.bukkit.entity.Player
import top.alazeprt.aqqbot.AQQBotBukkit
import top.alazeprt.aqqbot.profile.APlayer
import java.util.*

class BukkitPlayer(val player: Player) : APlayer {
    override fun kick(reason: String) {
        player.kickPlayer(reason)
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

    override fun sendMessage(message: Component) {
        AQQBotBukkit.audience.player(player).sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }
}