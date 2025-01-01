package top.alazeprt.aqqbot.command

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import taboolib.common.platform.command.CommandBody
import taboolib.common.platform.command.CommandHeader
import taboolib.common.platform.command.player
import taboolib.common.platform.command.subCommand
import taboolib.common.platform.function.info
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.platform.function.submit
import taboolib.module.configuration.Configuration
import top.alazeprt.aonebot.BotClient
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.AQQBot.botConfig
import top.alazeprt.aqqbot.AQQBot.config
import top.alazeprt.aqqbot.AQQBot.customCommands
import top.alazeprt.aqqbot.AQQBot.enableGroups
import top.alazeprt.aqqbot.AQQBot.isBukkit
import top.alazeprt.aqqbot.AQQBot.isFileStorage
import top.alazeprt.aqqbot.AQQBot.messageConfig
import top.alazeprt.aqqbot.AQQBot.oneBotClient
import top.alazeprt.aqqbot.DependencyImpl
import top.alazeprt.aqqbot.handler.WhitelistAdminHandler.Companion.validateName
import top.alazeprt.aqqbot.qq.BotListener
import top.alazeprt.aqqbot.util.ACustom
import top.alazeprt.aqqbot.util.AI18n.get
import top.alazeprt.aqqbot.util.AI18n.getList
import top.alazeprt.aqqbot.util.DBQuery
import top.alazeprt.aqqbot.util.DBQuery.addPlayer
import top.alazeprt.aqqbot.util.DBQuery.getUserIdByName
import top.alazeprt.aqqbot.util.DBQuery.playerInDatabase
import top.alazeprt.aqqbot.util.DBQuery.qqInDatabase
import top.alazeprt.aqqbot.util.DBQuery.removePlayerByName
import top.alazeprt.aqqbot.util.DBQuery.removePlayerByUserId
import java.net.URI

@CommandHeader("aqqbot", ["abot", "qqbot"])
object ABotCommand {
    @CommandBody(permission = "aqqbot.reload")
    val reload = subCommand {
        execute<CommandSender> { sender, _, _ ->
            val s = System.currentTimeMillis()
            submit(async = true) {
                info("Reloading AQQBot ...")
                if (oneBotClient.isConnected) {
                    oneBotClient.disconnect()
                }
                enableGroups.clear()
                val configFile = releaseResourceFile("config.yml", replace = false)
                config = Configuration.loadFromFile(configFile)
                val botFile = releaseResourceFile("bot.yml", replace = false)
                botConfig = Configuration.loadFromFile(botFile)
                val messageFile = releaseResourceFile("messages.yml", replace = false)
                messageConfig = Configuration.loadFromFile(messageFile)
                customCommands.clear()
                val customFile = releaseResourceFile("custom.yml", replace = false)
                val customConfig = Configuration.loadFromFile(customFile)
                customConfig.getKeys(false).forEach {
                    if (customConfig.getBoolean("$it.enable")) {
                        val command = customConfig.getStringList("$it.command")
                        val output = customConfig.getStringList("$it.output")
                        val unbind_output = customConfig.getStringList("$it.unbind_output")
                        val format = customConfig.getBoolean("$it.format")
                        customCommands.add(ACustom(command, output, unbind_output, format))
                    }
                }
                botConfig.getStringList("groups").forEach {
                    enableGroups.add(it)
                }
                DependencyImpl.loadSpark()
                if (isBukkit) {
                    DependencyImpl.loadPAPI()
                }
                val url = "ws://" + botConfig.getString("ws.host") + ":" + botConfig.getInt("ws.port")
                oneBotClient = BotClient(URI.create(url))
                oneBotClient.connect()
                oneBotClient.registerEvent(BotListener())
                sender.sendMessage(formatString(get("game.reload", mutableMapOf("time" to (System.currentTimeMillis() - s).toString()))))
                info("Reloaded AQQBot in ${System.currentTimeMillis() - s} ms")
            }
        }
    }

    @CommandBody(permission = "aqqbot.forcebind")
    val forcebind = subCommand {
        dynamic("userId") {
            player("playerName") {
                suggestion<CommandSender>(uncheck = true) { _, _ ->
                    if (isBukkit) Bukkit.getOnlinePlayers().map { it.name }.toList() else emptyList()
                }
                execute<CommandSender> { sender, context, _ ->
                    val userId = context["userId"]
                    val playerName = context.player("playerName").name
                    if (isFileStorage && AQQBot.dataMap.containsKey(userId)) {
                        AQQBot.dataMap.remove(userId)
                    } else if (!isFileStorage && qqInDatabase(userId.toLong()) != null) {
                        DBQuery.removePlayerByUserId(userId.toLong())
                    }
                    if (!validateName(playerName)) {
                        sender.sendMessage(formatString(get("game.invalid_arguments")))
                        return@execute
                    }
                    if (isFileStorage) {
                        var existUserId = ""
                        AQQBot.dataMap.forEach { k, v ->
                            if (v == playerName) {
                                existUserId = k
                                return@forEach
                            }
                        }
                        if (existUserId != "") {
                            AQQBot.dataMap.remove(existUserId)
                        }
                    } else {
                        if (playerInDatabase(playerName)) {
                            DBQuery.removePlayerByName(playerName)
                        }
                    }
                    if (isFileStorage) AQQBot.dataMap[userId] = playerName
                    else addPlayer(userId.toLong(), playerName)
                    sender.sendMessage(formatString(get("game.successfully_bind")))
                }
            }
        }
    }

    @CommandBody(permission = "aqqbot.forceunbind")
    val forceunbind = subCommand {
        dynamic("mode") {
            dynamic("data") {
                suggestion<CommandSender>(uncheck = true) { _, context ->
                    if (isBukkit && !context["mode"].contains("qq")) Bukkit.getOnlinePlayers().map { it.name }.toList() else emptyList()
                }
                execute<CommandSender> { sender, context, _ ->
                    val mode = context["mode"]
                    val data = context["data"]
                    if (mode.contains("qq")) {
                        if (isFileStorage && !AQQBot.dataMap.containsKey(data)) {
                            sender.sendMessage(formatString(get("game.invalid_arguments")))
                            return@execute
                        } else if (!isFileStorage && qqInDatabase(data.toLong()) == null) {
                            sender.sendMessage(formatString(get("game.invalid_arguments")))
                            return@execute
                        }
                        if (isFileStorage) {
                            AQQBot.dataMap.forEach { (k, _) ->
                                if (k == data) {
                                    AQQBot.dataMap.remove(k)
                                    sender.sendMessage(formatString(get("game.successfully_unbind")))
                                    return@execute
                                }
                            }
                            sender.sendMessage(formatString(get("game.invalid_arguments")))
                        } else {
                            removePlayerByUserId(data.toLong())
                            sender.sendMessage(formatString(get("game.successfully_unbind")))
                        }
                    } else {
                        if (isFileStorage && !AQQBot.dataMap.containsValue(data)) {
                            sender.sendMessage(formatString(get("game.invalid_arguments")))
                            return@execute
                        } else if (!isFileStorage && !playerInDatabase(data)) {
                            sender.sendMessage(formatString(get("game.invalid_arguments")))
                            return@execute
                        }
                        if (isFileStorage) {
                            AQQBot.dataMap.forEach { (_, v) ->
                                if (v == data) {
                                    AQQBot.dataMap.remove(v)
                                    sender.sendMessage(formatString(get("game.successfully_unbind")))
                                    return@execute
                                }
                            }
                            sender.sendMessage(formatString(get("game.invalid_arguments")))
                        } else {
                            removePlayerByName(data)
                            sender.sendMessage(formatString(get("game.successfully_unbind")))
                        }
                    }
                }
            }
        }
    }

    @CommandBody(permission = "aqqbot.query")
    val query = subCommand {
        dynamic("mode") {
            dynamic("data") {
                suggestion<CommandSender>(uncheck = true) { _, context ->
                    if (isBukkit && context["mode"].contains("qq")) Bukkit.getOnlinePlayers().map { it.name }.toList() else emptyList()
                }
                execute<CommandSender> { sender, context, _ ->
                    val mode = context["mode"]
                    val data = context["data"]
                    var userId = "未知"
                    var playerName = "未知"
                    if (mode.contains("qq")) {
                        userId = data
                        if (isFileStorage) {
                            playerName = AQQBot.dataMap[userId] ?: "未知"
                        } else {
                            playerName = qqInDatabase(userId.toLong()) ?: "未知"
                        }
                    } else {
                        playerName = data
                        if (isFileStorage) {
                            AQQBot.dataMap.forEach { k, v ->
                                if (v == data) {
                                    userId = k
                                    return@forEach
                                }
                            }
                        } else {
                            userId = if (playerInDatabase(playerName)) getUserIdByName(playerName).toString() else "未知"
                        }
                    }
                    sender.sendMessage(formatString(getList("game.query_result",
                        mutableMapOf("userId" to userId, "playerName" to playerName)
                    )))
                }
            }
        }
    }

    @CommandBody(permission = "aqqbot.help")
    val help = subCommand {
        execute<CommandSender> { sender, _, _ ->
            sender.sendMessage(formatString(getList("game.help")))
        }
    }

    private fun formatString(input: String): String {
        return input.replace(Regex("&([0-9a-fklmnor])")) { matchResult ->
            "§" + matchResult.groupValues[1]
        }
    }
}