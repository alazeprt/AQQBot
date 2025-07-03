package top.alazeprt.aqqbot.event

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import top.alazeprt.aqqbot.AQQBotBukkit
import top.alazeprt.aqqbot.adapter.BukkitPlayer

class BukkitEventHandler(val plugin: AQQBotBukkit) : Listener {
    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        AChatEvent(plugin, BukkitPlayer(event.player), event.message).handle()
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        AJoinEvent(plugin, BukkitPlayer(event.player)).handle()
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        AQuitEvent(plugin, BukkitPlayer(event.player)).handle()
    }
}