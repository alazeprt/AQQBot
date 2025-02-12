package top.alazeprt.aqqbot

import me.lucko.spark.api.Spark
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bukkit.Bukkit
import org.bukkit.command.CommandExecutor
import org.bukkit.plugin.java.JavaPlugin
import top.alazeprt.aconfiguration.file.FileConfiguration
import top.alazeprt.aqqbot.adapter.AQQBotAdapter
import top.alazeprt.aqqbot.adapter.BukkitAdapter
import top.alazeprt.aqqbot.adapter.BukkitConsoleSender
import top.alazeprt.aqqbot.adapter.BukkitSender
import top.alazeprt.aqqbot.command.ACommand
import top.alazeprt.aqqbot.config.BukkitConfigLoader
import top.alazeprt.aqqbot.data.DataProvider
import top.alazeprt.aqqbot.data.DataStorageType
import top.alazeprt.aqqbot.debug.ADebug
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import top.alazeprt.aqqbot.util.ACustom
import top.alazeprt.aqqbot.util.AExecution
import top.alazeprt.aqqbot.util.LogLevel
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future


object AQQBotBukkit : JavaPlugin(), AQQBot {
    override var debugModule: ADebug? = null

    override var adapter: AQQBotAdapter? = BukkitAdapter

    override val verifyCodeMap: MutableMap<String, Pair<String, Long>> = mutableMapOf()

    override lateinit var dataProvider: DataProvider

    lateinit var configLoader: BukkitConfigLoader

    override lateinit var enableGroups: MutableList<String>

    override lateinit var customCommands: MutableList<ACustom>
    override lateinit var generalConfig: FileConfiguration
    override lateinit var messageConfig: FileConfiguration
    override lateinit var botConfig: FileConfiguration

    override var spark: Spark? = null

    override var loadCount: Int = 0

    lateinit var audience: BukkitAudiences

    override fun onEnable() {
        configLoader = BukkitConfigLoader(this)
        this.enable()
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI")
        } catch (e: ClassNotFoundException) {
            log(LogLevel.WARN, "You don't install soft dependency PlaceholderAPI! You cannot use placeholder in anywhere!")
        }
        this.audience = BukkitAudiences.create(this);
    }

    override fun onDisable() {
        this.disable()
        this.audience.close()
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

    override fun setEnableGroups() {
        this.configLoader.setEnableGroups()
        this.enableGroups = this.configLoader.enableGroups
    }

    override fun loadGeneralConfig() {
        this.configLoader.loadGeneralConfig()
    }

    override fun loadMessageConfig() {
        this.configLoader.loadMessageConfig()
    }

    override fun loadBotConfig() {
        this.configLoader.loadBotConfig()
    }

    override fun loadCustomConfig() {
        this.configLoader.loadCustomConfig()
    }

    override fun getCustomConfig(): FileConfiguration {
        return this.configLoader.getCustomConfig()
    }

    override fun configNeedUpdate(): Boolean {
        return this.configLoader.configNeedUpdate()
    }

    override fun registerCommand(command: String, handler: ACommand) {
        getCommand(command)?.setExecutor(CommandExecutor { commandSender, command, s, strings ->
            handler.onCommand(s, BukkitSender(commandSender), strings.toList())
            false
        })
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
        sender.execute(command)
        return CompletableFuture.supplyAsync() {
            Thread.sleep(1000L * generalConfig.getInt("command_execution.delay"))
            sender
        }
    }

    fun getAdventure(): BukkitAudiences {
        return audience
    }
}