package top.alazeprt.aqqbot.event

import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.event.PlayerDisconnectEvent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.adapter.BungeePlayer

class BungeeEventHandler(val plugin: AQQBot): Listener {
    fun onJoin(event: PostLoginEvent) {
        AJoinEvent(plugin, BungeePlayer(event.player)) {
            event.player.disconnect(it)
        }.handle()
    }

    fun onQuit(event: PlayerDisconnectEvent) {
        AQuitEvent(plugin, BungeePlayer(event.player)).handle()
    }

    fun onChat(event: ChatEvent) {
        if (event.isCancelled || event.sender !is ProxiedPlayer) return
        AChatEvent(plugin, BungeePlayer(event.sender as ProxiedPlayer), event.message).handle()
    }
}