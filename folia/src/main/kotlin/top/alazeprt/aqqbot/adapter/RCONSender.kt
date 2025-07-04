package top.alazeprt.aqqbot.adapter

import org.glavo.rcon.Rcon
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.util.AExecution
import top.alazeprt.aqqbot.util.AFormatter
import java.util.concurrent.CompletableFuture

class RCONSender(private val plugin: AQQBot): AExecution {

    private lateinit var rcon: Rcon
    private lateinit var message: String

    fun check(groupId: Long?): Boolean {
        try {
            return initial(groupId)
        } catch (e: Exception) {
            return false
        }
    }

    private fun initial(groupId: Long?): Boolean {
        val address = plugin.generalConfig.getString("command_execution.rcon.host", groupId)
        val port = plugin.generalConfig.getInt("command_execution.rcon.port", groupId)
        val password = plugin.generalConfig.getString("command_execution.rcon.password", groupId)
        if (address == null || port !in 0..65535 || password == null) return false
        rcon = Rcon(address, port, password)
        return true
    }

    fun close() {
        rcon.close()
    }

    override fun execute(command: String, groupId: Long?): CompletableFuture<AExecution> {
        initial(groupId)
        message = rcon.command(command)
        return CompletableFuture.completedFuture(this)
    }

    override fun getRawString(): String {
        return message
    }

    override fun getFormattedString(groupId: Long?): String {
        return AFormatter(plugin).regexFilter(plugin.generalConfig.getStringList("command_execution.filter", groupId),
            AFormatter.chatClear(message)
        )
    }
}