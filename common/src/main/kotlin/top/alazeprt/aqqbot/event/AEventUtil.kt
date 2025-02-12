package top.alazeprt.aqqbot.event

import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.bot.BotProvider
import top.alazeprt.aqqbot.util.AFormatter
import java.util.*
import java.util.function.Consumer

object AEventUtil {
    fun whitelistHandler(plugin: AQQBot, playerName: String, kickMethod: Consumer<String>): Boolean {
        if (!plugin.getGeneralConfig().getBoolean("whitelist.enable") || !plugin.getGeneralConfig().getBoolean("whitelist.need_bind_to_login")) return false
        if (!plugin.hasPlayer(plugin.adapter!!.getOfflinePlayer(playerName))) {
            if (plugin.getGeneralConfig().getString("whitelist.verify_method")?.uppercase() == "GROUP_NAME") {
                kickMethod.accept(AFormatter.pluginToChat(plugin.getMessageManager().get("game.not_bind", mutableMapOf(Pair("command", plugin.getGeneralConfig().getStringList("whitelist.prefix.bind")[0])))))
                return true
            } else if (plugin.getGeneralConfig().getString("whitelist.verify_method")?.uppercase() == "VERIFY_CODE") {
                val verifyCode = if (plugin.verifyCodeMap.containsKey(playerName)) plugin.verifyCodeMap.get(playerName)!!.first else UUID.randomUUID().toString().substring(0, 6)
                kickMethod.accept(AFormatter.pluginToChat(AFormatter.pluginToChat(plugin.getMessageManager().get("game.not_verified", mutableMapOf(Pair("command", plugin.getGeneralConfig().getStringList("whitelist.prefix.bind")[0]), Pair("code", verifyCode))))))
                if (!plugin.verifyCodeMap.containsKey(playerName)) {
                    plugin.verifyCodeMap.put(playerName, Pair(verifyCode, System.currentTimeMillis()))
                }
                return true
            }
        }
        return false
    }

    fun playerStatusHandler(plugin: AQQBot, playerName: String, isJoin: Boolean) {
        if (plugin.getGeneralConfig().getBoolean("notify.player_status.enable")) {
            val qq: Long = plugin.getQQByPlayer(plugin.adapter!!.getOfflinePlayer(playerName))?: -1L
            plugin.submitAsync {
                plugin.enableGroups.forEach {
                    BotProvider.getBot()?.action(
                        SendGroupMessage(it.toLong(), plugin.getGeneralConfig().getString("notify.player_status.${if (isJoin) "join" else "leave"}")!!
                            .replace("\${playerName}", playerName)
                            .replace("\${userId}", qq.toString()), true)
                    )
                }
            }
        }
    }

    fun canForwardMessage(plugin: AQQBot, message: String): String? {
        if (!plugin.getGeneralConfig().getBoolean("chat.server_to_group.enable")) {
            return null
        }
        val formatter = AFormatter(plugin)
        var formattedMessage = formatter.regexFilter(plugin.getGeneralConfig().getStringList("chat.server_to_group.filter"), message)
        if (plugin.getGeneralConfig().getBoolean("chat.server_to_group.default_format")) {
            formattedMessage = AFormatter.chatClear(formattedMessage)
        }
        if (formattedMessage.length > plugin.getGeneralConfig().getInt("chat.max_forward_length")) {
            formattedMessage = formattedMessage.substring(0, plugin.getGeneralConfig().getInt("chat.max_forward_length")) + "..."
        }
        if (plugin.getGeneralConfig().getStringList("chat.server_to_group.prefix").contains("")) {
            return formattedMessage
        }
        plugin.getGeneralConfig().getStringList("chat.server_to_group.prefix").forEach {
            if (formattedMessage.startsWith(it)) {
                return formattedMessage.substring(it.length)
            }
        }
        return null
    }
}