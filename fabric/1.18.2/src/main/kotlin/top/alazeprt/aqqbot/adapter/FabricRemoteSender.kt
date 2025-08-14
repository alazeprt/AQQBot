package top.alazeprt.aqqbot.adapter

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
        server.commandManager.execute(server.commandSource, command)
        return CompletableFuture.supplyAsync {
            Thread.sleep(plugin.generalConfig.getLong("command_execution.delay", groupId) * 1000L)
            this
        }
    }
}