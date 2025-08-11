package top.alazeprt.aqqbot.event

import io.github.hello09x.fakeplayer.core.Main
import io.github.hello09x.fakeplayer.core.manager.FakeplayerManager
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.player.PlayerQuitEvent
import top.alazeprt.aqqbot.AQQBotBukkit
import top.alazeprt.aqqbot.adapter.BukkitPlayer

class BukkitEventHandler(val plugin: AQQBotBukkit) : Listener {
    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        if (event.isCancelled) {
            return
        }
        AChatEvent(plugin, BukkitPlayer(event.player), event.message).handle()
    }

    @EventHandler
    fun onJoin(event: PlayerLoginEvent) {
        if (plugin.fakePlayer) {
            val manager = Main.getInjector().getInstance(FakeplayerManager::class.java)
            if (manager.isFake(event.player)) {
                return
            }
        }
        AJoinEvent(plugin, BukkitPlayer(event.player)) {
            event.result = PlayerLoginEvent.Result.KICK_WHITELIST
            event.kickMessage = it
        }.handle()
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        if (plugin.fakePlayer) {
            val manager = Main.getInjector().getInstance(FakeplayerManager::class.java)
            if (manager.isFake(event.player)) {
                return
            }
        }
        AQuitEvent(plugin, BukkitPlayer(event.player)).handle()
    }
}