package top.alazeprt.aqqbot

import top.alazeprt.aconfiguration.file.FileConfiguration
import top.alazeprt.aonebot.action.SendGroupMessage
import top.alazeprt.aqqbot.adapter.AQQBotAdapter
import top.alazeprt.aqqbot.bot.BotProvider
import top.alazeprt.aqqbot.bot.BotProvider.loadBot
import top.alazeprt.aqqbot.bot.BotProvider.unloadBot
import top.alazeprt.aqqbot.command.CommandProvider
import top.alazeprt.aqqbot.config.ConfigProvider
import top.alazeprt.aqqbot.config.MessageManager
import top.alazeprt.aqqbot.data.*
import top.alazeprt.aqqbot.debug.ADebug
import top.alazeprt.aqqbot.event.AEvent
import top.alazeprt.aqqbot.hook.HookProvider
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import top.alazeprt.aqqbot.task.TaskProvider
import top.alazeprt.aqqbot.util.LogLevel
import java.net.URI

interface AQQBot: ConfigProvider, CommandProvider, DataProvider, HookProvider, TaskProvider {

    var debugModule: ADebug?

    var adapter: AQQBotAdapter?

    val verifyCodeMap: MutableMap<String, Pair<String, Long>>  // <name, <code, time>>

    var dataProvider: DataProvider

    override var generalConfig: FileConfiguration
    override var messageConfig: FileConfiguration
    override var botConfig: FileConfiguration

    fun enable() {
        loadConfig(this)
        loadData(DataStorageType.valueOf(generalConfig.getString("storage.type").uppercase()))
        loadDebug()
        loadCommands(this)
        adapter = loadAdapter()
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
        loadHook(this)
        if (generalConfig.getString("whitelist.verify_method")?.uppercase() == "VERIFY_CODE") {
            submitAsync {
                while (true) {
                    verifyCodeMap.forEach {
                        if (System.currentTimeMillis() - it.value.second >
                               generalConfig.getLong("whitelist.verify_code_expire_time") * 1000L) {
                            verifyCodeMap.remove(it.key)
                        }
                    }
                    Thread.sleep(5000)
                }
            }
        }
        if (generalConfig.getBoolean("notify.server_status.enable") && BotProvider.getBot() != null &&
                BotProvider.getBot()!!.isConnected()) {
            enableGroups.forEach {
                BotProvider.getBot()!!.action(SendGroupMessage(it.toLong(),
                    generalConfig.getString("notify.server_status.start")?: "[AQQBot] 服务器已启动!"))
            }
        }
    }

    fun disable() {
        if (generalConfig.getBoolean("notify.server_status.enable") && BotProvider.getBot() != null &&
            BotProvider.getBot()!!.isConnected) {
            enableGroups.forEach {
                BotProvider.getBot()!!.action(SendGroupMessage(it.toLong(),
                    generalConfig.getString("notify.server_status.stop")?: "[AQQBot] 服务器已关闭!"))
            }
        }
        unloadBot()
        saveData(DataStorageType.valueOf(generalConfig.getString("storage.type").uppercase()))
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
}