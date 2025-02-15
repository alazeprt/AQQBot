package top.alazeprt.aqqbot

import com.alessiodp.libby.BukkitLibraryManager
import com.alessiodp.libby.Library
import me.lucko.spark.api.Spark
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import top.alazeprt.aconfiguration.file.FileConfiguration
import top.alazeprt.aconfiguration.file.YamlConfiguration
import top.alazeprt.aqqbot.adapter.*
import top.alazeprt.aqqbot.command.ACommand
import top.alazeprt.aqqbot.data.DataProvider
import top.alazeprt.aqqbot.debug.ADebug
import top.alazeprt.aqqbot.event.BukkitEventHandler
import top.alazeprt.aqqbot.util.ACustom
import top.alazeprt.aqqbot.util.AExecution
import top.alazeprt.aqqbot.util.LogLevel
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future


class AQQBotBukkit : JavaPlugin(), AQQBot {
    override var debugModule: ADebug? = null

    override var adapter: AQQBotAdapter? = BukkitAdapter

    override val verifyCodeMap: MutableMap<String, Pair<String, Long>> = mutableMapOf()

    override lateinit var dataProvider: DataProvider

    override lateinit var enableGroups: MutableList<String>

    override lateinit var customCommands: MutableList<ACustom>
    override lateinit var generalConfig: FileConfiguration
    override lateinit var messageConfig: FileConfiguration
    override lateinit var botConfig: FileConfiguration
    override lateinit var customConfig: FileConfiguration

    override var spark: Spark? = null

    override var loadCount: Int = 0

    companion object {
        lateinit var audience: BukkitAudiences
    }

    override fun onEnable() {
        this.enable()
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI")
        } catch (e: ClassNotFoundException) {
            log(LogLevel.WARN, "You don't install soft dependency PlaceholderAPI! You cannot use placeholder in anywhere!")
        }
        audience = BukkitAudiences.create(this);
        server.pluginManager.registerEvents(BukkitEventHandler(this), this)
    }

    override fun onDisable() {
        this.disable()
        audience.close()
    }

    override fun loadAdapter(): AQQBotAdapter {
        return adapter!!
    }

    override fun log(level: LogLevel, message: String) {
        when (level) {
            LogLevel.TRACE -> logger.finest(message)
            LogLevel.DEBUG -> logger.fine(message)
            LogLevel.INFO -> logger.info(message)
            LogLevel.WARN -> logger.warning(message)
            LogLevel.ERROR -> logger.severe(message)
            LogLevel.FATAL -> logger.severe(message)
        }
    }

    override fun registerCommand(command: String, handler: ACommand) {
        getCommand(command)?.setExecutor { commandSender, _, s, strings ->
            handler.onCommand(s, BukkitSender(commandSender), strings.toList())
            false
        }
    }

    override fun submit(task: Runnable): Future<*> {
        Bukkit.getScheduler().runTask(this, task)
        return CompletableFuture.completedFuture<Void>(null)
    }

    override fun submitAsync(task: Runnable): Future<*> {
        Bukkit.getScheduler().runTaskAsynchronously(this, task)
        return CompletableFuture.completedFuture<Void>(null)
    }

    override fun submitLater(delay: Long, task: Runnable): Future<*> {
        Bukkit.getScheduler().runTaskLater(this, task, delay)
        return CompletableFuture.completedFuture<Void>(null)
    }

    override fun submitLaterAsync(delay: Long, task: Runnable): Future<*> {
        Bukkit.getScheduler().runTaskLaterAsynchronously(this, task, delay)
        return CompletableFuture.completedFuture<Void>(null)
    }

    override fun submitTimer(delay: Long, period: Long, task: Runnable): Future<*> {
        Bukkit.getScheduler().runTaskTimer(this, task, delay, period)
        return CompletableFuture.completedFuture<Void>(null)
    }

    override fun submitTimerAsync(delay: Long, period: Long, task: Runnable): Future<*> {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, task, delay, period)
        return CompletableFuture.completedFuture<Void>(null)
    }

    override fun submitCommand(command: String): CompletableFuture<AExecution> {
        val sender = BukkitConsoleSender(this)
        submit {
            sender.execute(command)
        }
        return CompletableFuture.supplyAsync {
            Thread.sleep(1000L * generalConfig.getInt("command_execution.delay"))
            sender
        }
    }

    fun getAdventure(): BukkitAudiences {
        return audience
    }

    override fun loadCustomConfig() {
        val file = File(dataFolder, "custom.yml")
        if (!file.exists()) {
            saveResource("custom.yml", false)
        }
        customCommands = mutableListOf()
        customConfig = YamlConfiguration.loadConfiguration(file)
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
                customCommands.add(ABukkitCustom(this, command, execute, unbind_execute, output, unbind_output, format, choose_account))
            }
        }
    }

    override fun loadDependencies() {
        val libraryManager = BukkitLibraryManager(this)
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
}