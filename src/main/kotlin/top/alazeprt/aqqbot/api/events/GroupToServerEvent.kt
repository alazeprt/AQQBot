package top.alazeprt.aqqbot.api.events

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import taboolib.platform.VelocityPlugin
import top.alazeprt.aqqbot.AQQBot.config
import top.alazeprt.aqqbot.AQQBot.isBukkit
import top.alazeprt.aqqbot.util.AFormatter
import top.alazeprt.aqqbot.util.AI18n.get

class GroupToServerEvent(val groupId: Long, val senderId: Long, val card: String, var message: String): AQBEventInterface {
    override fun handle() {
        if (isBukkit) {
            Bukkit.broadcastMessage(
                AFormatter.pluginToChat(
                    get("game.chat_from_qq", mutableMapOf("groupId" to groupId.toString(),
                    "userName" to card,
                    "message" to message))
                ))
        } else if (config.getBoolean("chat.group_to_server.vc_broadcast") && !isBukkit) {
            VelocityPlugin.getInstance().server.allServers.forEach {
                it.sendMessage(
                    Component.text(
                        AFormatter.pluginToChat(
                            get("game.chat_from_qq", mutableMapOf("groupId" to groupId.toString(),
                            "userName" to card,
                            "message" to message))
                        )
                    )
                )
            }
        }
    }
}