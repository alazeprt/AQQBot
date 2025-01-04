package top.alazeprt.aqqbot.command

import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.player
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.submitAsync
import top.alazeprt.aqqbot.AQQBot.isBukkit
import top.alazeprt.aqqbot.util.ACommandTask.forceBind
import top.alazeprt.aqqbot.util.ACommandTask.forceUnbind
import top.alazeprt.aqqbot.util.ACommandTask.query
import top.alazeprt.aqqbot.util.ACommandTask.startReload
import top.alazeprt.aqqbot.util.AI18n.get
import top.alazeprt.aqqbot.util.AI18n.getList
import java.awt.Component

@CommandHeader("aqqbot", ["abot", "qqbot"])
object ABotCommand {

    @CommandBody(permission = "aqqbot.reload")
    val reload = subCommand {
        if (isBukkit) {
            execute<org.bukkit.command.CommandSender> { sender, _, _ ->
                submitAsync {
                    sender.sendMessage(formatString(get("game.reload", mutableMapOf("time" to startReload().toString()))))
                }
            }
        } else {
            execute<com.velocitypowered.api.command.CommandSource> { sender, _, _ ->
                submitAsync {
                    sender.sendMessage(net.kyori.adventure.text.Component.text(formatString(get("game.reload", mutableMapOf("time" to startReload().toString())))))
                }
            }
        }
    }

    @CommandBody(permission = "aqqbot.forcebind")
    val forcebind = subCommand {
        dynamic("userId") {
            player("playerName") {
                if (isBukkit) {
                    suggestion<org.bukkit.command.CommandSender>(uncheck = true) { _, _ ->
                        org.bukkit.Bukkit.getOnlinePlayers().map { it.name }.toList()
                    }
                    execute<org.bukkit.command.CommandSender> { sender, context, _ ->
                        sender.sendMessage(forceBind(context["userId"], context.player("playerName").name))
                    }
                } else {
                    execute<com.velocitypowered.api.command.CommandSource> { sender, context, _ ->
                        sender.sendMessage(net.kyori.adventure.text.Component.text(forceBind(context["userId"], context["playerName"])))
                    }
                }
            }
        }
    }

    @CommandBody(permission = "aqqbot.forceunbind")
    val forceunbind = subCommand {
        dynamic("mode") {
            dynamic("data") {
                if (isBukkit) {
                    suggestion<org.bukkit.command.CommandSender>(uncheck = true) { _, context ->
                        if (!context["mode"].contains("qq")) org.bukkit.Bukkit.getOnlinePlayers().map { it.name }.toList() else emptyList()
                    }
                    execute<org.bukkit.command.CommandSender> { sender, context, _ ->
                        sender.sendMessage(forceUnbind(context["mode"], context["data"]))
                    }
                } else {
                    execute<com.velocitypowered.api.command.CommandSource> { sender, context, _ ->
                        sender.sendMessage(net.kyori.adventure.text.Component.text(forceUnbind(context["mode"], context["data"])))
                    }
                }
            }
        }
    }

    @CommandBody(permission = "aqqbot.query")
    val query = subCommand {
        dynamic("mode") {
            dynamic("data") {
                if (isBukkit) {
                    suggestion<org.bukkit.command.CommandSender>(uncheck = true) { _, context ->
                        if (context["mode"].contains("qq")) org.bukkit.Bukkit.getOnlinePlayers().map { it.name }.toList() else emptyList()
                    }
                    execute<org.bukkit.command.CommandSender> { sender, context, _ ->
                        sender.sendMessage(query(context["mode"], context["data"]))
                    }
                } else {
                    execute<com.velocitypowered.api.command.CommandSource> { sender, context, _ ->
                        sender.sendMessage(net.kyori.adventure.text.Component.text(query(context["mode"], context["data"])))
                    }
                }
            }
        }
    }

    @CommandBody(permission = "aqqbot.help")
    val help = subCommand {
        if (isBukkit) {
            execute<org.bukkit.command.CommandSender> { sender, _, _ ->
                sender.sendMessage(formatString(getList("game.help")))
            }
        } else {
            execute<com.velocitypowered.api.command.CommandSource> { sender, _, _ ->
                sender.sendMessage(net.kyori.adventure.text.Component.text(formatString(getList("game.help"))))
            }
        }
    }

    private fun formatString(input: String): String {
        return input.replace(Regex("&([0-9a-fklmnor])")) { matchResult ->
            "§" + matchResult.groupValues[1]
        }
    }
}