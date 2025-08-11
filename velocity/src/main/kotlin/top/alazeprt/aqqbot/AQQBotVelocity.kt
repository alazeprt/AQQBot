package top.alazeprt.aqqbot

import com.alessiodp.libby.Library
import com.alessiodp.libby.LibraryManager
import com.alessiodp.libby.VelocityLibraryManager
import com.google.inject.Inject
import com.velocitypowered.api.command.SimpleCommand
import com.velocitypowered.api.event.Subscribe
import com.velocitypowered.api.event.connection.DisconnectEvent
import com.velocitypowered.api.event.connection.LoginEvent
import com.velocitypowered.api.event.player.PlayerChatEvent
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent
import com.velocitypowered.api.plugin.Plugin
import com.velocitypowered.api.plugin.annotation.DataDirectory
import com.velocitypowered.api.proxy.ProxyServer
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bstats.velocity.Metrics
import org.slf4j.Logger
import top.alazeprt.aconfiguration.file.FileConfiguration
import top.alazeprt.aconfiguration.file.YamlConfiguration
import top.alazeprt.aqqbot.adapter.*
import top.alazeprt.aqqbot.api.webhook.AQQBotWebhookServer
import top.alazeprt.aqqbot.command.ACommand
import top.alazeprt.aqqbot.config.MessageManager
import top.alazeprt.aqqbot.data.DataProvider
import top.alazeprt.aqqbot.debug.ADebug
import top.alazeprt.aqqbot.event.AChatEvent
import top.alazeprt.aqqbot.event.AJoinEvent
import top.alazeprt.aqqbot.event.AQuitEvent
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import top.alazeprt.aqqbot.profile.APlayer
import top.alazeprt.aqqbot.util.*
import java.io.File
import java.nio.file.Path
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors


@Plugin(id = "aqqbot", name = "AQQBot", version = "2.0-alpha.12", url = "https://aqqbot.alazeprt.top", authors = ["alazeprt"])
class AQQBotVelocity : AQQBot {
    override var debugModule: ADebug? = null

    override var adapter: AQQBotAdapter = VelocityAdapter(this)

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

    override lateinit var messageManager: MessageManager

    override lateinit var serverUUID: UUID

    private val executor = Executors.newFixedThreadPool(16)

    private val pluginId = 24071

    override var loadSparkCount: Int = 0

    lateinit var server: ProxyServer
    lateinit var logger: Logger
    lateinit var dataFolder: Path
    lateinit var metricsFactory: Metrics.Factory

    override var fakePlayer: Boolean = false

    override var spark: Boolean = false

    @Inject
    fun AQQBotVelocity(server: ProxyServer?, logger: Logger?, @DataDirectory dataDirectory: Path,
                       metricsFactory: Metrics.Factory) {
        this.server = server!!
        this.logger = logger!!
        this.dataFolder = dataDirectory
        this.metricsFactory = metricsFactory
    }

    @Subscribe
    fun onProxyInitialization(event: ProxyInitializeEvent) {
        libraryManager = VelocityLibraryManager(this, logger, dataFolder, server.pluginManager)
        this.enable()
        metricsFactory.make(this, pluginId)
    }

    @Subscribe
    fun onProxyShutdown(event: ProxyShutdownEvent) {
        this.disable()
    }

    override fun setPlaceholders(player: APlayer, message: String): String {
        return message
    }

    override fun loadAdapter(): AQQBotAdapter {
        return adapter!!
    }

    override fun log(level: LogLevel, message: String) {
        when (level) {
            LogLevel.DEBUG -> logger.debug(message)
            LogLevel.TRACE -> logger.trace(message)
            LogLevel.INFO -> logger.info(message)
            LogLevel.WARN -> logger.warn(message)
            LogLevel.ERROR -> logger.error(message)
            LogLevel.FATAL -> logger.error(message)
        }
    }

    override fun loadCustomConfig() {
        val file = dataFolder.resolve("custom.yml").toFile()
        if (!file.exists()) {
            saveResource("custom.yml", false)
        }
        customCommands = mutableListOf()
        customConfig = YamlConfiguration.loadConfiguration(file)
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
                image = AImage(File(dataFolder.toFile().resolve("images"), path), elements)
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
                unbind_image = AImage(File(dataFolder.toFile().resolve("images"), path), elements)
            }
            val format = customConfig.getBoolean("$it.format")
            val choose_account = if (customConfig.getInt("$it.choose_account") == 0) 1
            else customConfig.getInt("$it.choose_account")
            customCommands.add(AVelocityCustom(
                this, it, command, execute, unbind_execute, output, unbind_output, image, unbind_image, format, choose_account, enable))
        }
    }

    override fun getDataFolder(): File {
        return dataFolder.toFile()
    }

    override fun saveResource(name: String, replace: Boolean) {
        val file = dataFolder.resolve(name).toFile()
        if (!replace && file.exists()) {
            return
        }
        this.javaClass.getResource("/$name")?.let { file.writeText(it.readText()) }
    }

    override fun registerCommand(command: String, handler: ACommand) {
        val commandManager = server.commandManager
        val commandMeta = commandManager.metaBuilder(command)
            .plugin(this)
            .build()
        server.commandManager.register(commandMeta, object : SimpleCommand {
            override fun execute(invocation: SimpleCommand.Invocation) {
                val source = invocation.source()
                val args = invocation.arguments()
                handler.onCommand(command, VelocitySender(source), args.toList())
            }

            override fun suggest(invocation: SimpleCommand.Invocation?): MutableList<String> {
                val args = invocation?.arguments() ?: emptyArray<String>()
                return handler.onComplete(args.toList()).toMutableList()
            }
        })
    }

    override fun getAllData(): Map<Long, List<AOfflinePlayer>> {
        return dataProvider.getAllData()
    }

    override fun submit(task: Runnable): Cancelable {
        val future = server.scheduler.buildTask(this, task).delay(Duration.ofMillis(0)).schedule()
        return VelocityTaskCancelable(future)
    }

    override fun submitAsync(task: Runnable): Cancelable {
        val future = executor.submit(task)
        return RunnableTaskCancelable(future)
    }

    override fun submitLater(delay: Long, task: Runnable): Cancelable {
        val future = server.scheduler.buildTask(this, task).delay(Duration.ofMillis(delay * 50L)).schedule()
        return VelocityTaskCancelable(future)
    }

    override fun submitLaterAsync(delay: Long, task: Runnable): Cancelable {
        val future = server.scheduler.buildTask(this, Runnable { executor.submit(task) })
            .delay(Duration.ofMillis(delay * 50L)).schedule()
        return VelocityTaskCancelable(future)
    }

    override fun submitTimer(delay: Long, period: Long, task: Runnable): Cancelable {
        val future = server.scheduler.buildTask(this, task)
            .delay(Duration.ofMillis(delay * 50L))
            .repeat(Duration.ofMillis(period * 50L))
            .schedule()
        return VelocityTaskCancelable(future)
    }

    override fun submitTimerAsync(delay: Long, period: Long, task: Runnable): Cancelable {
        val future = server.scheduler.buildTask(this, Runnable { executor.submit(task) })
            .delay(Duration.ofMillis(delay * 50L))
            .repeat(Duration.ofMillis(period * 50L))
            .schedule()
        return VelocityTaskCancelable(future)
    }

    override fun setSender() {
        enableGroups.forEach { group, _ ->
            this.sender[group.toLong()] = VelocityConsoleSender::class.java
        }
    }

    override fun getBrandName(): String {
        return server.version.name
    }

    override fun getServerVersion(): String {
        return server.version.version
    }

    @Subscribe
    fun onJoin(event: LoginEvent) {
        AJoinEvent(this, VelocityPlayer(event.player), {VelocityPlayer(event.player).kick(it)}).handle()
    }

    @Subscribe
    fun onQuit(event: DisconnectEvent) {
        AQuitEvent(this, VelocityPlayer(event.player)).handle()
    }

    @Subscribe
    fun onChat(event: PlayerChatEvent) {
        AChatEvent(this, VelocityPlayer(event.player), event.message).handle()
    }

    override fun loadDependencies() {
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
        libraryManager.loadLibraries(guavaLib, hikaricpLib, sqliteLib, mysqlLib, aconfigurationLib, databaseLib, aonebotLib)
    }

    override fun handleImage(url: String): TextComponent? {
        return null
    }
}