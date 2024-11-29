package top.alazeprt.aqqbot

import taboolib.common.platform.Plugin
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.submit
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import top.alazeprt.aonebot.BotClient
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.qq.BotListener
import java.io.File
import java.net.URI

object AQQBot : Plugin() {

    @Config("bot.yml")
    lateinit var botConfig: ConfigFile

    @Config("data.yml")
    lateinit var dataConfig: ConfigFile

    @Config("config.yml")
    lateinit var config: ConfigFile

    lateinit var oneBotClient: BotClient

    var alsoNoticed = false

    val enableGroups: MutableList<String> = mutableListOf()

    val dataMap: MutableMap<String, String> = mutableMapOf()

    override fun onActive() {
        info("Loading data...")
        dataConfig.getKeys(false).forEach {
            dataMap[it] = (dataConfig.getString(it)?: return@forEach)
        }
        botConfig.getStringList("groups").forEach {
            enableGroups.add(it)
        }
        info("Loading soft dependency...")
        DependencyImpl.loadSpark()
        DependencyImpl.loadPlayerStats()
        DependencyImpl.loadPAPI()
        submit(async = true) {
            info("Enabling bot...")
            val url = "ws://" + botConfig.getString("ws.host") + ":" + botConfig.getInt("ws.port")
            oneBotClient = BotClient(URI.create(url))
            oneBotClient.connect()
            if (config.getBoolean("notify.enable") && !alsoNoticed) {
                enableGroups.forEach {
                    val msg = config.getString("notify.messages.start")?: return@forEach
                    oneBotClient.action(SendGroupMessage(it.toLong(), msg))
                }
                alsoNoticed = true
            }
            oneBotClient.registerEvent(BotListener())
        }
    }

    override fun onDisable() {
        dataMap.forEach {
            dataConfig[it.key] = it.value
        }
        dataConfig.saveToFile(File(getDataFolder(), "data.yml"))
        if (config.getBoolean("notify.enable")) {
            enableGroups.forEach {
                val msg = config.getString("notify.messages.stop")?: return@forEach
                if (oneBotClient.isConnected) {
                    oneBotClient.action(SendGroupMessage(it.toLong(), msg))
                }
            }
        }
        if (oneBotClient.isConnected) {
            oneBotClient.disconnect()
        }
    }
}