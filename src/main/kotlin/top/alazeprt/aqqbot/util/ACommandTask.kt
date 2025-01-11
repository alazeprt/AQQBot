package top.alazeprt.aqqbot.util

import taboolib.common.platform.function.info
import taboolib.common.platform.function.releaseResourceFile
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
import top.alazeprt.aqqbot.command.sender.ASender
import top.alazeprt.aqqbot.debug.ADebug
import top.alazeprt.aqqbot.event.AGameEvent
import top.alazeprt.aqqbot.handler.WhitelistAdminHandler.Companion.validateName
import top.alazeprt.aqqbot.qq.BotListener
import top.alazeprt.aqqbot.util.AI18n.get
import top.alazeprt.aqqbot.util.AI18n.getList
import top.alazeprt.aqqbot.util.DBQuery.addPlayer
import top.alazeprt.aqqbot.util.DBQuery.getUserIdByName
import top.alazeprt.aqqbot.util.DBQuery.playerInDatabase
import top.alazeprt.aqqbot.util.DBQuery.qqInDatabase
import top.alazeprt.aqqbot.util.DBQuery.removePlayerByName
import top.alazeprt.aqqbot.util.DBQuery.removePlayerByUserId
import java.net.URI

object ACommandTask {
    fun startReload(): Long {
        val s = System.currentTimeMillis()
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
        // Debug Option
        if (config.getBoolean("debug.enable")) {
            ADebug.shutdown()
            ADebug.initialize()
        }
        // Formatter
        ASender.formatter.initialUrl(config.getStringList("command_execution.filter"))
        BotListener.formatter.initialUrl(config.getStringList("chat.group_to_server.filter"))
        AGameEvent.formatter.initialUrl(config.getStringList("chat.server_to_group.filter"))
        DependencyImpl.loadSpark()
        if (isBukkit) {
            DependencyImpl.loadPAPI()
        }
        val url = "ws://" + botConfig.getString("ws.host") + ":" + botConfig.getInt("ws.port")
        oneBotClient = BotClient(URI.create(url))
        oneBotClient.connect()
        oneBotClient.registerEvent(BotListener())
        val time = System.currentTimeMillis() - s
        info("Reloaded AQQBot in $time ms")
        return time
    }

    fun forceBind(userId: String, playerName: String): String {
        if (isFileStorage && AQQBot.dataMap.containsKey(userId)) {
            AQQBot.dataMap.remove(userId)
        } else if (!isFileStorage && qqInDatabase(userId.toLong()) != null) {
            DBQuery.removePlayerByUserId(userId.toLong())
        }
        if (!validateName(playerName)) {
            return AFormatter.pluginToChat(get("game.invalid_arguments"))
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
        return AFormatter.pluginToChat(get("game.successfully_bind"))
    }
    
    fun forceUnbind(mode: String, data: String): String {
        if (mode.contains("qq")) {
            if (isFileStorage && !AQQBot.dataMap.containsKey(data)) {
                return AFormatter.pluginToChat(get("game.invalid_arguments"))
            } else if (!isFileStorage && qqInDatabase(data.toLong()) == null) {
                return AFormatter.pluginToChat(get("game.invalid_arguments"))
            }
            if (isFileStorage) {
                AQQBot.dataMap.forEach { (k, _) ->
                    if (k == data) {
                        AQQBot.dataMap.remove(k)
                        return AFormatter.pluginToChat(get("game.successfully_unbind"))
                    }
                }
                return AFormatter.pluginToChat(get("game.invalid_arguments"))
            } else {
                removePlayerByUserId(data.toLong())
                return AFormatter.pluginToChat(get("game.successfully_unbind"))
            }
        } else {
            if (isFileStorage && !AQQBot.dataMap.containsValue(data)) {
                return AFormatter.pluginToChat(get("game.invalid_arguments"))
            } else if (!isFileStorage && !playerInDatabase(data)) {
                return AFormatter.pluginToChat(get("game.invalid_arguments"))
            }
            if (isFileStorage) {
                AQQBot.dataMap.forEach { (_, v) ->
                    if (v == data) {
                        AQQBot.dataMap.remove(v)
                        return AFormatter.pluginToChat(get("game.successfully_unbind"))
                    }
                }
                return AFormatter.pluginToChat(get("game.invalid_arguments"))
            } else {
                removePlayerByName(data)
                return AFormatter.pluginToChat(get("game.successfully_unbind"))
            }
        }
    }

    fun query(mode: String, data: String): String {
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
        return AFormatter.pluginToChat(getList("game.query_result", mutableMapOf("userId" to userId, "playerName" to playerName)))
    }
}