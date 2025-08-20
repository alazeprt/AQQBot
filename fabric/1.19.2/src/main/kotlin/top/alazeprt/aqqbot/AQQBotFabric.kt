package top.alazeprt.aqqbot

import com.alessiodp.libby.FabricLibraryManager
import com.alessiodp.libby.Library
import com.alessiodp.libby.LibraryManager
import com.mojang.brigadier.arguments.StringArgumentType
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.kyori.adventure.platform.fabric.FabricServerAudiences
import net.kyori.adventure.text.TextComponent
import net.minecraft.server.MinecraftServer
import net.minecraft.server.command.CommandManager
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import top.alazeprt.aconfiguration.file.FileConfiguration
import top.alazeprt.aconfiguration.file.YamlConfiguration
import top.alazeprt.aqqbot.adapter.*
import top.alazeprt.aqqbot.command.ACommand
import top.alazeprt.aqqbot.config.MessageManager
import top.alazeprt.aqqbot.data.DataProvider
import top.alazeprt.aqqbot.debug.ADebug
import top.alazeprt.aqqbot.drivers.Web2ImageDriver
import top.alazeprt.aqqbot.event.AChatEvent
import top.alazeprt.aqqbot.event.ADeathEvent
import top.alazeprt.aqqbot.event.AJoinEvent
import top.alazeprt.aqqbot.event.AQuitEvent
import top.alazeprt.aqqbot.plugins.PluginLoader
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import top.alazeprt.aqqbot.profile.APlayer
import top.alazeprt.aqqbot.util.*
import java.io.File
import java.util.*
import java.util.concurrent.ConcurrentHashMap


class AQQBotFabric : ModInitializer, AQQBot {
    override fun onInitialize() {
        libraryManager = FabricLibraryManager("aqqbot", FabricLogAdapter(LOGGER), "lib")
        this.enable()
        ServerLifecycleEvents.SERVER_STARTED.register { server ->
            audience = FabricServerAudiences.of(server)
            AQQBotFabric.server = server
        }
        ServerLifecycleEvents.SERVER_STOPPING.register { server ->
            audience.close()
            FabricScheduler.cancelAllTasks()
            this.disable()
        }
        ServerPlayConnectionEvents.JOIN.register { handler, sender, server ->
            AJoinEvent(this, FabricPlayer(handler.player)) {
                handler.disconnect(Text.of(it))
            }.handle()
        }
        ServerPlayConnectionEvents.DISCONNECT.register { handler, server ->
            AQuitEvent(this, FabricPlayer(handler.player)).handle()
        }
        ServerMessageEvents.CHAT_MESSAGE.register { message, entity, parameters ->
            AChatEvent(this, FabricPlayer(entity), message.content.string).handle()
        }
        ServerLivingEntityEvents.AFTER_DEATH.register { player, source ->
            if (player is ServerPlayerEntity) {
                ADeathEvent(this, FabricPlayer(player), source.name).handle()
            }
            true
        }
    }

    override lateinit var pluginLoader: PluginLoader

    override var debugModule: ADebug? = null

    override var adapter: AQQBotAdapter = FabricAdapter

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

    override lateinit var webDriver: Web2ImageDriver

    override var fakePlayer: Boolean = false

    private val pluginId = 24071

    override lateinit var serverUUID: UUID

    override var spark: Boolean = false
    override var luckperms: Boolean = false

    override var loadSparkCount: Int = 0

    companion object {
        const val MOD_ID: String = "aqqbot"
        val LOGGER: Logger = LogManager.getLogger(MOD_ID)
        lateinit var audience: FabricServerAudiences
        lateinit var server: MinecraftServer
    }

    override fun loadDependencies() {
        val adventurePlatformFabricLib = Library.builder()
            .groupId("net.kyori") // 注意：此依赖未提供group前缀
            .artifactId("adventure-platform-fabric")
            .version("5.3.1")
            .resolveTransitiveDependencies(false)
            .build()

        val adventureApiLib = Library.builder()
            .groupId("net.kyori")
            .artifactId("adventure-api")
            .version("4.11.0")
            .resolveTransitiveDependencies(false)
            .build()

        val adventureKeyLib = Library.builder()
            .groupId("net.kyori")
            .artifactId("adventure-key")
            .version("4.11.0")
            .resolveTransitiveDependencies(false)
            .build()

        val adventurePlatformApiLib = Library.builder()
            .groupId("net.kyori")
            .artifactId("adventure-platform-api")
            .version("4.1.0")
            .resolveTransitiveDependencies(false)
            .build()

        val adventureTextLoggerSlf4jLib = Library.builder()
            .groupId("net.kyori")
            .artifactId("adventure-text-logger-slf4j")
            .version("4.11.0")
            .resolveTransitiveDependencies(false)
            .build()

        val adventureTextMinimessageLib = Library.builder()
            .groupId("net.kyori")
            .artifactId("adventure-text-minimessage")
            .version("4.11.0")
            .resolveTransitiveDependencies(false)
            .build()

        val adventureTextSerializerGsonLib = Library.builder()
            .groupId("net.kyori")
            .artifactId("adventure-text-serializer-gson")
            .version("4.11.0")
            .resolveTransitiveDependencies(false)
            .build()

        val adventureTextSerializerPlainLib = Library.builder()
            .groupId("net.kyori")
            .artifactId("adventure-text-serializer-plain")
            .version("4.11.0")
            .resolveTransitiveDependencies(false)
            .build()

        val examinationApiLib = Library.builder()
            .groupId("net.kyori")
            .artifactId("examination-api")
            .version("1.3.0")
            .resolveTransitiveDependencies(false)
            .build()

        val examinationStringLib = Library.builder()
            .groupId("net.kyori")
            .artifactId("examination-string")
            .version("1.3.0")
            .resolveTransitiveDependencies(false)
            .build()

        libraryManager.loadLibraries(adventurePlatformFabricLib, adventureApiLib, adventureKeyLib, adventurePlatformApiLib, adventureTextLoggerSlf4jLib, adventureTextMinimessageLib, adventureTextSerializerGsonLib, adventureTextSerializerPlainLib, examinationApiLib, examinationStringLib)
    }

    override fun loadAdapter(): AQQBotAdapter {
        return adapter!!
    }

    override fun log(level: LogLevel, message: String) {
        when (level) {
            LogLevel.DEBUG -> LOGGER.debug(message)
            LogLevel.WARN -> LOGGER.warn(message)
            LogLevel.ERROR -> LOGGER.error(message)
            LogLevel.INFO -> LOGGER.trace(message)
            LogLevel.FATAL -> LOGGER.error(message)
            LogLevel.TRACE -> LOGGER.trace(message)
        }
    }

    override fun setSender() {
        enableGroups.forEach { group, _ ->
            this.sender[group.toLong()] = FabricRemoteSender::class.java
        }
    }

    override fun getBrandName(): String {
        return server.name
    }

    override fun getServerVersion(): String {
        return server.version
    }

    override fun handleImage(url: String): TextComponent? {
        return null
    }

    override fun loadCustomConfig() {
        val file = getDataFolder().resolve("custom.yml")
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
                image = AImage(File(getDataFolder().resolve("images"), path), elements)
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
                unbind_image = AImage(File(getDataFolder().resolve("images"), path), elements)
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
                web = AWeb(File(getDataFolder().resolve("web"), path), width, height, delay, placeholdersMap)
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
                unbind_web = AWeb(File(getDataFolder().resolve("web"), path), width, height, delay, placeholdersMap)
            }
            if ((web != null || unbind_web != null) && enable) {
                webDriver = Web2ImageDriver(this)
                webDriver.loadDependencies()
                webDriver.downloadDrivers()
            }
            val format = customConfig.getBoolean("$it.format")
            val choose_account = if (customConfig.getInt("$it.choose_account") == 0) 1
            else customConfig.getInt("$it.choose_account")
            val permission = customConfig.getString("$it.permission") ?: ""
            customCommands.add(AFabricCustom(
                this, it, command, execute, unbind_execute, output, unbind_output, image, unbind_image, format, web, unbind_web, choose_account, enable, permission))
        }
    }

    override fun getDataFolder(): File {
        return FabricLoader.getInstance().configDir.resolve("aqqbot").toFile()
    }

    override fun saveResource(name: String, replace: Boolean) {
        val file = getDataFolder().resolve(name)
        if (!replace && file.exists()) {
            return
        }
        this.javaClass.getResource("/$name")?.let { file.writeText(it.readText()) }
    }

    override fun registerCommand(command: String, handler: ACommand) {
        CommandRegistrationCallback.EVENT.register(CommandRegistrationCallback { dispatcher, _, _ ->
            dispatcher.register(CommandManager.literal(command)
                .executes { ctx ->
                    handler.onCommand(command, FabricSender(ctx.source), emptyList())
                    1
                }
                .then(CommandManager.argument("args", StringArgumentType.greedyString())
                    .executes { ctx ->
                        val allArgs = StringArgumentType.getString(ctx, "args")
                        val argsList = allArgs.split(" ").filter { it.isNotBlank() }
                        handler.onCommand(command, FabricSender(ctx.source), argsList)
                        1
                    }
                )
            )
        })
    }

    override fun getAllData(): Map<Long, List<AOfflinePlayer>> {
        return dataProvider.getAllData()
    }

    override fun setPlaceholders(player: APlayer, message: String): String {
        return message
    }

    override fun submit(task: Runnable): Cancelable {
        return FabricScheduler.runTask(task)
    }

    override fun submitAsync(task: Runnable): Cancelable {
        return FabricScheduler.runTaskAsync(task)
    }

    override fun submitLater(delay: Long, task: Runnable): Cancelable {
        return FabricScheduler.runTaskLater(task, delay)
    }

    override fun submitLaterAsync(delay: Long, task: Runnable): Cancelable {
        return FabricScheduler.runTaskLaterAsync(task, delay)
    }

    override fun submitTimer(
        delay: Long,
        period: Long,
        task: Runnable
    ): Cancelable {
        return FabricScheduler.runTaskTimerAsync(task, delay, period)
    }

    override fun submitTimerAsync(
        delay: Long,
        period: Long,
        task: Runnable
    ): Cancelable {
        return FabricScheduler.runTaskTimerAsync(task, delay, period)
    }
}