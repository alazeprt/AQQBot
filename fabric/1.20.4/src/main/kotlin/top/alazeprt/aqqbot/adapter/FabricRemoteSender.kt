package top.alazeprt.aqqbot.adapter

import com.mojang.brigadier.ParseResults
import net.minecraft.server.command.ServerCommandSource
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.AQQBotFabric.Companion.server
import top.alazeprt.aqqbot.util.AExecution
import java.util.concurrent.CompletableFuture

class FabricRemoteSender(private val plugin: AQQBot) : AExecution {
    override fun getRawString(): String {
        return ""
    }

    override fun getFormattedString(groupId: Long?): String {
        return ""
    }

    override fun execute(
        command: String,
        groupId: Long?
    ): CompletableFuture<AExecution> {
        val pr: ParseResults<ServerCommandSource> = server.commandManager.dispatcher.parse(command, server.commandSource)
        server.commandManager.execute(pr, command)
        return CompletableFuture.supplyAsync {
            Thread.sleep(plugin.generalConfig.getLong("command_execution.delay", groupId) * 1000L)
            this
        }
    }
}