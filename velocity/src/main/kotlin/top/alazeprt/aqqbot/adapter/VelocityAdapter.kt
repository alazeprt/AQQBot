package top.alazeprt.aqqbot.adapter

import com.velocitypowered.api.util.GameProfile
import net.kyori.adventure.text.Component
import top.alazeprt.aqqbot.AQQBotVelocity
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import top.alazeprt.aqqbot.profile.APlayer
import kotlin.jvm.optionals.getOrNull

class VelocityAdapter(val plugin: AQQBotVelocity) : AQQBotAdapter {
    override fun getOfflinePlayer(name: String): AOfflinePlayer {
        return VelocityOfflinePlayer(GameProfile.forOfflinePlayer(name))
    }

    override fun getOnlinePlayer(name: String): APlayer? {
        return plugin.server.getPlayer(name).getOrNull()?.let { VelocityPlayer(it) }
    }

    override fun getPlayerList(): List<APlayer> {
        return plugin.server.allPlayers.map { VelocityPlayer(it) }.toList()
    }

    override fun broadcastMessage(message: String) {
        plugin.server.allServers.forEach {
            it.sendMessage(Component.text(message))
        }
    }

    override fun broadcastMessage(message: Component) {
        plugin.server.allServers.forEach {
            it.sendMessage(message)
        }
    }
}