package top.alazeprt.aqqbot.hook

import net.luckperms.api.LuckPermsProvider
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.profile.APlayer
import top.alazeprt.aqqbot.util.LogLevel

interface HookProvider {

    var spark: Boolean

    var fakePlayer: Boolean

    var luckperms: Boolean

    var loadSparkCount: Int

    fun loadSpark(plugin: AQQBot) {
        try {
            Class.forName("me.lucko.spark.api.SparkProvider")
            spark = true
            if (loadSparkCount > 0) {
                plugin.log(LogLevel.INFO, "[Hook] spark has been loaded successfully!")
            }
            loadSparkCount = 0
        } catch (e: ClassNotFoundException) {
            plugin.log(LogLevel.WARN, "You don't install soft dependency: spark! You can't get server status via this plugin!")
        } catch (e: IllegalStateException) {
            if (loadSparkCount >= 5) {
                plugin.log(LogLevel.WARN, "After five attempts spark still does not work and will stop trying, you can then retry via the /aqqbot reload")
                return
            }
            plugin.log(LogLevel.WARN, "spark has not loaded yet! We'll try to load it 2 seconds later.")
            plugin.submitLaterAsync(40L) {
                loadSparkCount++
                loadSpark(plugin)
            }
        }
    }

    fun loadFakeplayer(plugin: AQQBot) {
        try {
            Class.forName("io.github.hello09x.fakeplayer.core.Main")
        } catch (ignored: ClassNotFoundException) {
            return
        }
        plugin.log(LogLevel.INFO, "[Hook] minecraft-fakeplayer has been loaded successfully!")
    }

    fun loadLuckPerms(plugin: AQQBot) {
        try {
            LuckPermsProvider.get()
        } catch (ignored: ClassNotFoundException) {
            return
        }
        plugin.log(LogLevel.INFO, "[Hook] luckperms has been loaded successfully!")
    }

    fun setPlaceholders(player: APlayer, message: String): String

    fun loadHook(plugin: AQQBot) {
        loadSpark(plugin)
        loadFakeplayer(plugin)
        loadLuckPerms(plugin)
    }
}