package top.alazeprt.aqqbot

import me.lucko.spark.api.Spark
import me.lucko.spark.api.SparkProvider
import org.bukkit.Bukkit
import taboolib.common.platform.function.warning
import top.alazeprt.aqqbot.event.AJoinEvent

class DependencyImpl {
    companion object {

        var spark: Spark? = null

        fun loadSpark() {
            try {
                Class.forName("me.lucko.spark.api.SparkProvider")
                spark = SparkProvider.get()
            } catch (e: Exception) {
                warning("You don't install soft dependency: Spark! You can't get server status via this plugin!")
            }
        }
    }
}