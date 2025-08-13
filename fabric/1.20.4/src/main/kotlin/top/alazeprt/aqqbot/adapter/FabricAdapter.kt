package top.alazeprt.aqqbot.adapter

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import top.alazeprt.aqqbot.AQQBotFabric
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import top.alazeprt.aqqbot.profile.APlayer

object FabricAdapter : AQQBotAdapter {
    override fun getOfflinePlayer(name: String): AOfflinePlayer {
        return FabricOfflinePlayer.from(name)
    }

    override fun getOnlinePlayer(name: String): APlayer? {
        AQQBotFabric.server.playerManager.playerList.forEach {
            if (it.name.string == name) {
                return FabricPlayer(it)
            }
        }
        return null
    }

    override fun getPlayerList(): List<APlayer> {
        return AQQBotFabric.server.playerManager.playerList.map { FabricPlayer(it) }
    }

    override fun broadcastMessage(message: String) {
        AQQBotFabric.audience.all().sendMessage(Component.text(message))
    }

    override fun broadcastMessage(message: TextComponent) {
        AQQBotFabric.audience.all().sendMessage(message)
    }
}