package top.alazeprt.aqqbot.config

import top.alazeprt.aconfiguration.file.FileConfiguration
import top.alazeprt.aconfiguration.file.YamlConfiguration
import top.alazeprt.aqqbot.util.ACustom
import java.io.File

interface ConfigProvider {

    var enableGroups: MutableList<String>
    val customCommands: MutableList<ACustom>

    var generalConfig: FileConfiguration
    var messageConfig: FileConfiguration
    var botConfig: FileConfiguration

    fun loadConfig() {
        loadGeneralConfig()
        loadBotConfig()
        loadMessageConfig()
        loadCustomConfig()
        setEnableGroups()
    }

    fun setEnableGroups() {
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
        if (generalConfig.getInt("chat.max_forward_length") == 0) {
            generalConfig.set("chat.max_forward_length", 200)
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

    fun getCustomConfig(): FileConfiguration

    fun getDataFolder(): File

    fun configNeedUpdate(): Boolean {
        if (generalConfig.getInt("version") != 16) {
            saveResource("config.yml", true)
            return true
        }
        return false
    }
    
    fun saveResource(name: String, replace: Boolean)
}