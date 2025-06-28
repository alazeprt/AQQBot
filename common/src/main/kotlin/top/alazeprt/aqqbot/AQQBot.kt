package top.alazeprt.aqqbot

import com.alessiodp.libby.LibraryManager
import top.alazeprt.aconfiguration.file.FileConfiguration
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.adapter.AQQBotAdapter
import top.alazeprt.aqqbot.bot.BotProvider.getBot
import top.alazeprt.aqqbot.bot.BotProvider.loadBot
import top.alazeprt.aqqbot.bot.BotProvider.unloadBot
import top.alazeprt.aqqbot.command.CommandProvider
import top.alazeprt.aqqbot.config.ConfigProvider
import top.alazeprt.aqqbot.config.MessageManager
import top.alazeprt.aqqbot.data.*
import top.alazeprt.aqqbot.debug.ADebug
import top.alazeprt.aqqbot.hook.HookProvider
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import top.alazeprt.aqqbot.task.TaskProvider
import top.alazeprt.aqqbot.util.AExecution
import top.alazeprt.aqqbot.util.AFormatter
import top.alazeprt.aqqbot.util.LogLevel
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap

interface AQQBot: ConfigProvider, CommandProvider, DataProvider, HookProvider, TaskProvider {

    var debugModule: ADebug?

    var adapter: AQQBotAdapter?

    val verifyCodeMap: MutableMap<String, Pair<String, Long>>  // <name, <code, time>>

    val bindCooldownMap: MutableMap<String, Long>   // <name, time>
    val unbindCooldownMap: MutableMap<String, Long>

    var dataProvider: DataProvider

    var toGroupFormatter: AFormatter
    var toGameFormatter: AFormatter

    var sender: Class<out AExecution>

    var libraryManager: LibraryManager

    override var generalConfig: FileConfiguration
    override var messageConfig: FileConfiguration
    override var botConfig: FileConfiguration

    fun enable() {
        log(LogLevel.INFO, "Loading libraries...")
        loadDependencies()
        log(LogLevel.INFO, "Loading config...")
        loadConfig(this)
        log(LogLevel.INFO, "Loading data...")
        loadData(DataStorageType.valueOf(generalConfig.getString("storage.type").uppercase()))
        log(LogLevel.INFO, "Loading debug system...")
        loadDebug()
        log(LogLevel.INFO, "Registering commands...")
        loadCommands(this)
        log(LogLevel.INFO, "Loading command execution system ...")
        setSender()
        log(LogLevel.INFO, "Command Execution System: ${sender.name}")
        log(LogLevel.INFO, "Loading formatters...")
        toGroupFormatter = AFormatter(this)
        toGameFormatter = AFormatter(this)
        toGroupFormatter.initialUrl(generalConfig.getStringList("chat.server_to_group.filter"))
        toGameFormatter.initialUrl(generalConfig.getStringList("chat.group_to_server.filter"))
        adapter = loadAdapter()
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
        if (generalConfig.getString("whitelist.verify_method")?.uppercase() == "VERIFY_CODE") {
            submitTimerAsync(0L, 5 * 20L) {
                verifyCodeMap.forEach {
                    if (System.currentTimeMillis() - it.value.second >
                           generalConfig.getLong("whitelist.verify_code_expire_time") * 1000L) {
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
        if (generalConfig.getBoolean("notify.server_status.enable") && getBot() != null &&
                getBot()!!.isConnected()) {
            enableGroups.forEach {
                getBot()!!.action(SendGroupMessage(it.toLong(),
                    if (generalConfig.getStringList("notify.server_status.start").isEmpty())
                        generalConfig.getString("notify.server_status.start")?: "[AQQBot] 服务器已启动!"
                    else generalConfig.getStringList("notify.server_status.start").random()?: "[AQQBot] 服务器已启动!"))
            }
        }
    }

    fun loadDependencies()

    fun disable() {
        log(LogLevel.INFO, "Disconnecting bot...")
        if (generalConfig.getBoolean("notify.server_status.enable") && getBot() != null &&
            getBot()!!.isConnected) {
            enableGroups.forEach {
                getBot()!!.action(SendGroupMessage(it.toLong(),
                    if (generalConfig.getStringList("notify.server_status.stop").isEmpty())
                        generalConfig.getString("notify.server_status.stop")?: "[AQQBot] 服务器已关闭!"
                    else generalConfig.getStringList("notify.server_status.stop").random()?: "[AQQBot] 服务器已关闭!"))
            }
        }
        unloadBot()
        log(LogLevel.INFO, "Saving data...")
        saveData(DataStorageType.valueOf(generalConfig.getString("storage.type").uppercase()))
        log(LogLevel.INFO, "Unloading debug system...")
        unloadDebug()
    }

    fun reload() {
        loadConfig(this)
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
        reloadDebug()
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

    fun getMessageManager(): MessageManager {
        return MessageManager(this)
    }

    fun setSender()

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

    override fun hasPlayer(player: AOfflinePlayer): Boolean {
        return dataProvider.hasPlayer(player)
    }

    override fun hasQQ(qq: Long): Boolean {
        return dataProvider.hasQQ(qq)
    }

    override fun addPlayer(qq: Long, player: AOfflinePlayer) {
        return dataProvider.addPlayer(qq, player)
    }

    override fun removePlayer(player: AOfflinePlayer) {
        return dataProvider.removePlayer(player)
    }

    override fun removePlayer(qq: Long) {
        return dataProvider.removePlayer(qq)
    }

    override fun removePlayer(qq: Long, player: AOfflinePlayer) {
        return dataProvider.removePlayer(qq, player)
    }

    override fun getQQByPlayer(player: AOfflinePlayer): Long? {
        return dataProvider.getQQByPlayer(player)
    }

    override fun getPlayerByQQ(qq: Long): List<AOfflinePlayer> {
        return dataProvider.getPlayerByQQ(qq)
    }

    override fun submitCommand(command: String): CompletableFuture<AExecution> {
        val senderInstance: AExecution = sender.constructors[0].newInstance(this) as AExecution
        if (senderInstance.javaClass.methods.map { it.name }.contains("check")) {
            senderInstance.javaClass.getMethod("check").invoke(senderInstance)
        }
        return senderInstance.execute(command)
    }
}