package top.alazeprt.aqqbot.event

import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.bot.BotProvider
import top.alazeprt.aqqbot.profile.APlayer
import top.alazeprt.aqqbot.util.AFormatter
import java.util.*
import java.util.function.Consumer

object AEventUtil {
    fun whitelistHandler(plugin: AQQBot, playerName: String, kickMethod: Consumer<String>): Boolean {
        if (!plugin.generalConfig.getBoolean("whitelist.enable", null) || !plugin.generalConfig.getBoolean("whitelist.need_bind_to_login", null)) return false
        if (!plugin.hasPlayer(plugin.adapter!!.getOfflinePlayer(playerName))) {
            if (plugin.generalConfig.getString("whitelist.verify_method", null).uppercase() == "GROUP_NAME") {
                kickMethod.accept(AFormatter.pluginToChat(plugin.messageManager.get("game.not_bind", mutableMapOf(Pair("command", plugin.generalConfig.getStringList("whitelist.prefix.bind", null)[0])), null)))
                return true
            } else if (plugin.generalConfig.getString("whitelist.verify_method", null).uppercase() == "VERIFY_CODE") {
                val verifyCode = if (plugin.verifyCodeMap.containsKey(playerName)) plugin.verifyCodeMap.get(playerName)!!.first else UUID.randomUUID().toString().substring(0, 6)
                kickMethod.accept(AFormatter.pluginToChat(AFormatter.pluginToChat(plugin.messageManager.get("game.not_verified", mutableMapOf(Pair("command", plugin.generalConfig.getStringList("whitelist.prefix.bind", null)[0]), Pair("code", verifyCode)), null))))
                if (!plugin.verifyCodeMap.containsKey(playerName)) {
                    plugin.verifyCodeMap.put(playerName, Pair(verifyCode, System.currentTimeMillis()))
                }
                return true
            }
        }
        return false
    }

    fun playerStatusHandler(plugin: AQQBot, player: APlayer, isJoin: Boolean) {
        val playerName = player.getName()
        if (plugin.generalConfig.getBoolean("notify.player_status.enable", null)) {
            val qq: Long = plugin.getQQByPlayer(plugin.adapter!!.getOfflinePlayer(playerName))?: -1L
            plugin.submitAsync {
                plugin.enableGroups.forEach {
                    if (!plugin.generalConfig.getBoolean("notify.player_status.group_enable", it.key.toLong())) return@submitAsync
                    val messagePath = "notify.player_status.${if (isJoin) "join" else "leave"}"
                    plugin.debugModule?.debugLogger?.log("send the $playerName's ${if (isJoin) "join" else "leave"} message to group ${it.key}")
                    val message = if (plugin.generalConfig.getStringList(messagePath, it.key.toLong()).isEmpty())
                        plugin.generalConfig.getString(messagePath, it.key.toLong())?: ""
                    else plugin.generalConfig.getStringList(messagePath, it.key.toLong()).random()
                    BotProvider.getBot()?.action(
                        SendGroupMessage(it.key.toLong(), plugin.setPlaceholders(player, message)
                            .replace("\${playerName}", playerName)
                            .replace("\${userId}", qq.toString()), true)
                    )
                }
            }
        }
    }

    fun canForwardMessage(plugin: AQQBot, message: String, groupId: Long): String? {
        if (!plugin.generalConfig.getBoolean("chat.server_to_group.enable", groupId)) {
            return null
        }
        val formatter = plugin.toGroupFormatter
        var formattedMessage = formatter[groupId]?.regexFilter(plugin.generalConfig.getStringList("chat.server_to_group.filter", groupId), message)?: message
        if (formattedMessage.contains("!CANCEL")) return null
        if (plugin.generalConfig.getBoolean("chat.server_to_group.default_format", groupId)) {
            formattedMessage = AFormatter.chatClear(formattedMessage)
        }
        if (formattedMessage.length > plugin.generalConfig.getInt("chat.max_forward_length", groupId)) {
            formattedMessage = formattedMessage.substring(0, plugin.generalConfig.getInt("chat.max_forward_length", groupId)) + "..."
        }
        if (plugin.generalConfig.getStringList("chat.server_to_group.prefix", groupId).contains("")) {
            return formattedMessage
        }
        plugin.generalConfig.getStringList("chat.server_to_group.prefix", groupId).forEach {
            if (formattedMessage.startsWith(it)) {
                return formattedMessage.substring(it.length)
            }
        }
        return null
    }
}