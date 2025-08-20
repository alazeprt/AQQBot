package top.alazeprt.aqqbot.plugins

import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.api.AQQBotAPI
import top.alazeprt.aqqbot.api.event.game.PluginStartEvent
import top.alazeprt.aqqbot.api.event.game.PluginStopEvent
import top.alazeprt.aqqbot.bot.BotManager
import top.alazeprt.aqqbot.util.LogLevel
import javax.script.Compilable
import javax.script.ScriptEngine
import javax.script.ScriptEngineManager

class PluginLoader(val plugin: AQQBot) {
    private var engine: ScriptEngine? = null
    private val eventManager = JSEventManager(plugin)

    fun load() {
        try {
            engine = ScriptEngineManager().getEngineByName("nashorn")
            if (engine == null) throw NullPointerException("engine is null")
        } catch (e1: Exception) {
            try {
                val factoryClass = Class.forName("org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory")
                val factory = factoryClass.getConstructor().newInstance()
                val engineMethod = factoryClass.getDeclaredMethod("getScriptEngine")
                engine = engineMethod.invoke(factory) as ScriptEngine
                if (engine == null) throw NullPointerException("engine is null")
            } catch (e2: Exception) {
                try {
                    val factoryClass = Class.forName("jdk.nashorn.api.scripting.NashornScriptEngineFactory")
                    val factory = factoryClass.getConstructor().newInstance()
                    val engineMethod = factoryClass.getDeclaredMethod("getScriptEngine")
                    engine = engineMethod.invoke(factory) as ScriptEngine
                    if (engine == null) throw NullPointerException("engine is null")
                } catch (e3: Exception) {
                    plugin.log(LogLevel.ERROR, "Failed to load Nashorn JavaScript engine, plugin feature won't work normally! Error: $e1, $e2, $e3")
                }
            }
        }
        expose("plugin", plugin)
        expose("eventManager", eventManager)
        expose("botManager", BotManager)
        eventManager.load()
        val compilable = engine as Compilable
        if (plugin.getDataFolder().resolve("plugins").isDirectory) {
            plugin.getDataFolder().resolve("plugins").listFiles { file -> file.name.endsWith(".js") }.forEach { file ->
                plugin.log(LogLevel.INFO, "Loading plugin ${file.name}")
                val script = file.readText()
                val compiled = compilable.compile(script)
                compiled.eval()
            }
        }
        AQQBotAPI.fireEvent(PluginStartEvent())
    }

    fun expose(name: String, instance: Any) {
        engine?.put(name, instance)
    }

    fun unload() {
        AQQBotAPI.fireEvent(PluginStopEvent())
        eventManager.unload()
    }
}