package top.alazeprt.aqqbot.config

import top.alazeprt.aconfiguration.file.FileConfiguration
import top.alazeprt.aconfiguration.file.YamlConfiguration
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.util.ACustom
import top.alazeprt.aqqbot.util.AFormatter
import top.alazeprt.aqqbot.util.LogLevel
import java.io.File
import kotlin.math.log

interface ConfigProvider {

    var enableGroups: MutableList<String>
    val customCommands: MutableList<ACustom>

    var generalConfig: FileConfiguration
    var messageConfig: FileConfiguration
    var botConfig: FileConfiguration
    var customConfig: FileConfiguration

    fun loadConfig(plugin: AQQBot) {
        loadGeneralConfig()
        loadBotConfig()
        loadMessageConfig()
        loadCustomConfig()
        setEnableGroups()
    }

    fun setEnableGroups() {
        enableGroups = mutableListOf()
        botConfig.getStringList("groups")?.forEach {
            enableGroups.add(it)
        }
    }

    fun loadGeneralConfig() {
        val file = File(getDataFolder(), "config.yml")
        if (!file.exists()) {
            saveResource("config.yml", false)
        }
        generalConfig = YamlConfiguration.loadConfiguration(file)
        if (generalConfig.getInt("chat.max_forward_length") <= 0) {
            generalConfig.set("chat.max_forward_length", 200)
        }
        if (generalConfig.getInt("version") != 17) {
            generalConfig.set("version", 17)
            generalConfig.set("whitelist.cooldown.bind", 60)
            generalConfig.set("whitelist.cooldown.unbind", 86400)
        }
        if (generalConfig.getInt("version") != 18) {
            generalConfig.set("command_execution.sort", listOf("NATIVE", "DEDICATED_SERVER", "MINECRAFT_SERVER", "SIMULATE_CONSOLE"))
            generalConfig.set("command_execution.rcon.host", "127.0.0.1")
            generalConfig.set("command_execution.rcon.port", "25575")
            generalConfig.set("command_execution.rcon.password", "password")
            generalConfig.set("whitelist.name_rule", "[a-zA-Z0-9_]+")
        }
    }

    fun loadMessageConfig() {
        val file = File(getDataFolder(), "messages.yml")
        if (!file.exists()) {
            saveResource("messages.yml", false)
        }
        messageConfig = YamlConfiguration.loadConfiguration(file)
    }

    fun loadBotConfig() {
        val file = File(getDataFolder(), "bot.yml")
        if (!file.exists()) {
            saveResource("bot.yml", false)
        }
        botConfig = YamlConfiguration.loadConfiguration(file)
    }

    fun loadCustomConfig()

    fun getDataFolder(): File

    fun configNeedUpdate(): Boolean {
        if (generalConfig.getInt("version") != 18) {
            val file = File(getDataFolder(), "config_new.yml")
            this.javaClass.getResource("/config.yml")?.let { file.writeText(it.readText()) }
            return true
        }
        return false
    }

    fun saveResource(name: String, replace: Boolean)
}