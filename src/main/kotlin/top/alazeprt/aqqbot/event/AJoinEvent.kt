package top.alazeprt.aqqbot.event

import com.velocitypowered.api.event.connection.PostLoginEvent
import net.kyori.adventure.text.Component
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import taboolib.common.platform.Ghost
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.submit
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.AQQBot

object AJoinEvent {
    // Bukkit
    @Ghost
    @SubscribeEvent
    fun onJoin(event: AsyncPlayerPreLoginEvent) {
        if (event.name !in AQQBot.dataMap.values) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "[AQQBot] 你的账户还没有绑定QQ!" +
                    "\n请通过在QQ群发送 \"${AQQBot.config.getStringList("whitelist.prefix.bind")[0]} <游戏名称>\" 绑定账户")
        }
    }

    @Ghost
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

    // Velocity
    @Ghost
    @SubscribeEvent
    fun onVCJoin(event: PostLoginEvent) {
        if (event.player.username !in AQQBot.dataMap.values)
            event.player.disconnect(Component.text("[AQQBot] 你的账户还没有绑定QQ!" +
                    "\n请通过在QQ群发送 \"${AQQBot.config.getStringList("whitelist.prefix.bind")[0]} <游戏名称>\" 绑定账户"))
    }
}