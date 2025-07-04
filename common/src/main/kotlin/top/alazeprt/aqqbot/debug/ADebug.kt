package top.alazeprt.aqqbot.debug

import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.debug.logger.DebugLogger

class ADebug(val plugin: AQQBot) {

    var debugLogger: DebugLogger? = null

    fun load() {
        if (plugin.generalConfig.getBoolean("debug.enable", null)) {
            debugLogger = DebugLogger(plugin)
            debugLogger?.initial()
        }
    }

    fun unload() {
        if (plugin.generalConfig.getBoolean("debug.enable", null)) {
            debugLogger?.close()
            debugLogger = null
        }
    }

    fun reload() {
        if (plugin.generalConfig.getBoolean("debug.enable", null) && debugLogger == null) {
            load()
        } else {
            unload()
        }
    }
}