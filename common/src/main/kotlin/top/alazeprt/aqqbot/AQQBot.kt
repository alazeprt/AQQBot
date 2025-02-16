package top.alazeprt.aqqbot

import com.alessiodp.libby.Library
import com.alessiodp.libby.LibraryManager
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

    var libraryManager: LibraryManager

    override var generalConfig: FileConfiguration
    override var messageConfig: FileConfiguration
    override var botConfig: FileConfiguration

    fun enable() {
        loadDependencies()
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

    fun loadDependencies() {
        val adventureBukkitLib = Library.builder()
            .groupId("net{}kyori")
            .artifactId("adventure-platform-bukkit")
            .version("4.3.4")
            .relocate("net{}kyori", "top{}alazeprt{}aqqbot{}lib")
            .resolveTransitiveDependencies(true)
            .build()
        val databaseLib = Library.builder()
            .groupId("com{}github{}alazeprt")
            .artifactId("taboolib-database")
            .version("1.0.4")
            .relocate("taboolib", "top{}alazeprt{}aqqbot{}lib{}taboolib")
            .relocate("com{}zaxxer", "top{}alazeprt{}aqqbot{}lib{}com{}zaxxer")
            .relocate("com{}google{}common", "top{}alazeprt{}aqqbot{}lib{}com{}google{}common")
            .relocate("org{}sqlite", "top{}alazeprt{}aqqbot{}lib{}org{}sqlite")
            .relocate("com{}mysql", "top{}alazeprt{}aqqbot{}lib{}com{}mysql")
            .build()
        val hikaricpLib = Library.builder()
            .groupId("com{}zaxxer")
            .artifactId("HikariCP")
            .version("4.0.3")
            .relocate("com{}zaxxer", "top{}alazeprt{}aqqbot{}lib{}com{}zaxxer")
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
            .relocate("org{}sqlite", "top{}alazeprt{}aqqbot{}lib{}org{}sqlite")
            .resolveTransitiveDependencies(true)
            .build()
        val aconfigurationLib = Library.builder()
            .groupId("com{}github{}alazeprt")
            .artifactId("AConfiguration")
            .version("1.2")
            .relocate("com{}github{}alazeprt", "top{}alazeprt{}aqqbot{}lib{}aconfiguration")
            .build()
        val mysqlLib = Library.builder()
            .groupId("com{}mysql")
            .artifactId("mysql-connector-j")
            .version("8.3.0")
            .relocate("com{}mysql", "top{}alazeprt{}aqqbot{}lib{}com{}mysql")
            .resolveTransitiveDependencies(true)
            .build()
        val aonebotLib = Library.builder()
            .groupId("com{}github{}alazeprt")
            .artifactId("AOneBot")
            .version("1.0.10-beta.2")
            .relocate("org{}java_websocket", "top{}alazeprt{}aonebot{}lib{}java_websocket")
            .relocate("com{}google{}code{}gson", "top{}alazeprt{}aonebot{}lib{}com{}google")
            .resolveTransitiveDependencies(true)
            .build()
        libraryManager.addRepository("https://maven.aliyun.com/repository/public")
        libraryManager.addMavenCentral()
        libraryManager.addJitPack()
        libraryManager.loadLibrary(adventureBukkitLib)
        libraryManager.loadLibrary(guavaLib)
        libraryManager.loadLibrary(hikaricpLib)
        libraryManager.loadLibrary(sqliteLib)
        libraryManager.loadLibrary(mysqlLib)
        libraryManager.loadLibrary(aconfigurationLib)
        libraryManager.loadLibrary(databaseLib)
        libraryManager.loadLibrary(aonebotLib)
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