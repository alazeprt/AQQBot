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
import top.alazeprt.aqqbot.command.sender.ASender
import top.alazeprt.aqqbot.debug.ADebug
import top.alazeprt.aqqbot.event.AGameEvent
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

    val dataMap: MutableMap<String, MutableList<String>> = mutableMapOf()

    val verifyCodeMap: MutableMap<String, Pair<String, Long>> = mutableMapOf() // <name, <code, time>>

    val customCommands: MutableList<ACustom> = mutableListOf()

    lateinit var table: Table<*, *>

    lateinit var dataSource: DataSource

    var isFileStorage: Boolean = false

    lateinit var dataFolder: File

    var updateConfig = false

    override fun onEnable() {
        info("Checking server type...")
        try {
            Class.forName("org.bukkit.Bukkit")
        } catch (e: ClassNotFoundException) {
            isBukkit = false
        }
        val metrics = Metrics(24071, "1.0.14.1", Platform.CURRENT)
        info("Loading data...")
        val configFile = releaseResourceFile("config.yml", replace = false)
        dataFolder = getDataFolder()
        config = Configuration.loadFromFile(configFile)
        if (config.getInt("version") != 14) {
            updateConfig = true
            config.saveToFile(File(getDataFolder(), "config_new.yml"))
            if (config.getInt("chat.max_forward_length") == 0) {
                config.set("chat.max_forward_length", 200)
            }
        }
        // Data loader
        val dataFile = releaseResourceFile("data.yml", replace = false)
        dataConfig = Configuration.loadFromFile(dataFile)
        if (config.getString("storage.type")!!.lowercase() == "file") isFileStorage = true
        else if (config.getString("storage.type")!!.lowercase() == "sqlite") {
            val host = HostSQLite(File(getDataFolder(), config.getString("storage.sqlite.file")?: "aqqbot.db"))
            host.createDataSource()
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
        // Add verify code map future
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
        // Add custom commands
        val customFile = releaseResourceFile("custom.yml", replace = false)
        val customConfig = Configuration.loadFromFile(customFile)
        customConfig.getKeys(false).forEach {
            if (customConfig.getBoolean("$it.enable")) {
                val command = customConfig.getStringList("$it.command")
                val execute = customConfig.getStringList("$it.execute")
                val unbind_execute = customConfig.getStringList("$it.unbind_execute")
                val output = customConfig.getStringList("$it.output")
                val unbind_output = customConfig.getStringList("$it.unbind_output")
                val format = customConfig.getBoolean("$it.format")
                val choose_account = if (customConfig.getInt("$it.chooseAccount") == 0) 1
                    else customConfig.getInt("$it.chooseAccount")
                customCommands.add(ACustom(command, execute, unbind_execute, output, unbind_output, format, choose_account))
            }
        }
        // Add exists qq data
        dataConfig.getKeys(false).forEach {
            dataMap[it] = dataConfig.getStringList(it).toMutableList()
        }
        // Add enable groups
        botConfig.getStringList("groups").forEach {
            enableGroups.add(it)
        }
        // Debug Option
        if (config.getBoolean("debug.enable")) ADebug.initialize()
        // Formatter
        ASender.formatter.initialUrl(config.getStringList("command_execution.filter"))
        BotListener.formatter.initialUrl(config.getStringList("chat.group_to_server.filter"))
        AGameEvent.formatter.initialUrl(config.getStringList("chat.server_to_group.filter"))
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
            if (config.getBoolean("notify.server_status.enable") && !alsoNoticed) {
                enableGroups.forEach {
                    val msg = config.getString("notify.server_status.start")?: return@forEach
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
        if (config.getBoolean("notify.server_status.enable")) {
            enableGroups.forEach {
                val msg = config.getString("notify.server_status.stop")?: return@forEach
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