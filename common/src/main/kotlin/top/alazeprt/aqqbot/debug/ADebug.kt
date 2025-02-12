package top.alazeprt.aqqbot.debug

import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.debug.logger.DebugLogger

class ADebug(val plugin: AQQBot) {

    var debugLogger: DebugLogger? = null

    fun load() {
        if (plugin.getGeneralConfig().getBoolean("debug.enable")) {
            debugLogger = DebugLogger(plugin)
            debugLogger?.initial()
        }
    }

    fun unload() {
        if (plugin.getGeneralConfig().getBoolean("debug.enable")) {
            debugLogger?.close()
            debugLogger = null
        }
    }

    fun reload() {
        if (plugin.getGeneralConfig().getBoolean("debug.enable") && debugLogger == null) {
            load()
        } else {
            unload()
        }
    }
}