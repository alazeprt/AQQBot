package top.alazeprt.aqqbot

import top.alazeprt.aqqbot.util.LogLevel
import top.alazeprt.aqqbot.adapter.AQQBotAdapter
import top.alazeprt.aqqbot.bot.BotProvider.loadBot
import top.alazeprt.aqqbot.bot.BotProvider.unloadBot
import top.alazeprt.aqqbot.command.CommandProvider
import top.alazeprt.aqqbot.config.ConfigProvider
import top.alazeprt.aqqbot.config.MessageManager
import top.alazeprt.aqqbot.data.DataProvider
import top.alazeprt.aqqbot.data.DataStorageType
import top.alazeprt.aqqbot.debug.ADebug
import top.alazeprt.aqqbot.hook.HookProvider
import top.alazeprt.aqqbot.task.TaskProvider
import java.net.URI

interface AQQBot: ConfigProvider, CommandProvider, DataProvider, HookProvider, TaskProvider {

    var debugModule: ADebug?

    var adapter: AQQBotAdapter?

    val verifyCodeMap: MutableMap<String, Pair<String, Long>>  // <name, <code, time>>

    fun enable() {
        loadConfig()
        loadData(DataStorageType.valueOf(getGeneralConfig().getString("storage.type")))
        loadDebug()
        loadCommands(this)
        adapter = loadAdapter()
        loadBot(
            this,
            URI.create("ws://" + getBotConfig().getString("ws.host") + ":" + getBotConfig().getInt("ws.port"))
        )
        loadHook(this)
    }

    fun disable() {
        unloadBot()
        saveData(DataStorageType.valueOf(getGeneralConfig().getString("storage.type")))
        unloadDebug()
    }

    fun reload() {
        loadConfig()
        reloadDebug()
    }

    fun loadDebug() {
        debugModule = ADebug(this)
        debugModule?.load()
    }

    fun unloadDebug() {
        debugModule?.unload()
        debugModule = null
    }

    fun loadAdapter(): AQQBotAdapter

    fun reloadDebug() {
        debugModule?.reload()
    }

    fun log(level: LogLevel, message: String)

    fun getMessageManager(): MessageManager {
        return MessageManager(this)
    }
}