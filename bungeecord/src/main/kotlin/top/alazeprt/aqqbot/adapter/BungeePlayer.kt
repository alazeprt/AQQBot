package top.alazeprt.aqqbot.adapter

import net.kyori.adventure.text.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import top.alazeprt.aqqbot.AQQBotBungee
import top.alazeprt.aqqbot.profile.APlayer
import java.net.InetSocketAddress
import java.util.UUID

class BungeePlayer(val player: ProxiedPlayer): APlayer {
    override fun kick(reason: String) {
        player.disconnect(reason)
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
        AQQBotBungee.audience.player(player).sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }
}