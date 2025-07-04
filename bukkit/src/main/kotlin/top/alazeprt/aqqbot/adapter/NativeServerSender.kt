package top.alazeprt.aqqbot.adapter

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.util.AExecution
import top.alazeprt.aqqbot.util.AFormatter
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

class NativeServerSender(private val plugin: AQQBot) : AExecution {

    private lateinit var commandSender: CommandSender
    private val messageList: MutableList<String> = mutableListOf()

    fun check(): Boolean {
        try {
            val method = Class.forName("org.bukkit.Bukkit").getMethod("createCommandSender", Consumer::class.java)
            commandSender = method.invoke(null, Consumer<Component> { text ->
                val message = LegacyComponentSerializer.legacySection().serialize(text)
                messageList.add(message)
            }) as CommandSender
            return true
        } catch (e: Exception) {
            return false
        }
    }

    override fun execute(command: String, groupId: Long?): CompletableFuture<AExecution> {
        Bukkit.dispatchCommand(commandSender, command)
        return CompletableFuture.supplyAsync {
            Thread.sleep(plugin.generalConfig.getLong("command_execution.delay", groupId) * 1000L)
            this
        }
    }

    override fun getFormattedString(groupId: Long?): String {
        return AFormatter(plugin).regexFilter(plugin.generalConfig.getStringList("command_execution.filter", groupId),
            AFormatter.chatClear(messageList.joinToString("\n"))
        )
    }

    override fun getRawString(): String {
        return messageList.joinToString("\n")
    }
}