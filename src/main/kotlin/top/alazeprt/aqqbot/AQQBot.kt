package top.alazeprt.aqqbot

import taboolib.common.platform.Platform
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.releaseResourceFile
import taboolib.common.platform.function.submit
import taboolib.module.configuration.Configuration
import taboolib.module.database.*
import taboolib.module.metrics.Metrics
import top.alazeprt.aonebot.BotClient
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.debug.ADebug
import top.alazeprt.aqqbot.qq.BotListener
import top.alazeprt.aqqbot.util.ACustom
import java.io.File
import java.net.URI
import javax.sql.DataSource

object AQQBot : Plugin() {

    lateinit var botConfig: Configuration

    lateinit var dataConfig: Configuration

    lateinit var config: Configuration

    lateinit var messageConfig: Configuration

    lateinit var oneBotClient: BotClient

    private var alsoNoticed = false

    var isBukkit = true

    val enableGroups: MutableList<String> = mutableListOf()

    val dataMap: MutableMap<String, String> = mutableMapOf()

    val verifyCodeMap: MutableMap<String, Pair<String, Long>> = mutableMapOf() // <name, <code, time>>

    val customCommands: MutableList<ACustom> = mutableListOf()

    lateinit var table: Table<*, *>

    lateinit var dataSource: DataSource

    var isFileStorage: Boolean = false

    lateinit var dataFolder: File

    override fun onEnable() {
        info("Checking server type...")
        try {
            Class.forName("org.bukkit.Bukkit")
        } catch (e: ClassNotFoundException) {
            isBukkit = false
        }
        val metrics = Metrics(24071, "1.0.12", Platform.CURRENT)
        info("Loading data...")
        val configFile = releaseResourceFile("config.yml", replace = false)
        dataFolder = getDataFolder()
        config = Configuration.loadFromFile(configFile)
        val dataFile = releaseResourceFile("data.yml", replace = false)
        dataConfig = Configuration.loadFromFile(dataFile)
        if (config.getString("storage.type")!!.lowercase() == "file") isFileStorage = true
        else if (config.getString("storage.type")!!.lowercase() == "sqlite") {
            val host = HostSQLite(File(getDataFolder(), config.getString("storage.sqlite.file")?: "aqqbot.db"))
            val dataSource by lazy { host.createDataSource() }
            table = Table("account_data", host) {
                add("userId") {
                    type(ColumnTypeSQLite.INTEGER) {
                        options(ColumnOptionSQLite.PRIMARY_KEY)
                    }
                }
                add("name") {
                    type(ColumnTypeSQLite.TEXT) {
                        options(ColumnOptionSQLite.NOTNULL)
                    }
                }
            }
            AQQBot.dataSource = dataSource
            table.createTable(dataSource)
        } else if (config.getString("storage.type")!!.lowercase() == "mysql") {
            val host = config.getHost("storage.mysql")
            val dataSource by lazy { host.createDataSource() }
            table = Table("account_data", host) {
                add("userId") {
                    type(ColumnTypeSQL.BIGINT) {
                        options(ColumnOptionSQL.PRIMARY_KEY)
                    }
                }
                add("name") {
                    type(ColumnTypeSQL.VARCHAR) {
                        options(ColumnOptionSQL.NOTNULL)
                    }
                }
            }
            AQQBot.dataSource = dataSource
            table.createTable(dataSource)
        }
        if (config.getString("whitelist.verify_method")?.uppercase() == "VERIFY_CODE") {
            submit(async = true) {
                while (true) {
                    verifyCodeMap.forEach {
                        if (System.currentTimeMillis() - it.value.second > config.getLong("whitelist.verify_code_expire_time") * 1000L) {
                            verifyCodeMap.remove(it.key)
                        }
                    }
                    Thread.sleep(5000)
                }
            }
        }
        val botFile = releaseResourceFile("bot.yml", replace = false)
        botConfig = Configuration.loadFromFile(botFile)
        val messageFile = releaseResourceFile("messages.yml", replace = false)
        messageConfig = Configuration.loadFromFile(messageFile)
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
        dataConfig.getKeys(false).forEach {
            dataMap[it] = (dataConfig.getString(it)?: return@forEach)
        }
        botConfig.getStringList("groups").forEach {
            enableGroups.add(it)
        }
        if (config.getBoolean("debug.enable")) ADebug.initialize()
        info("Loading soft dependency...")
        DependencyImpl.loadSpark()
        if (isBukkit) {
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
        if (isFileStorage) {
            dataMap.forEach {
                dataConfig[it.key] = it.value
            }
            dataConfig.saveToFile(File(getDataFolder(), "data.yml"))
        }
        if (config.getBoolean("notify.enable")) {
            enableGroups.forEach {
                val msg = config.getString("notify.messages.stop")?: return@forEach
                if (oneBotClient.isConnected) {
                    oneBotClient.action(SendGroupMessage(it.toLong(), msg))
                }
            }
        }
        if (config.getBoolean("debug.enable")) ADebug.shutdown()
        if (oneBotClient.isConnected) {
            oneBotClient.disconnect()
        }
        if (!isFileStorage) dataSource.connection.close()
    }
}