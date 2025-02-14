package top.alazeprt.aqqbot

import taboolib.common.platform.Platform
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.*
import taboolib.common.platform.service.PlatformExecutor
import taboolib.common.reflect.hasAnnotation
import taboolib.module.configuration.Configuration
import taboolib.module.database.*
import taboolib.module.metrics.Metrics
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aonebot.client.websocket.WebsocketBotClient
import top.alazeprt.aqqbot.api.events.AQBEvent
import top.alazeprt.aqqbot.api.events.AQBEventInterface
import top.alazeprt.aqqbot.api.events.AQBListener
import top.alazeprt.aqqbot.command.sender.ASender
import top.alazeprt.aqqbot.debug.ADebug
import top.alazeprt.aqqbot.event.AGameEvent
import top.alazeprt.aqqbot.qq.BotListener
import top.alazeprt.aqqbot.util.ACommandTask
import top.alazeprt.aqqbot.util.ACustom
import java.io.File
import java.net.URI
import javax.sql.DataSource

object AQQBot : Plugin() {

    lateinit var botConfig: Configuration

    lateinit var dataConfig: Configuration

    lateinit var config: Configuration

    lateinit var messageConfig: Configuration

    lateinit var oneBotClient: WebsocketBotClient

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

    var checkTask: PlatformExecutor.PlatformTask? = null

    private val eventList = mutableListOf<AQBListener>()

    override fun onEnable() {
        info("Checking server type...")
        try {
            Class.forName("org.bukkit.Bukkit")
        } catch (e: ClassNotFoundException) {
            isBukkit = false
        }
        val metrics = Metrics(24071, "1.1.2", Platform.CURRENT)
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
                    type(ColumnTypeSQLite.TEXT, 512) {
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
                    type(ColumnTypeSQL.VARCHAR, 1024) {
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
            oneBotClient = if (botConfig.getString("access_token").isNullOrBlank()) {
                WebsocketBotClient(URI.create(url))
            } else {
                WebsocketBotClient(URI.create(url), botConfig.getString("access_token"))
            }
            oneBotClient.connect()
            if (config.getBoolean("notify.server_status.enable") && !alsoNoticed) {
                enableGroups.forEach {
                    val msg = config.getString("notify.server_status.start")?: return@forEach
                    oneBotClient.action(SendGroupMessage(it.toLong(), msg))
                }
                alsoNoticed = true
            }
            oneBotClient.registerEvent(BotListener())
            if (botConfig.getLong("check_interval") > 0) {
                info("Enabling connection checking system...")
                checkTask = submit(async = true, period = 20 * botConfig.getLong("check_interval")) {
                    if (!oneBotClient.isConnected) {
                        warning("Bot connection lost, trying to reconnect...")
                        oneBotClient.connect()
                    }
                }
            }
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

    fun registerEvent(event: AQBListener) {
        eventList.add(event)
    }

    fun postEvent(event: AQBEventInterface) {
        synchronized(eventList) {
            eventList.forEach {
                it.javaClass.declaredMethods.forEach { method ->
                    if (method.hasAnnotation(AQBEvent::class.java) && method.parameters.size == 1
                        && method.parameters[0].parameterizedType.typeName == event.javaClass.typeName) {
                        method.invoke(it, event)
                    }
                }
            }
        }
    }

    fun searchQQByName(name: String): String {
        return ACommandTask.query("name", name)
    }

    fun searchNameByQQ(qq: String): String {
        return ACommandTask.query("qq", qq)
    }
}