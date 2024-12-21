package top.alazeprt.aqqbot

import me.lucko.spark.api.Spark
import me.lucko.spark.api.SparkProvider
import taboolib.common.platform.function.warning

class DependencyImpl {
    companion object {

        var spark: Spark? = null
        var withPAPI: Boolean = false

        fun loadSpark() {
            try {
                Class.forName("me.lucko.spark.api.SparkProvider")
                spark = SparkProvider.get()
            } catch (e: Exception) {
                warning("You don't install soft dependency: Spark! You can't get server status via this plugin!")
            }
        }

        fun loadPAPI() {
            try {
                Class.forName("me.clip.placeholderapi.PlaceholderAPI")
                withPAPI = true
            } catch (e: Exception) {
                warning("You don't install soft dependency: PlaceholderAPI! You can't use PAPI placeholders!")
            }
        }
    }
}