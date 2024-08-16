package top.alazeprt.aqqbot

import cn.evole.onebot.client.OneBotClient
import cn.evole.onebot.client.core.BotConfig
import me.lucko.spark.api.Spark
import org.bukkit.Bukkit
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.Plugin
import taboolib.common.platform.function.getDataFolder
import taboolib.common.platform.function.info
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.warning
import taboolib.module.configuration.Config
import taboolib.module.configuration.ConfigFile
import top.alazeprt.aqqbot.qq.BotListener
import java.io.File


@RuntimeDependencies(
    RuntimeDependency(
        value = "org.java-websocket:Java-WebSocket:1.5.5",
    ),
    RuntimeDependency(
        value = "net.kyori:event-method:3.0.0",
    ),
    RuntimeDependency(
        value = "net.kyori:event-api:3.0.0",
    ),
    RuntimeDependency(
        value = "cn.evole.onebot:OneBot-Client:0.4.0",
        repository = "https://maven.nova-committee.cn/releases"
    ),
)
object AQQBot : Plugin() {

    @Config("bot.yml")
    lateinit var botConfig: ConfigFile

    @Config("data.yml")
    lateinit var dataConfig: ConfigFile

    @Config("config.yml")
    lateinit var config: ConfigFile

    lateinit var oneBotClient: OneBotClient

    var spark: Spark? = null

    val enableGroups: MutableList<String> = mutableListOf()

    val dataMap: MutableMap<String, String> = mutableMapOf()

    override fun onActive() {
        info("Loading data...")
        dataConfig.getKeys(false).forEach {
            dataMap[it] = (dataConfig.getString(it)?: return@forEach)
        }
        botConfig.getStringList("groups").forEach {
            enableGroups.add(it)
        }
        info("Loading soft dependency...")
        val provider = Bukkit.getServicesManager().getRegistration(
            Spark::class.java
        )
        if (provider != null) {
            spark = provider.provider
        } else {
            warning("You don't install soft dependency: Spark! You can't get server status via this plugin!")
        }
        submit(async = true) {
            info("Enabling bot...")
            val url = "ws://" + botConfig.getString("ws.host") + ":" + botConfig.getInt("ws.port")
            oneBotClient = OneBotClient.create(BotConfig(url)).open().registerEvents(BotListener())
        }
    }

    override fun onDisable() {
        dataMap.forEach {
            dataConfig[it.key] = it.value
        }
        dataConfig.saveToFile(File(getDataFolder(), "data.yml"))
    }
}