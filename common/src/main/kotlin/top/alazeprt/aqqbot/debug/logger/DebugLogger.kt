package top.alazeprt.aqqbot.debug.logger

import top.alazeprt.aqqbot.AQQBot
import java.io.File
import java.util.*

open class DebugLogger(val plugin: AQQBot) {

    private val loggerList = mutableListOf<String>()

    private lateinit var loggerFile: File

    private val config = plugin.getGeneralConfig()

    private var initialized = false

    fun log(message: String) {
        if (!config.getBoolean("debug.enable") || !config.getBoolean("debug.logger.enable")) return
        val time = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        if (config.getLong("debug.logger.save_interval") == 0L) {
            loggerFile.appendText("[$time] $message\n")
        } else {
            loggerList.add("[$time] $message\n")
        }
    }

    fun initial() {
        loggerFile = File(plugin.getDataFolder(), config.getString("debug.logger.file")?: "debug.log")
        if (config.getLong("debug.logger.save_interval") >= 1L) {
            plugin.submitTimerAsync(0L, config.getLong("debug.logger.save_interval") * 20L) {
                if (loggerList.isNotEmpty()) {
                    loggerFile.appendText(loggerList.joinToString(""))
                }
            }
        }
        initialized = true
    }

    fun close() {
        if (config.getLong("debug.logger.save_interval") != 0L) {
            if (loggerList.isNotEmpty()) {
                loggerFile.appendText(loggerList.joinToString(""))
            }
        }
        initialized = false
    }

    fun reload(enable: Boolean) {
        if (enable && !initialized) {
            initial()
        } else if (!enable && initialized) {
            close()
        }
    }
}