package top.alazeprt.aqqbot

import com.alessiodp.libby.BungeeLibraryManager
import com.alessiodp.libby.Library
import com.alessiodp.libby.LibraryManager
import net.kyori.adventure.platform.bungeecord.BungeeAudiences
import net.kyori.adventure.text.TextComponent
import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.plugin.Command
import net.md_5.bungee.api.plugin.Plugin
import org.bstats.bungeecord.Metrics
import top.alazeprt.aconfiguration.file.FileConfiguration
import top.alazeprt.aconfiguration.file.YamlConfiguration
import top.alazeprt.aqqbot.adapter.ABungeeCustom
import top.alazeprt.aqqbot.adapter.AQQBotAdapter
import top.alazeprt.aqqbot.adapter.BungeeAdapter
import top.alazeprt.aqqbot.adapter.BungeeSender
import top.alazeprt.aqqbot.adapter.BungeeTask
import top.alazeprt.aqqbot.command.ACommand
import top.alazeprt.aqqbot.config.MessageManager
import top.alazeprt.aqqbot.data.DataProvider
import top.alazeprt.aqqbot.debug.ADebug
import top.alazeprt.aqqbot.drivers.Web2ImageDriver
import top.alazeprt.aqqbot.event.BungeeEventHandler
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import top.alazeprt.aqqbot.profile.APlayer
import top.alazeprt.aqqbot.scripts.ScriptLoader
import top.alazeprt.aqqbot.util.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.logging.Level


class AQQBotBungee : AQQBot, Plugin() {
    override var debugModule: ADebug? = null

    override var adapter: AQQBotAdapter = BungeeAdapter(this)

    override val verifyCodeMap: MutableMap<String, Pair<String, Long>> = ConcurrentHashMap()

    override lateinit var dataProvider: DataProvider

    override lateinit var enableGroups: MutableMap<String, FileConfiguration?>

    override val bindCooldownMap: MutableMap<String, Long> = ConcurrentHashMap()
    override val unbindCooldownMap: MutableMap<String, Long> = ConcurrentHashMap()

    override lateinit var toGameFormatter: MutableMap<Long, AFormatter>
    override lateinit var sender: MutableMap<Long, Class<out AExecution>>
    override lateinit var toGroupFormatter: MutableMap<Long, AFormatter>

    override lateinit var libraryManager: LibraryManager

    override lateinit var customCommands: MutableList<ACustom>
    override lateinit var generalConfig: GroupConfiguration
    override lateinit var messageConfig: FileConfiguration
    override lateinit var botConfig: FileConfiguration
    override lateinit var customConfig: FileConfiguration

    companion object {
        lateinit var audience: BungeeAudiences
    }

    override fun onEnable() {
        libraryManager = BungeeLibraryManager(this)
        this.enable()
        audience = BungeeAudiences.create(this)
        proxy.pluginManager.registerListener(this, BungeeEventHandler(this))
        val metrics = Metrics(this, pluginId)
    }

    override fun loadCustomConfig() {
        val file = dataFolder.resolve("custom.yml")
        if (!file.exists()) {
            saveResource("custom.yml", false)
        }
        customCommands = mutableListOf()
        val reader = InputStreamReader(FileInputStream(file), Charsets.UTF_8)
        customConfig = YamlConfiguration.loadConfiguration(reader)
        customConfig.getKeys(false).forEach {
            val enable = customConfig.getBoolean("$it.enable")
            val command = customConfig.getStringList("$it.command")
            val execute = customConfig.getStringList("$it.execute")
            val unbind_execute = customConfig.getStringList("$it.unbind_execute")
            val output = customConfig.getStringList("$it.output")
            val unbind_output = customConfig.getStringList("$it.unbind_output")
            var image: AImage? = null
            if (customConfig.contains("$it.image")) {
                val path = customConfig.getString("$it.image.path")
                val elements = mutableListOf<AImageElement>()
                customConfig.getConfigurationSection("$it.image.elements").getKeys(false).forEach { k ->
                    val type = customConfig.getString("$it.image.elements.$k.type")
                    val data = customConfig.get("$it.image.elements.$k.data")
                    val x = customConfig.getDouble("$it.image.elements.$k.x")
                    val y = customConfig.getDouble("$it.image.elements.$k.y")
                    when (type) {
                        "text" -> {
                            val size = customConfig.getInt("$it.image.elements.$k.size")
                            val font = customConfig.getString("$it.image.elements.$k.font")
                            val color = customConfig.getString("$it.image.elements.$k.color")
                            val bold = customConfig.getBoolean("$it.image.elements.$k.bold")
                            val italic = customConfig.getBoolean("$it.image.elements.$k.italic")
                            elements.add(AImageText(data.toString(), x, y, size, font, color, bold, italic))
                        }
                        else -> {
                            log(LogLevel.WARN, "Unknown image element type $type (in custom configuration)")
                        }
                    }
                }
                image = AImage(File(dataFolder.resolve("images"), path), elements)
            }
            var unbind_image: AImage? = null
            if (customConfig.contains("$it.unbind_image")) {
                val path = customConfig.getString("$it.unbind_image.path")
                val elements = mutableListOf<AImageElement>()
                customConfig.getConfigurationSection("$it.unbind_image.elements").getKeys(false).forEach { k ->
                    val type = customConfig.getString("$it.unbind_image.elements.$k.type")
                    val data = customConfig.get("$it.unbind_image.elements.$k.data")
                    val x = customConfig.getDouble("$it.unbind_image.elements.$k.x")
                    val y = customConfig.getDouble("$it.unbind_image.elements.$k.y")
                    when (type) {
                        "text" -> {
                            val size = customConfig.getInt("$it.unbind_image.elements.$k.size")
                            val font = customConfig.getString("$it.unbind_image.elements.$k.font")
                            val color = customConfig.getString("$it.unbind_image.elements.$k.color")
                            val bold = customConfig.getBoolean("$it.unbind_image.elements.$k.bold")
                            val italic = customConfig.getBoolean("$it.unbind_image.elements.$k.italic")
                            elements.add(AImageText(data.toString(), x, y, size, font, color, bold, italic))
                        }
                        else -> {
                            log(LogLevel.WARN, "Unknown image element type $type (in custom configuration)")
                        }
                    }
                }
                unbind_image = AImage(File(dataFolder.resolve("images"), path), elements)
            }
            var web: AWeb? = null
            if (customConfig.contains("$it.web")) {
                val path = customConfig.getString("$it.web.path")
                val width = customConfig.getInt("$it.web.width")
                val height = customConfig.getInt("$it.web.height")
                val delay = customConfig.getLong("$it.web.delay")
                val placeholders = customConfig.getConfigurationSection("$it.web.placeholders")
                val placeholdersMap = mutableMapOf<String, String>()
                placeholders.getKeys(false).forEach { k ->
                    placeholdersMap[k] = placeholders.get(k).toString()
                }
                web = AWeb(File(dataFolder.resolve("web"), path), width, height, delay, placeholdersMap)
            }
            var unbind_web: AWeb? = null
            if (customConfig.contains("$it.unbind_web")) {
                val path = customConfig.getString("$it.unbind_web.path")
                val width = customConfig.getInt("$it.unbind_web.width")
                val height = customConfig.getInt("$it.unbind_web.height")
                val delay = customConfig.getLong("$it.unbind_web.delay")
                val placeholders = customConfig.getConfigurationSection("$it.unbind_web.placeholders")
                val placeholdersMap = mutableMapOf<String, String>()
                placeholders.getKeys(false).forEach { k ->
                    placeholdersMap[k] = placeholders.get(k).toString()
                }
                unbind_web = AWeb(File(dataFolder.resolve("web"), path), width, height, delay, placeholdersMap)
            }
            if ((web != null || unbind_web != null) && enable) {
                submitAsync {
                    webDriver = Web2ImageDriver(this)
                    webDriver.loadDependencies()
                    webDriver.downloadDrivers()
                }
            }
            val format = customConfig.getBoolean("$it.format")
            val choose_account = if (customConfig.getInt("$it.choose_account") == 0) 1
            else customConfig.getInt("$it.choose_account")
            val permission = customConfig.getString("$it.permission") ?: ""
            customCommands.add(ABungeeCustom(
                this, it, command, execute, unbind_execute, output, unbind_output, image, unbind_image, format, web, unbind_web, choose_account, enable, permission))
        }
    }

    override fun saveResource(name: String, replace: Boolean) {
        val configFile = File(getDataFolder(), name)

        if (replace || !configFile.exists()) {
            val outputStream = FileOutputStream(configFile)
            val inputStream = getResourceAsStream(name)
            inputStream.copyTo(outputStream)
        }
    }

    override lateinit var messageManager: MessageManager

    override lateinit var serverUUID: UUID

    private val executor = Executors.newFixedThreadPool(16)

    override lateinit var webDriver: Web2ImageDriver

    private val pluginId = 24071

    override var loadSparkCount: Int = 0

    override fun setPlaceholders(player: APlayer, message: String): String {
        return message
    }

    override var fakePlayer: Boolean = false

    override var spark: Boolean = false
    override var luckperms: Boolean = false

    override lateinit var scriptLoader: ScriptLoader

    override fun loadDependencies() {
        val adventureBungeeLib = Library.builder()
            .groupId("net{}kyori")
            .artifactId("adventure-platform-bungee")
            .version("4.4.1")
            .resolveTransitiveDependencies(true)
            .build()
        libraryManager.loadLibraries(adventureBungeeLib)
    }

    override fun loadAdapter(): AQQBotAdapter {
        return adapter!!
    }

    override fun log(level: LogLevel, message: String) {
        when (level) {
            LogLevel.TRACE -> logger.log(Level.FINEST, message)
            LogLevel.DEBUG -> logger.log(Level.FINE, message)
            LogLevel.INFO -> logger.info(message)
            LogLevel.WARN -> logger.warning(message)
            LogLevel.ERROR -> logger.log(Level.SEVERE, message)
            LogLevel.FATAL -> logger.log(Level.SEVERE, message)
        }
    }

    override fun setSender() {}

    override fun getBrandName(): String {
        return proxy.name
    }

    override fun getServerVersion(): String {
        return proxy.gameVersion
    }

    override fun handleImage(url: String): TextComponent? {
        return null
    }

    override fun registerCommand(command: String, handler: ACommand) {
        proxy.pluginManager.registerCommand(this, object : Command("aqqbot") {
            override fun execute(sender: CommandSender?, args: Array<out String?>?) {
                if (sender == null) return
                val list = mutableListOf<String>()
                for (arg in args ?: emptyArray()) {
                    list.add(arg ?: "")
                }
                handler.onCommand("aqqbot", BungeeSender(sender), list)
            }
        })
    }

    override fun getAllData(): Map<Long, List<AOfflinePlayer>> {
        return dataProvider.getAllData()
    }

    override fun submit(task: Runnable): Cancelable {
        return BungeeTask(proxy.scheduler.schedule(this, task, 0L, TimeUnit.MILLISECONDS))
    }

    override fun submitAsync(task: Runnable): Cancelable {
        val task = proxy.scheduler.runAsync(this) { task.run() }
        return BungeeTask(task)
    }

    override fun submitLater(delay: Long, task: Runnable): Cancelable {
        val task = proxy.scheduler.schedule(this, task, delay * 50L, TimeUnit.MILLISECONDS)
        return BungeeTask(task)
    }

    override fun submitLaterAsync(delay: Long, task: Runnable): Cancelable {
        val task = proxy.scheduler.runAsync(this) {
            Thread.sleep(delay * 50L)
            task.run()
        }
        return BungeeTask(task)
    }

    override fun submitTimer(
        delay: Long,
        period: Long,
        task: Runnable
    ): Cancelable {
        val task = proxy.scheduler.schedule(this, task, delay * 50L, period * 50L, TimeUnit.MILLISECONDS)
        return BungeeTask(task)
    }

    override fun submitTimerAsync(
        delay: Long,
        period: Long,
        task: Runnable
    ): Cancelable {
        val task = proxy.scheduler.runAsync(this) {
            Thread.sleep(delay * 50L)
            while (!Thread.currentThread().isInterrupted) {
                task.run()
                Thread.sleep(period * 50L)
            }
        }
        return BungeeTask(task)
    }
}