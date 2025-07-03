package top.alazeprt.aqqbot.adapter

import org.bukkit.Bukkit
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.util.AExecution
import top.alazeprt.aqqbot.util.AFormatter
import top.alazeprt.aqqbot.util.AReflection
import java.lang.reflect.Method
import java.net.InetSocketAddress
import java.util.concurrent.CompletableFuture

class MinecraftServerSender(private val plugin: AQQBot): AExecution {

    private lateinit var minecraftServer: Any
    private lateinit var method: Method
    private lateinit var rconConsoleSource: Any
    private lateinit var message: String

    fun check(): Boolean {
        val server = Bukkit.getServer()
        minecraftServer = AReflection.findFieldByType(server, "MinecraftServer")?: return false
        try {
            method = minecraftServer.javaClass.getMethod("runCommand", getRconConsoleSourceClassPath(), String::class.java)
            val rconConsoleSourceConstructor = getRconConsoleSourceClassPath().constructors[0]
            rconConsoleSource = rconConsoleSourceConstructor.newInstance(minecraftServer,
                InetSocketAddress.createUnresolved("", 0))
            return true
        } catch (e: Exception) {
            try {
                method = minecraftServer.javaClass.getMethod("executeRemoteCommand", String::class.java)
                return true
            } catch (e1: Exception) {
                return false
            }
        }
    }

    override fun execute(command: String): CompletableFuture<AExecution> {
        message = if (method.name == "runCommand") {
            method.invoke(minecraftServer, rconConsoleSource, command) as String
        } else {
            method.invoke(minecraftServer, command) as String
        }
        return CompletableFuture.completedFuture(this)
    }

    @Throws(ClassNotFoundException::class)
    private fun getRconConsoleSourceClassPath(): Class<*> {
        return try {
            Class.forName("net.minecraft.server.rcon.RconConsoleSource")
        } catch (classNotFoundException: ClassNotFoundException) {
            try {
                Class.forName("net.minecraft.server.rcon.RemoteControlCommandListener")
            } catch (classNotFoundException2: ClassNotFoundException) {
                throw ClassNotFoundException("Can not find RconConsoleSource class path")
            }
        }
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