package top.alazeprt.aqqbot

import taboolib.common.io.newFile
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.submit
import taboolib.module.configuration.Configuration
import top.alazeprt.aonebot.BotClient
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.qq.BotListener
import java.io.File
import java.net.URI

object AQQBot : Plugin() {

    lateinit var botConfig: Configuration

    lateinit var dataConfig: Configuration

    lateinit var config: Configuration

    lateinit var oneBotClient: BotClient

    private var alsoNoticed = false

    var isBukkit = true

    val enableGroups: MutableList<String> = mutableListOf()

    val dataMap: MutableMap<String, String> = mutableMapOf()

    override fun onActive() {
        info("Checking server type...")
        try {
            Class.forName("org.bukkit.Bukkit")
        } catch (e: ClassNotFoundException) {
            isBukkit = false
        }
        info("Loading data...")
        val configFile = newFile(getDataFolder(), "config.yml", create = true)
        config = Configuration.loadFromFile(configFile)
        val dataFile = newFile(getDataFolder(), "data.yml", create = true)
        dataConfig = Configuration.loadFromFile(dataFile)
        val botFile = newFile(getDataFolder(), "bot.yml", create = true)
        botConfig = Configuration.loadFromFile(botFile)
        dataConfig.getKeys(false).forEach {
            dataMap[it] = (dataConfig.getString(it)?: return@forEach)
        }
        botConfig.getStringList("groups").forEach {
            enableGroups.add(it)
        }
        info("Loading soft dependency...")
        DependencyImpl.loadSpark()
        if (isBukkit) {
            DependencyImpl.loadPlayerStats()
            DependencyImpl.loadPAPI()
        }
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