package top.alazeprt.aqqbot.adapter

import net.kyori.adventure.text.TextComponent
import net.md_5.bungee.api.ProxyServer
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.AQQBotBungee
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import top.alazeprt.aqqbot.profile.APlayer

class BungeeAdapter(val plugin: AQQBot) : AQQBotAdapter {
    override fun getOfflinePlayer(name: String): AOfflinePlayer {
        return BungeeOfflinePlayer(name)
    }

    override fun getOnlinePlayer(name: String): APlayer? {
        return if (ProxyServer.getInstance().getPlayer(name) == null) null else
            BungeePlayer(ProxyServer.getInstance().getPlayer(name))
    }

    override fun getPlayerList(): List<APlayer> {
        return ProxyServer.getInstance().players.map { BungeePlayer(it) }
    }

    override fun broadcastMessage(message: String) {
        ProxyServer.getInstance().broadcast(message)
    }

    override fun broadcastMessage(message: TextComponent) {
        AQQBotBungee.audience.all().sendMessage(message)
    }
}