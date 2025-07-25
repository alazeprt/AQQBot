package top.alazeprt.aqqbot

import com.alessiodp.libby.BukkitLibraryManager
import com.alessiodp.libby.Library
import com.alessiodp.libby.LibraryManager
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import top.alazeprt.aconfiguration.file.FileConfiguration
import top.alazeprt.aconfiguration.file.YamlConfiguration
import top.alazeprt.aqqbot.adapter.*
import top.alazeprt.aqqbot.api.webhook.AQQBotWebhookServer
import top.alazeprt.aqqbot.command.ACommand
import top.alazeprt.aqqbot.config.MessageManager
import top.alazeprt.aqqbot.data.DataProvider
import top.alazeprt.aqqbot.debug.ADebug
import top.alazeprt.aqqbot.event.BukkitEventHandler
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import top.alazeprt.aqqbot.profile.APlayer
import top.alazeprt.aqqbot.util.*
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit


class AQQBotBukkit : JavaPlugin(), AQQBot {
    override var debugModule: ADebug? = null

    override var adapter: AQQBotAdapter = BukkitAdapter

    override val verifyCodeMap: MutableMap<String, Pair<String, Long>> = ConcurrentHashMap()

    override val bindCooldownMap: MutableMap<String, Long> = ConcurrentHashMap()
    override val unbindCooldownMap: MutableMap<String, Long> = ConcurrentHashMap()

    override lateinit var dataProvider: DataProvider

    override lateinit var enableGroups: MutableMap<String, FileConfiguration?>

    override lateinit var toGameFormatter: MutableMap<Long, AFormatter>
    override lateinit var toGroupFormatter: MutableMap<Long, AFormatter>

    override lateinit var sender: MutableMap<Long, Class<out AExecution>>

    override lateinit var libraryManager: LibraryManager

    override lateinit var customCommands: MutableList<ACustom>
    override lateinit var generalConfig: GroupConfiguration
    override lateinit var messageConfig: FileConfiguration
    override lateinit var botConfig: FileConfiguration
    override lateinit var customConfig: FileConfiguration

    override lateinit var messageManager: MessageManager

    private val pluginId = 24071

    override var webhookServer: AQQBotWebhookServer? = null

    override lateinit var serverUUID: UUID

    override var spark: Boolean = false

    override var loadSparkCount: Int = 0

    val taskList: MutableList<BukkitTaskCancelable> = mutableListOf()

    companion object {
        lateinit var audience: BukkitAudiences
    }

    override fun onEnable() {
        libraryManager = BukkitLibraryManager(this)
        this.enable()
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI")
        } catch (e: ClassNotFoundException) {
            log(LogLevel.WARN, "You don't install soft dependency PlaceholderAPI! You cannot use placeholder in anywhere!")
        }
        audience = BukkitAudiences.create(this);
        server.pluginManager.registerEvents(BukkitEventHandler(this), this)
        val metrics = Metrics(this, pluginId)
    }

    override fun onDisable() {
        log(LogLevel.INFO , "Canceling task")
        taskList.forEach {
            it.cancel()
        }
        this.disable()
        audience.close()
    }

    override fun setPlaceholders(player: APlayer, message: String): String {
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI")
            val bukkitPlayer = player as BukkitPlayer
            return PlaceholderAPI.setPlaceholders(bukkitPlayer.player, message)
        } catch (e: ClassNotFoundException) {
            return message
        }
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
        getCommand(command)?.setTabCompleter { _, _, _, strings ->
            handler.onComplete(strings.toList())
        }
    }

    override fun getAllData(): Map<Long, List<AOfflinePlayer>> {
        return dataProvider.getAllData()
    }

    override fun submit(task: Runnable): Cancelable {
        server.globalRegionScheduler.run(this) {
            task.run()
        }
        val cancelable = BukkitTaskCancelable(this)
        taskList.add(cancelable)
        return cancelable
    }

    override fun submitAsync(task: Runnable): Cancelable {
        server.asyncScheduler.runNow(this) {
            task.run()
        }
        val cancelable = BukkitTaskCancelable(this)
        taskList.add(cancelable)
        return cancelable
    }

    override fun submitLater(delay: Long, task: Runnable): Cancelable {
        server.globalRegionScheduler.runDelayed(this, { task.run() }, delay)
        val cancelable = BukkitTaskCancelable(this)
        taskList.add(cancelable)
        return cancelable
    }

    override fun submitLaterAsync(delay: Long, task: Runnable): Cancelable {
        server.asyncScheduler.runDelayed(this, { task.run() }, delay * 50L, TimeUnit.MILLISECONDS)
        val cancelable = BukkitTaskCancelable(this)
        taskList.add(cancelable)
        return cancelable
    }

    override fun submitTimer(delay: Long, period: Long, task: Runnable): Cancelable {
        server.globalRegionScheduler.runAtFixedRate(this, { task.run() }, delay, period)
        val cancelable = BukkitTaskCancelable(this)
        taskList.add(cancelable)
        return cancelable
    }

    override fun submitTimerAsync(delay: Long, period: Long, task: Runnable): Cancelable {
        server.asyncScheduler.runAtFixedRate(this, { task.run() }, delay * 50L, period * 50L,
            TimeUnit.MILLISECONDS)
        val cancelable = BukkitTaskCancelable(this)
        taskList.add(cancelable)
        return cancelable
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
                var image: AImage? = null
                if (customConfig.contains("$it.image")) {
                    val path = customConfig.getString("$it.image")
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
                val format = customConfig.getBoolean("$it.format")
                val choose_account = if (customConfig.getInt("$it.choose_account") == 0) 1
                else customConfig.getInt("$it.choose_account")
                customCommands.add(ABukkitCustom(this, it, command, execute, unbind_execute, output, unbind_output, image,
                    unbind_image, format, choose_account))
            }
        }
    }

    override fun loadDependencies() {
        val adventureBukkitLib = Library.builder()
            .groupId("net{}kyori")
            .artifactId("adventure-platform-bukkit")
            .version("4.3.4")
            .resolveTransitiveDependencies(true)
            .build()
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
            .version("1.0.16-beta")
            .relocate("com{}google{}code{}gson", "top{}alazeprt{}aonebot{}lib{}com{}google")
            .resolveTransitiveDependencies(true)
            .build()

        libraryManager.addRepository("https://maven.aliyun.com/repository/public")
        libraryManager.addMavenCentral()
        libraryManager.addJitPack()
        libraryManager.loadLibraries(adventureBukkitLib, guavaLib, hikaricpLib, sqliteLib, mysqlLib, aconfigurationLib, databaseLib, aonebotLib)
    }

    override fun setSender() {
        enableGroups.forEach out@ { group, _ ->
            generalConfig.getStringList("command_execution.sort", group.toLong()).forEach {
                when (it.uppercase()) {
                    "NATIVE" -> if (NativeServerSender(this).check()) {
                        sender[group.toLong()] = NativeServerSender::class.java
                        return@out
                    }
                    "DECIDATED_SERVER" -> if (DecidatedServerSender(this).check()) {
                        sender[group.toLong()] = DecidatedServerSender::class.java
                        return@out
                    }
                    "MINECRAFT_SERVER" -> if (MinecraftServerSender(this).check()) {
                        sender[group.toLong()] = MinecraftServerSender::class.java
                        return@out
                    }
                    "RCON" -> {
                        val instance = RCONSender(this)
                        val pass = instance.check(group.toLong())
                        if (pass) {
                            sender[group.toLong()] = RCONSender::class.java
                            instance.close()
                            return@out
                        }
                    }
                    "SIMULATE_CONSOLE" -> {
                        sender[group.toLong()] = BukkitConsoleSender::class.java
                        return@out
                    }
                }
            }
        }
    }

    override fun getBrandName(): String {
        return Bukkit.getServer().name
    }

    override fun getServerVersion(): String {
        return Bukkit.getServer().version
    }
}