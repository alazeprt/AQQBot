package top.alazeprt.aqqbot.adapter

import com.velocitypowered.api.proxy.Player
import net.kyori.adventure.text.Component
import top.alazeprt.aqqbot.profile.APlayer
import java.util.*

class VelocityPlayer(val player: Player) : APlayer {
    override fun kick(reason: String) {
        player.disconnect(Component.text(reason))
    }

    override fun getName(): String {
        return player.username
    }

    override fun getUUID(): UUID {
        return player.uniqueId
    }

    override fun sendMessage(message: String) {
        player.sendMessage(Component.text(message))
    }

    override fun sendMessage(message: Component) {
        player.sendMessage(message)
    }

    override fun hasPermission(permission: String): Boolean {
        return player.hasPermission(permission)
    }

}