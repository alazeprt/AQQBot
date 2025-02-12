package top.alazeprt.aqqbot.hook

import top.alazeprt.aqqbot.util.LogLevel
import me.lucko.spark.api.Spark
import me.lucko.spark.api.SparkProvider
import top.alazeprt.aqqbot.AQQBot

interface HookProvider {

    var spark: Spark?

    var loadCount: Int

    fun loadSpark(plugin: AQQBot) {
        try {
            Class.forName("me.lucko.spark.api.SparkProvider")
            spark = SparkProvider.get()
            if (loadCount > 0) {
                plugin.log(LogLevel.WARN, "Spark has been loaded successfully!")
            }
            loadCount = 0
        } catch (e: ClassNotFoundException) {
            plugin.log(LogLevel.WARN, "You don't install soft dependency: Spark! You can't get server status via this plugin!")
        } catch (e: IllegalStateException) {
            if (loadCount >= 5) {
                plugin.log(LogLevel.WARN, "After five attempts Spark still does not work and will stop trying, you can then retry via the /aqqbot reload")
            }
            plugin.log(LogLevel.WARN, "Spark has not loaded yet! We'll try to load it 2 seconds later.")
            plugin.submitLaterAsync(40L) {
                loadCount++
                loadSpark(plugin)
            }
        }
    }

    fun loadHook(plugin: AQQBot) {
        loadSpark(plugin)
    }
}