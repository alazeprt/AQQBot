package top.alazeprt.aqqbot.event

import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import taboolib.common.platform.event.SubscribeEvent
import top.alazeprt.aqqbot.AQQBot

class AJoinEvent : Listener {
    companion object {
        @SubscribeEvent
        fun onJoin(event: PlayerJoinEvent) {
            if (event.player.name !in AQQBot.dataMap.values) {
                event.player.kickPlayer("[AQQBot] 你的账户还没有绑定QQ!" +
                        "\n请通过在QQ群发送 \"${AQQBot.config.getStringList("whitelist.prefix.bind").get(0)} <游戏名称>\" 绑定账户")
            }
        }

        @SubscribeEvent
        fun onChat(event: AsyncPlayerChatEvent) {
            if(AQQBot.config.getBoolean("chat.server_to_group")) {
                AQQBot.enableGroups.forEach {
                    AQQBot.oneBotClient.bot.sendGroupMsg(it.toLong(), "[服务器] ${event.player.name}: ${event.message}", true)
                }
            }
        }
    }
}