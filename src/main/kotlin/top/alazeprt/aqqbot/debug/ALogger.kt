package top.alazeprt.aqqbot.debug

import taboolib.common.platform.function.submitAsync
import top.alazeprt.aqqbot.AQQBot.config
import top.alazeprt.aqqbot.AQQBot.dataFolder
import java.io.File
import java.util.*

object ALogger {
    private lateinit var loggerFile: File

    private val loggerList = mutableListOf<String>()

    fun initialize() {
        loggerFile = File(dataFolder, config.getString("debug.logger.file")?: "debug.log")
        if (config.getLong("debug.logger.save_interval") >= 1L) {
            submitAsync(period = config.getLong("debug.logger.save_interval") * 20L) {
                if (loggerList.isNotEmpty()) {
                    loggerFile.appendText(loggerList.joinToString(""))
                }
            }
        }
    }

    fun close() {
        if (config.getLong("debug.logger.save_interval") != 0L) {
            if (loggerList.isNotEmpty()) {
                loggerFile.appendText(loggerList.joinToString(""))
            }
        }
    }

    fun log(message: String) {
        if (!config.getBoolean("debug.enable") || !config.getBoolean("debug.logger.enable")) return
        val time = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())
        if (config.getLong("debug.logger.save_interval") == 0L) {
            loggerFile.appendText("[$time] $message\n")
        } else {
            loggerList.add("[$time] $message\n")
        }
    }
}