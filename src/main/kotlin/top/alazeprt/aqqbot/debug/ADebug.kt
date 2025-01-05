package top.alazeprt.aqqbot.debug

import taboolib.common.platform.function.info
import top.alazeprt.aqqbot.AQQBot.config

object ADebug {

    fun initialize() {
        info("AQQBot debug system initializing ...")
        if (config.getBoolean("debug.logger.enable")) {
            ALogger.initialize()
        }
        info("AQQBot debug system initialized")
    }

    fun shutdown() {
        info("AQQBot debug system shutting down ...")
        if (config.getBoolean("debug.logger.enable")) {
            ALogger.close()
        }
        info("AQQBot debug system shutdown")
    }
}