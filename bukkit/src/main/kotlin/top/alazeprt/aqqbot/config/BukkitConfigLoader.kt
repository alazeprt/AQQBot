package top.alazeprt.aqqbot.config

import top.alazeprt.aconfiguration.file.FileConfiguration
import top.alazeprt.aconfiguration.file.YamlConfiguration
import top.alazeprt.aqqbot.AQQBotBukkit
import top.alazeprt.aqqbot.adapter.ABukkitCustom
import top.alazeprt.aqqbot.util.ACustom
import java.io.File

class BukkitConfigLoader(private val plugin: AQQBotBukkit) : ConfigProvider {
    override var enableGroups: MutableList<String> = mutableListOf()
    override val customCommands: MutableList<ACustom> = mutableListOf()
    override lateinit var generalConfig: FileConfiguration
    override lateinit var messageConfig: FileConfiguration
    override lateinit var botConfig: FileConfiguration

    private lateinit var customConfig: FileConfiguration

    override fun loadCustomConfig() {
        val file = File(plugin.getDataFolder(), "custom.yml")
        if (!file.exists()) {
            plugin.saveResource("custom.yml", false)
        }
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
                customCommands.add(ABukkitCustom(plugin, command, execute, unbind_execute, output, unbind_output, format, choose_account))
            }
        }
    }

    override fun getCustomConfig(): FileConfiguration {
        return customConfig
    }

    override fun getDataFolder(): File {
        return plugin.dataFolder
    }

    override fun saveResource(name: String, replace: Boolean) {
        plugin.saveResource(name, replace)
    }

}