package top.alazeprt.aqqbot.adapter

import org.bukkit.Bukkit
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.util.AExecution
import top.alazeprt.aqqbot.util.AFormatter
import top.alazeprt.aqqbot.util.AReflection
import java.lang.reflect.Method
import java.util.concurrent.CompletableFuture


class DecidatedServerSender(private val plugin: AQQBot): AExecution {

    private lateinit var method: Method
    private lateinit var dedicatedServer: Any
    private lateinit var message: String

    fun check(): Boolean {
        val server = Bukkit.getServer()
        dedicatedServer = AReflection.findFieldByType(server, "DedicatedServer") ?: return false
        try {
            method = dedicatedServer.javaClass.getMethod("runCommand", String::class.java)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override fun execute(command: String): CompletableFuture<AExecution> {
        message = method.invoke(dedicatedServer, command) as String
        return CompletableFuture.completedFuture(this)
    }

    override fun getRawString(): String {
        return message
    }

    override fun getFormattedString(): String {
        return AFormatter(plugin).regexFilter(plugin.generalConfig.getStringList("command_execution.filter"),
            AFormatter.chatClear(message)
        )
    }
}