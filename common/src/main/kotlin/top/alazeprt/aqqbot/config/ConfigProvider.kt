package top.alazeprt.aqqbot.config

import top.alazeprt.aconfiguration.file.FileConfiguration
import top.alazeprt.aconfiguration.file.YamlConfiguration
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.util.ACustom
import top.alazeprt.aqqbot.util.GroupConfiguration
import java.io.File
import java.nio.file.Files

interface ConfigProvider {

    var enableGroups: MutableMap<String, FileConfiguration?>
    val customCommands: MutableList<ACustom>
    var messageManager: MessageManager

    var generalConfig: GroupConfiguration
    var messageConfig: FileConfiguration
    var botConfig: FileConfiguration
    var customConfig: FileConfiguration

    fun loadConfig(plugin: AQQBot) {
        loadGeneralConfig(plugin)
        loadBotConfig()
        loadMessageConfig()
        loadCustomConfig()
        releasePluginsDevFile()
        Files.createDirectories(plugin.getDataFolder().resolve("images").toPath())
        setEnableGroups(plugin)
        updateGeneralConfig()
    }

    fun releasePluginsDevFile() {
        val file = File(getDataFolder().resolve("plugins"), "aqqbot.d.ts")
        if (!file.exists()) {
            file.parentFile.mkdirs()
            saveResource("plugins/aqqbot.d.ts", false)
        }
    }

    fun setEnableGroups(plugin: AQQBot) {
        enableGroups = mutableMapOf()
        botConfig.getStringList("groups")?.forEach {
            val file = File(getDataFolder(), "subconfig/$it.yml")
            if (file.exists()) {
                enableGroups[it] = YamlConfiguration.loadConfiguration(file)
            } else {
                enableGroups[it] = null
            }
        }
        messageManager = MessageManager(plugin)
        botConfig.getStringList("groups")?.forEach {
            val file = File(getDataFolder(), "submessages/$it.yml")
            if (file.exists()) {
                messageManager.enableGroups[it] = YamlConfiguration.loadConfiguration(file)
            } else {
                messageManager.enableGroups[it] = null
            }
        }
    }

    fun updateGeneralConfig() {
        if (generalConfig.getInt("chat.max_forward_length", null) <= 0) {
            generalConfig.setIfNotExists("chat.max_forward_length", 200)
        }
        if (generalConfig.getInt("version", null) < 17) {
            generalConfig.setIfNotExists("version", 17)
            generalConfig.setIfNotExists("whitelist.cooldown.bind", 60)
            generalConfig.setIfNotExists("whitelist.cooldown.unbind", 86400)
        }
        if (generalConfig.getInt("version", null) < 18) {
            generalConfig.setIfNotExists("command_execution.sort", listOf("NATIVE", "DEDICATED_SERVER", "MINECRAFT_SERVER", "SIMULATE_CONSOLE"))
            generalConfig.setIfNotExists("whitelist.name_rule", """[\S]*""")
        }
        // config version 20
        generalConfig.setIfNotExists("whitelist.bypass_permission", "aqqbot.bypass.whitelist")
        // config version 21
        generalConfig.setIfNotExists("notify.player_death.enable", false)
        generalConfig.setIfNotExists("notify.player_death.message", "[AQQBot] \${playerName}(\${userId}) 因 \${deathMessage} 死亡了!")
        // config version 22
        generalConfig.setIfNotExists("information.at.enable", true)
        generalConfig.setIfNotExists("information.at.message", mutableListOf("@\${player}", "\${player}"))
        generalConfig.setIfNotExists("information.at.action", mutableListOf("playsound block.bell.use master \${player}",
            "title @a subtitle {\"text\":\"[AQQBot] \${userId} @了你!\",\"color\":\"gold\"}"))
    }

    fun loadGeneralConfig(plugin: AQQBot) {
        val file = File(getDataFolder(), "config.yml")
        if (!file.exists()) {
            saveResource("config.yml", false)
        }
        generalConfig = GroupConfiguration(plugin, YamlConfiguration.loadConfiguration(file))
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
        if (generalConfig.getInt("version", null) != 22) {
            val file = File(getDataFolder(), "config_new.yml")
            this.javaClass.getResource("/config.yml")?.let { file.writeText(it.readText()) }
            return true
        }
        return false
    }

    fun saveResource(name: String, replace: Boolean)
}
