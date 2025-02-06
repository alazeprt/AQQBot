package top.alazeprt.aqqbot

import me.lucko.spark.api.Spark
import me.lucko.spark.api.SparkProvider
import taboolib.common.env.RuntimeDependencies
import taboolib.common.env.RuntimeDependency
import taboolib.common.platform.function.info
import taboolib.common.platform.function.submit
import taboolib.common.platform.function.submitAsync
import taboolib.common.platform.function.warning

@RuntimeDependencies(
    RuntimeDependency("!com.mysql:mysql-connector-j:8.4.0"),
    RuntimeDependency("!org.xerial:sqlite-jdbc:3.46.1.0")
)
class DependencyImpl {
    companion object {

        var spark: Spark? = null
        var withPAPI: Boolean = false
        private var loadCount = 0

        fun loadSpark() {
            try {
                Class.forName("me.lucko.spark.api.SparkProvider")
                spark = SparkProvider.get()
                if (loadCount > 0) {
                    info("Spark has been loaded successfully!")
                }
                loadCount = 0
            } catch (e: ClassNotFoundException) {
                warning("You don't install soft dependency: Spark! You can't get server status via this plugin!")
            } catch (e: IllegalStateException) {
                if (loadCount >= 5) {
                    warning("After five attempts Spark still does not work and will stop trying, you can then retry via the /aqqbot reload")
                }
                warning("Spark has not loaded yet! We'll try to load it 2 seconds later.")
                submitAsync(delay = 40L) {
                    loadCount++
                    loadSpark()
                }
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