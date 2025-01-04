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
import top.alazeprt.aqqbot.AQQBot.config
import top.alazeprt.aqqbot.AQQBot.isFileStorage
import top.alazeprt.aqqbot.util.AI18n.get
import top.alazeprt.aqqbot.util.DBQuery.playerInDatabase

object AGameEvent {
    // Bukkit
    @Ghost
    @SubscribeEvent
    fun onJoin(event: AsyncPlayerPreLoginEvent) {
        if (!config.getBoolean("whitelist.enable") || !config.getBoolean("whitelist.need_bind_to_login")) return
        if (isFileStorage && event.name !in AQQBot.dataMap.values) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, formatString(get("game.not_bind", mutableMapOf(Pair("command", AQQBot.config.getStringList("whitelist.prefix.bind")[0])))))
        }
        if (!isFileStorage && !playerInDatabase(event.name)) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, formatString(get("game.not_bind", mutableMapOf(Pair("command", AQQBot.config.getStringList("whitelist.prefix.bind")[0])))))
        }
    }

    @Ghost
    @SubscribeEvent
    fun onChat(event: AsyncPlayerChatEvent) {
        if (canForwardMessage(event.message) != null) {
            submit (async = true) {
                AQQBot.enableGroups.forEach {
                    AQQBot.oneBotClient.action(SendGroupMessage(it.toLong(), get("qq.chat_from_game", mutableMapOf("player" to event.player.name, "message" to canForwardMessage(event.message)!!)), true))
                }
            }
        }
    }

    // Velocity
    @Ghost
    @SubscribeEvent
    fun onVCJoin(event: PostLoginEvent) {
        if (!config.getBoolean("whitelist.enable") || !config.getBoolean("whitelist.need_bind_to_login")) return
        if (isFileStorage && event.player.username !in AQQBot.dataMap.values) {
            event.player.disconnect(Component.text(formatString(get("game.not_bind", mutableMapOf(Pair("command", AQQBot.config.getStringList("whitelist.prefix.bind")[0]))))))
        }
        if (!isFileStorage && !playerInDatabase(event.player.username)) {
            event.player.disconnect(Component.text(formatString(get("game.not_bind", mutableMapOf(Pair("command", AQQBot.config.getStringList("whitelist.prefix.bind")[0]))))))
        }
    }

    private fun formatString(input: String): String {
        return input.replace(Regex("&([0-9a-fklmnor])")) { matchResult ->
            "§" + matchResult.groupValues[1]
        }
    }

    private fun canForwardMessage(message: String): String? {
        if (!config.getBoolean("chat.server_to_group.enable")) {
            return null
        }
        var formattedMessage = message
        if (config.getBoolean("chat.server_to_group.format")) {
            formattedMessage = message.replace(Regex("§([0-9a-fklmnor])"), "")
        }
        config.getStringList("command_execution.format_list").forEach {
            if (it != "") {
                formattedMessage = formattedMessage.replace(it, "")
            }
        }
        if (config.getStringList("chat.server_to_group.prefix").contains("")) {
            return formattedMessage
        }
        config.getStringList("chat.server_to_group.prefix").forEach {
             if (formattedMessage.startsWith(it)) {
                 return formattedMessage.substring(it.length)
             }
        }
        return null
    }
}