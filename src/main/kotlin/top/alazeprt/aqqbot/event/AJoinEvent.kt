package top.alazeprt.aqqbot.event

import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.AQQBot

class AJoinEvent : Listener {
    companion object {
        @SubscribeEvent
        fun onJoin(event: AsyncPlayerPreLoginEvent) {
            if (event.name !in AQQBot.dataMap.values) {
                event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "[AQQBot] 你的账户还没有绑定QQ!" +
                        "\n请通过在QQ群发送 \"${AQQBot.config.getStringList("whitelist.prefix.bind")[0]} <游戏名称>\" 绑定账户")
            }
        }

        @SubscribeEvent
        fun onChat(event: AsyncPlayerChatEvent) {
            if(AQQBot.config.getBoolean("chat.server_to_group")) {
                submit (async = true) {
                    AQQBot.enableGroups.forEach {
                        AQQBot.oneBotClient.action(SendGroupMessage(it.toLong(), "[服务器] ${event.player.name}: ${event.message}", true))
                    }
                }
            }
        }
    }
}