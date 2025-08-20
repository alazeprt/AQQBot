package top.alazeprt.aqqbot

import com.alessiodp.libby.Library
import com.alessiodp.libby.LibraryManager
import net.kyori.adventure.text.TextComponent
import top.alazeprt.aconfiguration.file.FileConfiguration
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.adapter.AQQBotAdapter
import top.alazeprt.aqqbot.api.AQQBotAPI
import top.alazeprt.aqqbot.api.webhook.AQQBotWebhookServer
import top.alazeprt.aqqbot.api.webhook.WebhookProvider
import top.alazeprt.aqqbot.bot.BotProvider.getBot
import top.alazeprt.aqqbot.bot.BotProvider.loadBot
import top.alazeprt.aqqbot.bot.BotProvider.unloadBot
import top.alazeprt.aqqbot.command.CommandProvider
import top.alazeprt.aqqbot.config.ConfigProvider
import top.alazeprt.aqqbot.data.*
import top.alazeprt.aqqbot.debug.ADebug
import top.alazeprt.aqqbot.drivers.Web2ImageDriver
import top.alazeprt.aqqbot.hook.HookProvider
import top.alazeprt.aqqbot.plugins.PluginLoader
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import top.alazeprt.aqqbot.task.TaskProvider
import top.alazeprt.aqqbot.util.AExecution
import top.alazeprt.aqqbot.util.AFormatter
import top.alazeprt.aqqbot.util.GroupConfiguration
import top.alazeprt.aqqbot.util.LogLevel
import java.io.File
import java.net.InetSocketAddress
import java.net.URI
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

interface AQQBot: ConfigProvider, CommandProvider, DataProvider, HookProvider, TaskProvider {

    var debugModule: ADebug?

    var adapter: AQQBotAdapter

    val verifyCodeMap: MutableMap<String, Pair<String, Long>>  // <name, <code, time>>

    val bindCooldownMap: MutableMap<String, Long>   // <name, time>
    val unbindCooldownMap: MutableMap<String, Long>

    var dataProvider: DataProvider

    var toGroupFormatter: MutableMap<Long, AFormatter>
    var toGameFormatter: MutableMap<Long, AFormatter>

    var sender: MutableMap<Long, Class<out AExecution>>

    var libraryManager: LibraryManager

    var serverUUID: UUID

    var webDriver: Web2ImageDriver

    override var generalConfig: GroupConfiguration
    override var messageConfig: FileConfiguration
    override var botConfig: FileConfiguration

    var pluginLoader: PluginLoader

    fun enable() {
        AQQBotAPI.setInstance(this)
        log(LogLevel.INFO, "Loading libraries...")
        loadCommonDependencies()
        loadDependencies()
        log(LogLevel.INFO, "Loading config...")
        loadConfig(this)
        log(LogLevel.INFO, "Loading data...")
        loadData(DataStorageType.valueOf(generalConfig.getString("storage.type", null).uppercase()))
        log(LogLevel.INFO, "Loading debug system...")
        loadDebug()
        log(LogLevel.INFO, "Registering commands...")
        loadCommands(this)
        log(LogLevel.INFO, "Loading command execution system ...")
        sender = ConcurrentHashMap()
        setSender()
        log(LogLevel.INFO, "Command Execution System: ${sender.map { it.key.toString() to it.value.name }.joinToString(", ")}")
        log(LogLevel.INFO, "Loading formatters...")
        toGroupFormatter = ConcurrentHashMap()
        toGameFormatter = ConcurrentHashMap()
        enableGroups.forEach { group, _ ->
            toGroupFormatter[group.toLong()] = AFormatter(this)
            toGameFormatter[group.toLong()] = AFormatter(this)
            toGroupFormatter[group.toLong()]?.initialUrl(generalConfig.getStringList("chat.server_to_group.filter", group.toLong()))
            toGameFormatter[group.toLong()]?.initialUrl(generalConfig.getStringList("chat.group_to_server.filter", group.toLong()))
        }
        adapter = loadAdapter()
        if (generalConfig.getBoolean("webhook.enable", null)) {
            log(LogLevel.INFO, "Loading webhook server...")
            WebhookProvider.create(this, InetSocketAddress(
                generalConfig.getString("webhook.host", null),
                generalConfig.getInt("webhook.port", null)))
            WebhookProvider.start()
            try {
                serverUUID = UUID.fromString(generalConfig.getString("webhook.server_uuid", null))
            } catch (e: Exception) {
                serverUUID = UUID.randomUUID()
                generalConfig.set("webhook.server_uuid", serverUUID.toString())
                generalConfig.generalConfig.save(File(getDataFolder(), "config.yml"))
            }
        }
        log(LogLevel.INFO, "Connecting to the bot...")
        if (botConfig.getString("access_token").isNullOrBlank()) {
            loadBot(
                this,
                URI.create("ws://" + botConfig.getString("ws.host") + ":" + botConfig.getInt("ws.port"))
            )
        } else {
            loadBot(
                this,
                URI.create("ws://" + botConfig.getString("ws.host") + ":" + botConfig.getInt("ws.port")),
                botConfig.getString("access_token")
            )
        }
        submitTimerAsync(0L, botConfig.getLong("check_interval") * 20) {
            if (getBot()?.isConnected != true) {
                debugModule?.debugLogger?.log("Bot disconnected, trying to reconnect...")
                if (botConfig.getString("access_token").isNullOrBlank()) {
                    loadBot(
                        this,
                        URI.create("ws://" + botConfig.getString("ws.host") + ":" + botConfig.getInt("ws.port"))
                    )
                } else {
                    loadBot(
                        this,
                        URI.create("ws://" + botConfig.getString("ws.host") + ":" + botConfig.getInt("ws.port")),
                        botConfig.getString("access_token")
                    )
                }
            }
        }
        log(LogLevel.INFO, "Loading hooks...")
        loadHook(this)
        if (generalConfig.getAllString("whitelist.verify_method").map { it.uppercase() }.contains("VERIFY_CODE")) {
            submitTimerAsync(0L, 5 * 20L) {
                verifyCodeMap.forEach {
                    if (System.currentTimeMillis() - it.value.second >
                           generalConfig.getAllLong("whitelist.verify_code_expire_time")[0] * 1000L) {
                        verifyCodeMap.remove(it.key)
                    }
                }
            }
        }
        submitTimerAsync(0L, 1 * 20L) {
            for ((k, v) in bindCooldownMap) {
                if (v <= 0) {
                    bindCooldownMap.remove(k)
                } else {
                    bindCooldownMap[k] = v - 1;
                }
            }
            for ((k, v) in unbindCooldownMap) {
                if (v <= 0) {
                    unbindCooldownMap.remove(k)
                } else {
                    unbindCooldownMap[k] = v - 1;
                }
            }
        }
        log(LogLevel.INFO, "Loading plugins...")
        pluginLoader = PluginLoader(this)
        pluginLoader.load()
        if (getBot() != null && getBot()!!.isConnected()) {
            enableGroups.forEach {
                if (!generalConfig.getBoolean("notify.server_status.enable", it.key.toLong())) return@forEach
                debugModule?.debugLogger?.log("Plugin initialized, sending server status message to ${it.key}")
                getBot()!!.action(SendGroupMessage(it.key.toLong(),
                    if (generalConfig.getStringList("notify.server_status.start", it.key.toLong()).isEmpty())
                        generalConfig.getString("notify.server_status.start", it.key.toLong())
                    else generalConfig.getStringList("notify.server_status.start", it.key.toLong()).random()
                ))
            }
        }
    }

    fun loadCommonDependencies() {
        libraryManager.addRepository("https://maven.aliyun.com/repository/public")
        libraryManager.addMavenCentral()
        libraryManager.addJitPack()
        if (System.getProperty("java.specification.version").toDouble() >= 15) {
            val nashornLib = Library.builder()
                .groupId("org{}openjdk{}nashorn")
                .artifactId("nashorn-core")
                .version("15.6")
                .resolveTransitiveDependencies(true)
                .build()
            libraryManager.loadLibrary(nashornLib)
        }
        val databaseLib = Library.builder()
            .groupId("com{}github{}alazeprt")
            .artifactId("taboolib-database")
            .version("1.0.4")
            .relocate("com{}google{}common", "top{}alazeprt{}aqqbot{}lib{}com{}google{}common")
            .build()
        val hikaricpLib = Library.builder()
            .groupId("com{}zaxxer")
            .artifactId("HikariCP")
            .version("4.0.3")
            .resolveTransitiveDependencies(true)
            .build()
        val guavaLib = Library.builder()
            .groupId("com{}google{}guava")
            .artifactId("guava")
            .version("21.0")
            .relocate("com{}google{}common", "top{}alazeprt{}aqqbot{}lib{}com{}google{}common")
            .resolveTransitiveDependencies(true)
            .build()
        val sqliteLib = Library.builder()
            .groupId("org{}xerial")
            .artifactId("sqlite-jdbc")
            .version("3.49.0.0")
            .resolveTransitiveDependencies(true)
            .build()
        val aconfigurationLib = Library.builder()
            .groupId("com{}github{}alazeprt")
            .artifactId("AConfiguration")
            .version("1.2")
            .build()
        val mysqlLib = Library.builder()
            .groupId("com{}mysql")
            .artifactId("mysql-connector-j")
            .version("8.3.0")
            .resolveTransitiveDependencies(true)
            .build()
        val aonebotLib = Library.builder()
            .groupId("com{}github{}alazeprt")
            .artifactId("AOneBot")
            .version("1.0.18-beta")
            .relocate("com{}google{}code{}gson", "top{}alazeprt{}aonebot{}lib{}com{}google")
            .resolveTransitiveDependencies(true)
            .build()
        libraryManager.loadLibraries(databaseLib, hikaricpLib, guavaLib, sqliteLib, aconfigurationLib, mysqlLib, aonebotLib)
    }

    fun loadDependencies()

    fun disable() {
        log(LogLevel.INFO, "Unloading plugins...")
        pluginLoader.unload()
        log(LogLevel.INFO, "Disconnecting bot...")
        if (getBot() != null && getBot()!!.isConnected) {
            enableGroups.forEach {
                debugModule?.debugLogger?.log("Plugin is disabling, sending server status message to ${it.key}")
                if (!generalConfig.getBoolean("notify.server_status.enable", it.key.toLong())) return@forEach
                getBot()!!.action(SendGroupMessage(it.key.toLong(),
                    if (generalConfig.getStringList("notify.server_status.stop", it.key.toLong()).isEmpty())
                        generalConfig.getString("notify.server_status.stop", it.key.toLong())
                    else generalConfig.getStringList("notify.server_status.stop", it.key.toLong()).random()
                ))
            }
        }
        unloadBot()
        log(LogLevel.INFO, "Saving data...")
        saveData(DataStorageType.valueOf(generalConfig.getString("storage.type", null).uppercase()))
        log(LogLevel.INFO, "Closing webhook server...")
        WebhookProvider.stop()
        log(LogLevel.INFO, "Unloading debug system...")
        unloadDebug()
    }

    fun reload() {
        pluginLoader.unload()
        loadConfig(this)
        sender.clear()
        setSender()
        unloadBot()
        if (botConfig.getString("access_token").isNullOrBlank()) {
            loadBot(
                this,
                URI.create("ws://" + botConfig.getString("ws.host") + ":" + botConfig.getInt("ws.port"))
            )
        } else {
            loadBot(
                this,
                URI.create("ws://" + botConfig.getString("ws.host") + ":" + botConfig.getInt("ws.port")),
                botConfig.getString("access_token")
            )
        }
        WebhookProvider.stop()
        if (generalConfig.getBoolean("webhook.enable", null)) {
            WebhookProvider.create(this, InetSocketAddress(
                generalConfig.getString("webhook.host", null),
                generalConfig.getInt("webhook.port", null)))
            WebhookProvider.start()
            try {
                serverUUID = UUID.fromString(generalConfig.getString("webhook.server_uuid", null))
            } catch (e: Exception) {
                serverUUID = UUID.randomUUID()
                generalConfig.set("webhook.server_uuid", serverUUID.toString())
                generalConfig.generalConfig.save(File(getDataFolder(), "config.yml"))
            }
        }
        reloadDebug()
        pluginLoader.load()
    }

    fun loadDebug() {
        debugModule = ADebug(this)
        debugModule?.load()
    }

    fun unloadDebug() {
        debugModule?.unload()
        debugModule = null
    }

    fun loadAdapter(): AQQBotAdapter

    fun reloadDebug() {
        debugModule?.reload()
    }

    fun log(level: LogLevel, message: String)

    fun log(level: Int, message: String) {
        when (level) {
            0 -> log(LogLevel.TRACE, message)
            1 -> log(LogLevel.DEBUG, message)
            2 -> log(LogLevel.INFO, message)
            3 -> log(LogLevel.WARN, message)
            4 -> log(LogLevel.ERROR, message)
            5 -> log(LogLevel.FATAL, message)
            else -> log(LogLevel.INFO, message)
        }
    }

    fun setSender()

    fun getBrandName(): String

    fun getServerVersion(): String

    override fun loadData(type: DataStorageType) {
        dataProvider = when (type) {
            DataStorageType.SQLITE -> SQLiteProvider(this)
            DataStorageType.MYSQL -> MySQLProvider(this)
            DataStorageType.FILE -> FileDataProvider(this)
        }
        dataProvider.loadData(type)
    }

    override fun getStorageType(): DataStorageType {
        return dataProvider.getStorageType()
    }

    override fun saveData(type: DataStorageType) {
        return dataProvider.saveData(type)
    }

    override fun saveData(type: Int) {
        return dataProvider.saveData(type)
    }

    override fun hasPlayer(player: AOfflinePlayer): Boolean {
        return dataProvider.hasPlayer(player)
    }

    override fun hasPlayer(name: String): Boolean {
        return dataProvider.hasPlayer(name)
    }

    override fun hasQQ(qq: Long): Boolean {
        return dataProvider.hasQQ(qq)
    }

    override fun addPlayer(qq: Long, player: AOfflinePlayer) {
        return dataProvider.addPlayer(qq, player)
    }

    override fun addPlayer(qq: Long, name: String) {
        return dataProvider.addPlayer(qq, name)
    }

    override fun removePlayer(player: AOfflinePlayer) {
        return dataProvider.removePlayer(player)
    }

    override fun removePlayer(name: String) {
        return dataProvider.removePlayer(name)
    }

    override fun removePlayer(qq: Long) {
        return dataProvider.removePlayer(qq)
    }

    override fun removePlayer(qq: Long, player: AOfflinePlayer) {
        return dataProvider.removePlayer(qq, player)
    }

    override fun removePlayer(qq: Long, name: String) {
        return dataProvider.removePlayer(qq, name)
    }

    override fun getQQByPlayer(player: AOfflinePlayer): Long? {
        return dataProvider.getQQByPlayer(player)
    }

    override fun getQQByPlayer(name: String): Long? {
        return dataProvider.getQQByPlayer(name)
    }

    override fun getPlayerByQQ(qq: Long): List<AOfflinePlayer> {
        return dataProvider.getPlayerByQQ(qq)
    }

    override fun getPlayerNameByQQ(qq: Long): List<String> {
        return dataProvider.getPlayerNameByQQ(qq)
    }

    override fun submitCommand(command: String, groupId: Long): CompletableFuture<AExecution> {
        val senderInstance: AExecution = sender[groupId]!!.constructors[0].newInstance(this) as AExecution
        for (method in senderInstance.javaClass.methods) {
            if (method.name.contains("check") && method.parameterCount == 0) {
                method.invoke(senderInstance)
            } else if (method.name.contains("check")) {
                method.invoke(senderInstance, groupId)
            }
        }
        return senderInstance.execute(command, groupId)
    }

    fun handleImage(url: String): TextComponent?
}