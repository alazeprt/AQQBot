package top.alazeprt.aqqbot.event

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import top.alazeprt.aqqbot.AQQBot

class AJoinEvent : Listener {
    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        if (event.player.name !in AQQBot.dataMap.values) {
            event.player.kickPlayer("[AQQBot] 你的账户还没有绑定QQ!" +
                    "\n请通过在QQ群发送 \"${AQQBot.config.getString("whitelist.prefix.bind")} <游戏名称>\" 绑定账户")
        }
    }
}