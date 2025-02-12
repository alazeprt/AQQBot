package top.alazeprt.aqqbot.config

import top.alazeprt.aqqbot.util.ACustom
import java.io.File

interface ConfigProvider {

    var enableGroups: MutableList<String>

    val customCommands: MutableList<ACustom>

    fun loadConfig() {
        loadGeneralConfig()
        loadBotConfig()
        loadMessageConfig()
        loadCustomConfig()
        setEnableGroups()
    }

    fun setEnableGroups()

    fun loadGeneralConfig()

    fun loadMessageConfig()

    fun loadBotConfig()

    fun loadCustomConfig()

    fun getGeneralConfig(): Configuration

    fun getMessageConfig(): Configuration

    fun getCustomConfig(): Configuration

    fun getBotConfig(): Configuration

    fun getDataFolder(): File

    fun configNeedUpdate(): Boolean
}