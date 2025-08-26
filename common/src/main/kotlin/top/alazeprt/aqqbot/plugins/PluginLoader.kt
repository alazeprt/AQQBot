package top.alazeprt.aqqbot.plugins

import com.google.gson.Gson
import com.google.gson.JsonObject
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
    private val version = 1;

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
        expose("version", version)
        eventManager.load()
        val compilable = engine as Compilable
        if (plugin.getDataFolder().resolve("plugins").isDirectory) {
            plugin.getDataFolder().resolve("plugins").listFiles { file -> file.isDirectory }.forEach { dir ->
                plugin.log(LogLevel.INFO, "Reading the information of plugin directory ${dir.name}")
                val manifest = dir.resolve("manifest.json")
                if (!manifest.isFile) return@forEach
                val manifestContent = Gson().fromJson(manifest.readText(), JsonObject::class.java)
                val schemaVersion = manifestContent.get("schema_version").asInt
                if (schemaVersion > this.version) {
                    plugin.log(LogLevel.WARN, "Your plugin doesn't support the script schema version $schemaVersion (supported version is <= ${this.version}), please update your plugin!")
                    return@forEach
                }
                val name = if (manifestContent.has("name")) manifestContent.get("name").asString else dir.name
                val author = if (manifestContent.has("author")) manifestContent.get("author").asString else "Unknown"
                val version = if (manifestContent.has("version")) manifestContent.get("version").asString else "Unknown"
                val script = dir.resolve(manifestContent.get("entrypoint").asString)
                if (!script.isFile) return@forEach
                plugin.log(LogLevel.INFO, "Loading plugin $name version $version by $author")
                val compiled = compilable.compile(script.readText())
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