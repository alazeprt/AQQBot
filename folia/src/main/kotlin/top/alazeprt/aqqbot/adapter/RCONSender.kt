package top.alazeprt.aqqbot.adapter

import org.glavo.rcon.Rcon
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.util.AExecution
import top.alazeprt.aqqbot.util.AFormatter
import java.util.concurrent.CompletableFuture

class RCONSender(private val plugin: AQQBot): AExecution {

    private lateinit var rcon: Rcon
    private lateinit var message: String

    fun check(): Boolean {
        try {
            return initial()
        } catch (e: Exception) {
            return false
        }
    }

    private fun initial(): Boolean {
        val address = plugin.generalConfig.getString("command_execution.rcon.host")
        val port = plugin.generalConfig.getInt("command_execution.rcon.port")
        val password = plugin.generalConfig.getString("command_execution.rcon.password")
        if (address == null || port !in 0..65535 || password == null) return false
        rcon = Rcon(address, port, password)
        return true
    }

    fun close() {
        rcon.close()
    }

    override fun execute(command: String): CompletableFuture<AExecution> {
        initial()
        message = rcon.command(command)
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